package com.yihong.growth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yihong.growth.entity.Article;
import com.yihong.growth.mapper.ArticleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 将 CSDN RSS 同步到文章表。
 * 新文章默认不在前台展示，已存在文章保留封面、推荐和展示状态。
 */
@Service
@RequiredArgsConstructor
public class CsdnArticleSyncService {

    private static final ZoneId SITE_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Pattern HTML_TAG = Pattern.compile("<[^>]+>");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern IMAGE_TAG = Pattern.compile("<img\\b[^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern IMAGE_SOURCE = Pattern.compile(
            "\\b(?:data-src|src)\\s*=\\s*([\"'])(.*?)\\1", Pattern.CASE_INSENSITIVE);
    private static final Pattern CONTENT_START = Pattern.compile(
            "<div\\s+[^>]*id=[\"']content_views[\"'][^>]*>", Pattern.CASE_INSENSITIVE);
    private static final Pattern ARTICLE_ID = Pattern.compile("/article/details/(\\d+)");

    private static final List<CategoryRule> CATEGORY_RULES = List.of(
        new CategoryRule("mobile", List.of(
            "harmonyos", "arkts", "arkui", "鸿蒙", "form", "rck",
            "remote communication", "rdb", "kv-store", "preferences")),
        new CategoryRule("ai", List.of(
            "rag", "ai", "知识问答", "大模型", "机器学习", "python",
            "数据分析", "深度学习", "nlp", "llm", "embedding", "向量")),
        new CategoryRule("backend", List.of(
            "socket", "websocket", "http", "tcp", "udp", "tls", "spring",
            "java", "mysql", "redis", "api", "restful", "网络编程", "后端")),
        new CategoryRule("frontend", List.of(
            "vue", "react", "css", "html", "javascript", "typescript", "前端", "web", "组件", "ui")),
        new CategoryRule("tools", List.of(
            "git", "docker", "linux", "nginx", "防火墙", "devops", "ci/cd", "命令行", "终端", "shell"))
    );

    private final ArticleMapper articleMapper;
    private final ObjectMapper objectMapper;

    @Value("${csdn.rss-url}")
    private String rssUrl;

    @Value("${csdn.author-name:YiHong}")
    private String defaultAuthor;

    @Value("${csdn.sync-timeout-seconds:20}")
    private int timeoutSeconds;

    /**
     * 单实例内串行同步，避免连续点击造成重复插入。
     */
    public synchronized SyncResult sync() {
        try {
            URI uri = URI.create(rssUrl);
            if (!"https".equalsIgnoreCase(uri.getScheme())) {
                throw new CsdnSyncException("RSS 地址必须使用 HTTPS");
            }

            Duration timeout = Duration.ofSeconds(Math.max(5, timeoutSeconds));
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(timeout)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(timeout)
                    .header("User-Agent", "Mozilla/5.0 (compatible; OrangePortfolio/1.0)")
                    .header("Accept", "application/rss+xml, application/xml, text/xml")
                    .GET()
                    .build();

            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw new CsdnSyncException("CSDN 返回 HTTP " + response.statusCode());
            }

            List<RssItem> parsedItems = parseRss(response.body());
            if (parsedItems.isEmpty()) {
                throw new CsdnSyncException("RSS 中没有可同步的文章");
            }

            // 同一链接只同步一次，并保持 RSS 原有顺序
            Map<String, RssItem> uniqueItems = new LinkedHashMap<>();
            for (RssItem item : parsedItems) {
                if (!item.link().isBlank()) uniqueItems.putIfAbsent(item.link(), item);
            }

            int created = 0;
            int updated = 0;
            for (RssItem item : uniqueItems.values()) {
                String sourceUrl = truncate(item.link(), 255);
                Article existing = articleMapper.selectOne(
                        new LambdaQueryWrapper<Article>().eq(Article::getSourceUrl, sourceUrl).last("LIMIT 1"));
                String sourceCover = resolveSourceCover(client, item, existing);
                Article synced = toArticle(item, sourceCover);

                if (existing == null) {
                    // 用户在后台明确选择后，文章才会出现在前台
                    synced.setVisible(0);
                    synced.setStatus("published");
                    synced.setIsRecommended(0);
                    synced.setIsHot(0);
                    synced.setViews(0);
                    articleMapper.insert(synced);
                    created++;
                } else {
                    // 保留用户维护的字段，只更新来源数据
                    synced.setId(existing.getId());
                    synced.setCover(existing.getCover());
                    synced.setVisible(existing.getVisible());
                    synced.setIsRecommended(existing.getIsRecommended());
                    synced.setIsHot(existing.getIsHot());
                    synced.setViews(existing.getViews());
                    synced.setStatus(existing.getStatus());
                    synced.setSummary(existing.getSummary() != null && !existing.getSummary().isBlank()
                            ? existing.getSummary() : synced.getSummary());
                    synced.setCategory(existing.getCategory() != null && !existing.getCategory().isBlank()
                            ? existing.getCategory() : synced.getCategory());
                    synced.setTags(existing.getTags() != null && !existing.getTags().isBlank()
                            ? existing.getTags() : synced.getTags());
                    synced.setContent(existing.getContent() != null && !existing.getContent().isBlank()
                            ? existing.getContent() : synced.getContent());
                    synced.setAuthor(existing.getAuthor() != null && !existing.getAuthor().isBlank()
                            ? existing.getAuthor() : synced.getAuthor());
                    if (existing.getCreateTime() != null) synced.setCreateTime(existing.getCreateTime());
                    articleMapper.updateById(synced);
                    updated++;
                }
            }

            return new SyncResult(uniqueItems.size(), created, updated);
        } catch (CsdnSyncException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CsdnSyncException("同步请求被中断", e);
        } catch (Exception e) {
            throw new CsdnSyncException("无法读取 CSDN RSS：" + e.getMessage(), e);
        }
    }

    List<RssItem> parseRss(String xml) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);

        InputSource source = new InputSource(new StringReader(xml));
        NodeList nodes = factory.newDocumentBuilder().parse(source).getElementsByTagName("item");
        List<RssItem> items = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Element item = (Element) nodes.item(i);
            String title = text(item, "title");
            String link = text(item, "link");
            if (title.isBlank() || link.isBlank()) continue;

            String author = text(item, "author");
            if (author.isBlank()) author = text(item, "dc:creator");
            items.add(new RssItem(
                    title.trim(),
                    link.trim(),
                    text(item, "description"),
                    text(item, "pubDate"),
                    author.trim()
            ));
        }
        return items;
    }

    private Article toArticle(RssItem item, String sourceCover) {
        String summary = cleanDescription(item.description());
        List<String> tags = extractTags(item.title() + " " + summary);

        Article article = new Article();
        article.setTitle(truncate(item.title(), 200));
        article.setSummary(truncate(summary.isBlank() ? "暂无摘要" : summary, 500));
        article.setCategory(categorize(item.title() + " " + summary));
        article.setSourceCover(truncate(sourceCover, 500));
        article.setSourceType("csdn");
        article.setSourceId(extractArticleId(item.link()));
        article.setTags(toJson(tags));
        article.setContent("## 文章简介\n\n" + (summary.isBlank() ? "暂无摘要" : summary)
                + "\n\n本文同步自 CSDN，完整内容请查看原文。");
        article.setAuthor(item.author().isBlank() ? defaultAuthor : truncate(item.author(), 50));
        article.setSourceUrl(truncate(item.link(), 255));
        article.setReadTime(Math.max(3, (summary.length() + 199) / 200) + "分钟");

        LocalDateTime publishedAt = parsePublishedAt(item.pubDate());
        if (publishedAt != null) article.setCreateTime(publishedAt);
        return article;
    }

    /**
     * 优先使用 RSS 正文片段里的图片；RSS 没有图片时读取文章页正文首图。
     * 单篇封面提取失败不影响整批文章同步。
     */
    private String resolveSourceCover(HttpClient client, RssItem item, Article existing) {
        String fromRss = extractFirstContentImage(item.description());
        if (!fromRss.isBlank()) return fromRss;
        if (existing != null && existing.getSourceCover() != null && !existing.getSourceCover().isBlank()) {
            return existing.getSourceCover();
        }

        try {
            Duration pageTimeout = Duration.ofSeconds(Math.max(5, Math.min(timeoutSeconds, 12)));
            HttpRequest request = HttpRequest.newBuilder(URI.create(item.link()))
                    .timeout(pageTimeout)
                    .header("User-Agent", "Mozilla/5.0 (compatible; OrangePortfolio/1.0)")
                    .header("Accept", "text/html,application/xhtml+xml")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) return "";

            String html = response.body();
            Matcher contentStart = CONTENT_START.matcher(html);
            if (contentStart.find()) {
                int end = Math.min(html.length(), contentStart.end() + 300_000);
                html = html.substring(contentStart.end(), end);
            }
            return extractFirstContentImage(html);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return "";
        } catch (Exception ignored) {
            return "";
        }
    }

    String extractFirstContentImage(String html) {
        if (html == null || html.isBlank()) return "";
        Matcher tags = IMAGE_TAG.matcher(html);
        while (tags.find()) {
            Matcher source = IMAGE_SOURCE.matcher(tags.group());
            if (!source.find()) continue;

            String url = HtmlUtils.htmlUnescape(source.group(2)).trim();
            if (url.startsWith("//")) url = "https:" + url;
            if (!url.startsWith("https://") || isCsdnUiImage(url)) continue;
            return url;
        }
        return "";
    }

    private boolean isCsdnUiImage(String url) {
        String value = url.toLowerCase(Locale.ROOT);
        return value.contains("csdnimg.cn/release/")
                || value.contains("i-avatar.csdnimg.cn/")
                || value.contains("/columns/default/")
                || value.contains("/toolbar/")
                || value.contains("favicon")
                || value.contains("vip-limited")
                || value.contains("qrcode");
    }

    private Long extractArticleId(String link) {
        if (link == null) return null;
        Matcher matcher = ARTICLE_ID.matcher(link);
        if (!matcher.find()) return null;
        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String cleanDescription(String html) {
        String withoutTags = HTML_TAG.matcher(html == null ? "" : html).replaceAll(" ");
        String unescaped = HtmlUtils.htmlUnescape(withoutTags).replace('\u00A0', ' ');
        return WHITESPACE.matcher(unescaped).replaceAll(" ").trim();
    }

    private String categorize(String source) {
        String text = source.toLowerCase(Locale.ROOT);
        for (CategoryRule rule : CATEGORY_RULES) {
            if (rule.keywords().stream().anyMatch(keyword -> containsKeyword(text, keyword))) {
                return rule.category();
            }
        }
        return "growth";
    }

    private List<String> extractTags(String source) {
        String text = source.toLowerCase(Locale.ROOT);
        List<String> tags = new ArrayList<>();
        for (CategoryRule rule : CATEGORY_RULES) {
            for (String keyword : rule.keywords()) {
                if (containsKeyword(text, keyword)
                        && tags.stream().noneMatch(keyword::equalsIgnoreCase)) {
                    tags.add(keyword);
                    if (tags.size() == 6) return tags;
                }
            }
        }
        return tags.isEmpty() ? List.of("CSDN") : tags;
    }

    private boolean containsKeyword(String source, String keyword) {
        String normalized = keyword.toLowerCase(Locale.ROOT);
        if (normalized.matches("[a-z0-9]+") && normalized.length() <= 3) {
            return Pattern.compile("(?<![a-z0-9])" + Pattern.quote(normalized) + "(?![a-z0-9])")
                    .matcher(source)
                    .find();
        }
        return source.contains(normalized);
    }

    private LocalDateTime parsePublishedAt(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return ZonedDateTime.parse(value.trim(), DateTimeFormatter.RFC_1123_DATE_TIME)
                    .withZoneSameInstant(SITE_ZONE)
                    .toLocalDateTime();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String toJson(List<String> value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new CsdnSyncException("文章标签序列化失败", e);
        }
    }

    private String text(Element parent, String tagName) {
        NodeList nodes = parent.getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? "" : nodes.item(0).getTextContent();
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private record CategoryRule(String category, List<String> keywords) {}

    record RssItem(String title, String link, String description, String pubDate, String author) {}

    public record SyncResult(int total, int created, int updated) {}

    public static class CsdnSyncException extends RuntimeException {
        public CsdnSyncException(String message) {
            super(message);
        }

        public CsdnSyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
