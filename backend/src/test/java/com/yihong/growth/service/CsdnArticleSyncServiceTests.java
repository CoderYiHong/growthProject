package com.yihong.growth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CsdnArticleSyncServiceTests {

    private final CsdnArticleSyncService service =
            new CsdnArticleSyncService(null, new ObjectMapper());

    @Test
    void parsesCsdnRssItemsAndCdata() throws Exception {
        String xml = """
                <?xml version="1.0" encoding="UTF-8"?>
                <rss version="2.0">
                  <channel>
                    <item>
                      <title><![CDATA[Vue3 项目实践]]></title>
                      <link>https://blog.csdn.net/test/article/details/1</link>
                      <description><![CDATA[<p>文章摘要</p>]]></description>
                      <pubDate>Sun, 26 Jul 2026 10:00:00 +0800</pubDate>
                      <author><![CDATA[YiHong]]></author>
                    </item>
                  </channel>
                </rss>
                """;

        List<CsdnArticleSyncService.RssItem> items = service.parseRss(xml);

        assertEquals(1, items.size());
        assertEquals("Vue3 项目实践", items.get(0).title());
        assertEquals("https://blog.csdn.net/test/article/details/1", items.get(0).link());
        assertEquals("<p>文章摘要</p>", items.get(0).description());
        assertEquals("YiHong", items.get(0).author());
    }

    @Test
    void rejectsDoctypeToPreventExternalEntityExpansion() {
        String xml = """
                <?xml version="1.0"?>
                <!DOCTYPE rss [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <rss><channel><item><title>&xxe;</title><link>https://example.com</link></item></channel></rss>
                """;

        assertThrows(Exception.class, () -> service.parseRss(xml));
    }

    @Test
    void extractsContentImageAndIgnoresCsdnInterfaceImages() {
        String html = """
                <img src="https://csdnimg.cn/release/blogv2/dist/pc/img/toolbar/like.png">
                <p>正文</p>
                <img data-src="https://i-blog.csdnimg.cn/direct/example-cover.png" alt="文章配图">
                """;

        assertEquals(
                "https://i-blog.csdnimg.cn/direct/example-cover.png",
                service.extractFirstContentImage(html));
    }
}
