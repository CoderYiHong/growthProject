package com.yihong.growth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yihong.growth.entity.Project;
import com.yihong.growth.mapper.ProjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * 将指定 GitHub 用户拥有的公开仓库同步到项目表。
 * 新仓库默认隐藏；再次同步只更新来源元数据，不覆盖自定义封面、显隐、推荐与排序。
 */
@Service
@RequiredArgsConstructor
public class GithubProjectSyncService {

    private static final ZoneId SITE_ZONE = ZoneId.of("Asia/Shanghai");
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z0-9](?:[A-Za-z0-9-]{0,37}[A-Za-z0-9])?$");

    private final ProjectMapper projectMapper;
    private final ObjectMapper objectMapper;

    @Value("${github.username:CoderYiHong}")
    private String username;

    @Value("${github.token:}")
    private String token;

    @Value("${github.include-forks:false}")
    private boolean includeForks;

    @Value("${github.sync-timeout-seconds:20}")
    private int timeoutSeconds;

    public synchronized SyncResult sync() {
        if (username == null || !USERNAME_PATTERN.matcher(username.trim()).matches()) {
            throw new GithubSyncException("GitHub 用户名配置不正确");
        }

        try {
            Duration timeout = Duration.ofSeconds(Math.max(5, timeoutSeconds));
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(timeout)
                    .followRedirects(HttpClient.Redirect.NORMAL)
                    .build();
            URI uri = URI.create("https://api.github.com/users/" + username.trim()
                    + "/repos?type=owner&sort=updated&direction=desc&per_page=100");

            HttpRequest.Builder request = HttpRequest.newBuilder(uri)
                    .timeout(timeout)
                    .header("Accept", "application/vnd.github+json")
                    .header("X-GitHub-Api-Version", "2022-11-28")
                    .header("User-Agent", "OrangePortfolio/1.0");
            if (token != null && !token.isBlank()) {
                request.header("Authorization", "Bearer " + token.trim());
            }

            HttpResponse<String> response = client.send(
                    request.GET().build(),
                    HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (response.statusCode() != 200) {
                throw responseException(response.statusCode(), response.body());
            }

            List<GithubRepository> repositories = parseRepositories(response.body());
            int created = 0;
            int updated = 0;
            int skipped = 0;
            int sourceOrder = 1;

            for (GithubRepository repository : repositories) {
                if (repository.fork() && !includeForks) {
                    skipped++;
                    continue;
                }

                Project existing = findExisting(repository);
                Project synced = toProject(repository);
                if (existing == null) {
                    synced.setVisible(0);
                    synced.setIsRecommended(0);
                    synced.setSortOrder(sourceOrder);
                    synced.setRole("个人项目");
                    synced.setDescription(synced.getSummary());
                    projectMapper.insert(synced);
                    created++;
                } else {
                    // 保护用户编辑的字段，只更新来源数据
                    synced.setId(existing.getId());
                    synced.setCover(existing.getCover());
                    synced.setVisible(existing.getVisible());
                    synced.setIsRecommended(existing.getIsRecommended());
                    synced.setSortOrder(existing.getSortOrder());
                    synced.setSummary(existing.getSummary() != null && !existing.getSummary().isBlank()
                            ? existing.getSummary() : synced.getSummary());
                    synced.setCategory(existing.getCategory() != null && !existing.getCategory().isBlank()
                            ? existing.getCategory() : synced.getCategory());
                    synced.setStatus(existing.getStatus() != null && !existing.getStatus().isBlank()
                            ? existing.getStatus() : synced.getStatus());
                    synced.setTechStack(existing.getTechStack() != null && !existing.getTechStack().isBlank()
                            ? existing.getTechStack() : synced.getTechStack());
                    synced.setDemoUrl(existing.getDemoUrl() != null && !existing.getDemoUrl().isBlank()
                            ? existing.getDemoUrl() : synced.getDemoUrl());
                    synced.setDescription(existing.getDescription() != null && !existing.getDescription().isBlank()
                            ? existing.getDescription() : synced.getDescription());
                    synced.setRole(existing.getRole() != null && !existing.getRole().isBlank()
                            ? existing.getRole() : synced.getRole());
                    // 不覆盖用户设置的 createTime
                    if (existing.getCreateTime() != null) synced.setCreateTime(existing.getCreateTime());
                    projectMapper.updateById(synced);
                    updated++;
                }
                sourceOrder++;
            }

            return new SyncResult(repositories.size(), created, updated, skipped);
        } catch (GithubSyncException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new GithubSyncException("GitHub 同步请求被中断", e);
        } catch (Exception e) {
            throw new GithubSyncException("无法读取 GitHub 仓库：" + e.getMessage(), e);
        }
    }

    List<GithubRepository> parseRepositories(String json) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(json);
        if (!root.isArray()) {
            throw new GithubSyncException("GitHub 返回的数据格式不正确");
        }

        List<GithubRepository> repositories = new ArrayList<>();
        for (JsonNode node : root) {
            long id = node.path("id").asLong(0);
            String name = node.path("name").asText("").trim();
            String htmlUrl = node.path("html_url").asText("").trim();
            if (id <= 0 || name.isBlank() || !htmlUrl.startsWith("https://github.com/")) {
                continue;
            }

            List<String> topics = new ArrayList<>();
            if (node.path("topics").isArray()) {
                node.path("topics").forEach(topic -> {
                    String value = topic.asText("").trim();
                    if (!value.isBlank()) topics.add(value);
                });
            }

            repositories.add(new GithubRepository(
                    id,
                    name,
                    htmlUrl,
                    node.path("description").asText(""),
                    node.path("homepage").asText(""),
                    node.path("language").asText(""),
                    topics,
                    node.path("archived").asBoolean(false),
                    node.path("fork").asBoolean(false),
                    node.path("owner").path("login").asText(username),
                    parseTimestamp(node.path("created_at").asText("")),
                    parseTimestamp(node.path("updated_at").asText(""))
            ));
        }
        return repositories;
    }

    Project toProject(GithubRepository repository) {
        List<String> stack = buildTechStack(repository);
        String summary = repository.description() == null || repository.description().isBlank()
                ? "GitHub 项目 " + repository.name()
                : repository.description().trim();

        Project project = new Project();
        project.setName(truncate(repository.name(), 100));
        project.setSummary(truncate(summary, 500));
        project.setCategory(categorize(repository, stack));
        project.setTechStack(toJson(stack));
        project.setStatus(repository.archived() ? "completed" : "in_progress");
        project.setDemoUrl(truncate(normalizeUrl(repository.homepage()), 255));
        project.setSourceUrl(truncate(repository.htmlUrl(), 255));
        project.setSourceType("github");
        project.setSourceId(repository.id());
        project.setSourceCover(githubOpenGraphImage(repository));
        if (repository.createdAt() != null) project.setCreateTime(repository.createdAt());
        return project;
    }

    private Project findExisting(GithubRepository repository) {
        Project byId = projectMapper.selectOne(new LambdaQueryWrapper<Project>()
                .eq(Project::getSourceId, repository.id())
                .last("LIMIT 1"));
        if (byId != null) return byId;
        return projectMapper.selectOne(new LambdaQueryWrapper<Project>()
                .eq(Project::getSourceUrl, truncate(repository.htmlUrl(), 255))
                .last("LIMIT 1"));
    }

    private List<String> buildTechStack(GithubRepository repository) {
        Set<String> values = new LinkedHashSet<>();
        if (repository.language() != null && !repository.language().isBlank()) {
            values.add(repository.language().trim());
        }
        for (String topic : repository.topics()) {
            if (values.size() >= 8) break;
            values.add(topic);
        }
        return new ArrayList<>(values);
    }

    private String categorize(GithubRepository repository, List<String> stack) {
        String text = (repository.name() + " " + repository.description() + " "
                + repository.language() + " " + String.join(" ", stack)).toLowerCase(Locale.ROOT);

        if (containsAny(text, "ai", "llm", "rag", "machine-learning", "deep-learning", "人工智能")) return "ai";
        if (containsAny(text, "harmonyos", "arkts", "arkui", "android", "kotlin", "swift", "mobile")) return "mobile";

        boolean frontend = containsAny(text, "vue", "react", "svelte", "angular", "frontend", "typescript", "javascript", "css");
        boolean backend = containsAny(text, "spring", "java", "node", "express", "nestjs", "django", "flask", "backend", "mysql", "redis");
        if (frontend && backend) return "fullstack";
        if (frontend) return "frontend";
        if (backend) return "backend";
        return "enterprise";
    }

    private boolean containsAny(String source, String... keywords) {
        for (String keyword : keywords) {
            if (source.contains(keyword)) return true;
        }
        return false;
    }

    private String githubOpenGraphImage(GithubRepository repository) {
        long cacheKey = repository.updatedAt() == null
                ? repository.id()
                : repository.updatedAt().atZone(SITE_ZONE).toEpochSecond();
        return "https://opengraph.githubassets.com/" + cacheKey + "/"
                + encodePathSegment(repository.owner()) + "/" + encodePathSegment(repository.name());
    }

    private String encodePathSegment(String value) {
        return URLEncoder.encode(value == null ? "" : value, StandardCharsets.UTF_8)
                .replace("+", "%20");
    }

    private LocalDateTime parseTimestamp(String value) {
        if (value == null || value.isBlank()) return null;
        try {
            return LocalDateTime.ofInstant(Instant.parse(value), SITE_ZONE);
        } catch (DateTimeParseException ignored) {
            return null;
        }
    }

    private GithubSyncException responseException(int status, String body) {
        String upstreamMessage = "";
        try {
            upstreamMessage = objectMapper.readTree(body).path("message").asText("");
        } catch (Exception ignored) {
            // 使用下面的通用错误
        }
        if (status == 403 || status == 429) {
            return new GithubSyncException("GitHub API 请求额度已用完，请配置 GITHUB_TOKEN 后重试");
        }
        if (status == 404) {
            return new GithubSyncException("找不到 GitHub 用户 " + username);
        }
        String suffix = upstreamMessage.isBlank() ? "" : "：" + upstreamMessage;
        return new GithubSyncException("GitHub 返回 HTTP " + status + suffix);
    }

    private String normalizeUrl(String value) {
        if (value == null || value.isBlank()) return "";
        String url = value.trim();
        return url.startsWith("https://") || url.startsWith("http://") ? url : "";
    }

    private String toJson(List<String> values) {
        try {
            return objectMapper.writeValueAsString(values);
        } catch (JsonProcessingException e) {
            throw new GithubSyncException("项目技术栈序列化失败", e);
        }
    }

    private String truncate(String value, int maxLength) {
        if (value == null) return "";
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    record GithubRepository(
            long id,
            String name,
            String htmlUrl,
            String description,
            String homepage,
            String language,
            List<String> topics,
            boolean archived,
            boolean fork,
            String owner,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record SyncResult(int total, int created, int updated, int skipped) {}

    public static class GithubSyncException extends RuntimeException {
        public GithubSyncException(String message) {
            super(message);
        }

        public GithubSyncException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
