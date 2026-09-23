package com.yihong.growth.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yihong.growth.entity.Project;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GithubProjectSyncServiceTests {

    private final GithubProjectSyncService service =
            new GithubProjectSyncService(null, new ObjectMapper());

    @Test
    void parsesRepositoryAndBuildsSourceMetadata() throws Exception {
        String json = """
                [{
                  "id": 987654321,
                  "name": "HarmonyPortfolio",
                  "html_url": "https://github.com/CoderYiHong/HarmonyPortfolio",
                  "description": "HarmonyOS portfolio app",
                  "homepage": "https://example.com",
                  "language": "ArkTS",
                  "topics": ["harmonyos", "arkui"],
                  "archived": false,
                  "fork": false,
                  "owner": {"login": "CoderYiHong"},
                  "created_at": "2025-01-02T03:04:05Z",
                  "updated_at": "2026-07-26T12:30:00Z"
                }]
                """;

        List<GithubProjectSyncService.GithubRepository> repositories =
                service.parseRepositories(json);
        Project project = service.toProject(repositories.get(0));

        assertEquals(1, repositories.size());
        assertEquals("github", project.getSourceType());
        assertEquals(987654321L, project.getSourceId());
        assertEquals("mobile", project.getCategory());
        assertTrue(project.getTechStack().contains("ArkTS"));
        assertTrue(project.getSourceCover().startsWith("https://opengraph.githubassets.com/"));
        assertNull(project.getCover(), "同步对象不能写入自定义封面字段");
        assertNull(project.getVisible(), "同步对象不能覆盖后台显隐选择");
    }
}
