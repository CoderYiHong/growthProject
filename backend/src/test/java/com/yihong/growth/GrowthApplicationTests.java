package com.yihong.growth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GrowthApplicationTests {

    @Autowired private MockMvc mvc;
    @Autowired private ObjectMapper mapper;

    private String token;

    @Test @Order(1)
    @DisplayName("正确登录 → 200 + token")
    void loginSuccess() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "admin123"));
        String resp = mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.token").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        token = mapper.readTree(resp).get("data").get("token").asText();
    }

    @Test @Order(2)
    @DisplayName("错误密码 → 401")
    void loginWrongPassword() throws Exception {
        String body = mapper.writeValueAsString(Map.of("username", "admin", "password", "wrongpassword"));
        // 使用独立 IP，避开登录接口 60s 限流窗口（Order 1 已用默认 IP 登录成功）
        mvc.perform(post("/api/auth/login")
                        .with(r -> { r.setRemoteAddr("10.99.99.1"); return r; })
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test @Order(3)
    @DisplayName("无 Token → 401")
    void noToken() throws Exception {
        mvc.perform(get("/api/admin/dashboard/stats"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(401));
    }

    @Test @Order(4)
    @DisplayName("Project 空名称 → 400")
    void projectEmptyName() throws Exception {
        String body = mapper.writeValueAsString(Map.of("name", "", "category", "test"));
        mvc.perform(post("/api/admin/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test @Order(5)
    @DisplayName("Project 开始日期晚于结束日期 → 400")
    void projectInvalidDate() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
            "name", "Test", "category", "test",
            "startDate", "2026-12-31", "endDate", "2026-01-01"));
        mvc.perform(post("/api/admin/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test @Order(6)
    @DisplayName("Project 非法 URL → 400")
    void projectBadUrl() throws Exception {
        String body = mapper.writeValueAsString(Map.of("name", "Test", "category", "test", "demoUrl", "not-a-url"));
        mvc.perform(post("/api/admin/projects")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test @Order(7)
    @DisplayName("资源不存在 → 404")
    void notFound() throws Exception {
        mvc.perform(get("/api/public/articles/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test @Order(8)
    @DisplayName("setting_key 连续保存不重复")
    void settingUnique() throws Exception {
        String v1 = mapper.writeValueAsString(Map.of("test_unique_key", "v1"));
        mvc.perform(put("/api/admin/settings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(v1))
                .andExpect(status().isOk());
        String v2 = mapper.writeValueAsString(Map.of("test_unique_key", "v2"));
        mvc.perform(put("/api/admin/settings")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(v2))
                .andExpect(status().isOk());
        // 第二次应更新而非插入，GET 验证只有一条
        mvc.perform(get("/api/admin/settings").header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.test_unique_key").value("v2"));
    }

    @Test @Order(9)
    @DisplayName("Article Boolean 推荐状态保存与读取")
    void articleRecommendToggle() throws Exception {
        // 创建测试文章
        String create = mapper.writeValueAsString(Map.of("title", "Toggle Test Article"));
        mvc.perform(post("/api/admin/articles")
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isOk());

        // H2 AUTO_INCREMENT 从 1 开始（空库）
        long id = 1;

        // 推荐 ON
        String on = mapper.writeValueAsString(Map.of("isRecommended", true));
        mvc.perform(put("/api/admin/articles/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(on))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/articles/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.isRecommended").value(1));

        // 推荐 OFF
        String off = mapper.writeValueAsString(Map.of("isRecommended", false));
        mvc.perform(put("/api/admin/articles/" + id)
                .header("Authorization", "Bearer " + token)
                .contentType(MediaType.APPLICATION_JSON).content(off))
                .andExpect(status().isOk());
        mvc.perform(get("/api/admin/articles/" + id).header("Authorization", "Bearer " + token))
                .andExpect(jsonPath("$.data.isRecommended").value(0));
    }

    @Test @Order(10)
    @DisplayName("联系表单限流 → 429")
    void contactRateLimit() throws Exception {
        String body = mapper.writeValueAsString(Map.of(
            "name", "Test", "email", "a@b.com", "subject", "Hi",
            "content", "This is a test message long enough for validation"));
        // 使用独立 IP，避免与登录接口共用限流窗口（RateLimitService 按 IP 记 60s 窗口）
        // 第一次成功
        mvc.perform(post("/api/contact")
                        .with(r -> { r.setRemoteAddr("10.99.99.2"); return r; })
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("留言已保存，邮件通知暂未启用。"))
                .andExpect(jsonPath("$.data.emailNotified").value(false));
        // 第二次被限流（同一 IP，60s 内）
        mvc.perform(post("/api/contact")
                        .with(r -> { r.setRemoteAddr("10.99.99.2"); return r; })
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().is(429))
                .andExpect(jsonPath("$.code").value(429));
    }

    @Test @Order(11)
    @DisplayName("Certificate 局部更新公开状态")
    void certificateVisibilityToggle() throws Exception {
        String create = mapper.writeValueAsString(Map.of(
                "name", "Test Certificate",
                "imageUrl", "/uploads/2026/07/test.jpg",
                "credentialNo", "PRIVATE-001",
                "visible", true));
        mvc.perform(post("/api/admin/certificates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isOk());

        String adminList = mvc.perform(get("/api/admin/certificates")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long certificateId = -1;
        for (JsonNode item : mapper.readTree(adminList).get("data")) {
            if ("Test Certificate".equals(item.get("name").asText())) {
                certificateId = item.get("id").asLong();
                break;
            }
        }
        Assertions.assertTrue(certificateId > 0, "新建证书应能在管理列表中找到");

        String hide = mapper.writeValueAsString(Map.of("visible", false));
        mvc.perform(put("/api/admin/certificates/" + certificateId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(hide))
                .andExpect(status().isOk());
        mvc.perform(get("/api/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.name == 'Test Certificate')]").isEmpty());

        String show = mapper.writeValueAsString(Map.of("visible", true));
        mvc.perform(put("/api/admin/certificates/" + certificateId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(show))
                .andExpect(status().isOk());
        mvc.perform(get("/api/certificates"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].imageUrl").value("/uploads/2026/07/test.jpg"))
                .andExpect(jsonPath("$.data[0].credentialNo").doesNotExist());
    }

    @Test @Order(12)
    @DisplayName("Certificate 新增时名称必填")
    void certificateCreateRequiresName() throws Exception {
        String body = mapper.writeValueAsString(Map.of("visible", true));
        mvc.perform(post("/api/admin/certificates")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test @Order(13)
    @DisplayName("CSDN 文章可由后台选择是否在前台展示")
    void articleVisibilityToggle() throws Exception {
        String title = "CSDN Visibility Test";
        String create = mapper.writeValueAsString(Map.of(
                "title", title,
                "status", "published",
                "visible", 0,
                "sourceUrl", "https://blog.csdn.net/test/article/details/visibility"));
        mvc.perform(post("/api/admin/articles")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isOk());

        String adminList = mvc.perform(get("/api/admin/articles?page=1&pageSize=100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long articleId = -1;
        for (JsonNode item : mapper.readTree(adminList).get("data").get("records")) {
            if (title.equals(item.get("title").asText())) {
                articleId = item.get("id").asLong();
                break;
            }
        }
        Assertions.assertTrue(articleId > 0, "同步文章应能在后台列表中找到");

        mvc.perform(get("/api/public/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.title == '" + title + "')]").isEmpty());

        String show = mapper.writeValueAsString(Map.of("visible", 1));
        mvc.perform(put("/api/admin/articles/" + articleId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(show))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mvc.perform(get("/api/public/articles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.title == '" + title + "')]").isNotEmpty());
    }

    @Test @Order(14)
    @DisplayName("GitHub 项目可由后台选择是否在前台展示")
    void projectVisibilityToggle() throws Exception {
        String name = "GitHub Visibility Test";
        String create = mapper.writeValueAsString(Map.of(
                "name", name,
                "category", "backend",
                "visible", 0,
                "sourceUrl", "https://github.com/CoderYiHong/visibility-test"));
        mvc.perform(post("/api/admin/projects")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isOk());

        String adminList = mvc.perform(get("/api/admin/projects?page=1&pageSize=100")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        long projectId = -1;
        for (JsonNode item : mapper.readTree(adminList).get("data").get("records")) {
            if (name.equals(item.get("name").asText())) {
                projectId = item.get("id").asLong();
                break;
            }
        }
        Assertions.assertTrue(projectId > 0, "GitHub 项目应能在后台列表中找到");

        mvc.perform(get("/api/public/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.name == '" + name + "')]").isEmpty());

        String show = mapper.writeValueAsString(Map.of("visible", 1));
        mvc.perform(put("/api/admin/projects/" + projectId)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(show))
                .andExpect(status().isOk());

        mvc.perform(get("/api/public/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[?(@.name == '" + name + "')]").isNotEmpty());
    }

    @Test @Order(15)
    @DisplayName("课表 课程/排课 CRUD 与校验")
    void timetableCrud() throws Exception {
        // 空课程名称 → 400
        String empty = mapper.writeValueAsString(Map.of("name", ""));
        mvc.perform(post("/api/admin/timetable/courses")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(empty))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        // 新增课程
        String create = mapper.writeValueAsString(Map.of(
                "name", "操作系统", "teacher", "任大娟", "credit", 4.0, "color", 2));
        mvc.perform(post("/api/admin/timetable/courses")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(create))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        String agg = mvc.perform(get("/api/admin/timetable")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long courseId = -1;
        for (JsonNode c : mapper.readTree(agg).get("data").get("courses")) {
            if ("操作系统".equals(c.get("name").asText())) { courseId = c.get("id").asLong(); break; }
        }
        Assertions.assertTrue(courseId > 0, "新增课程应出现在聚合列表中");

        // 不存在的课程 → 400
        String badEntry = mapper.writeValueAsString(Map.of(
                "courseId", 999999, "dayOfWeek", 2, "slot", 0, "startWeek", 1, "endWeek", 16, "room", "x"));
        mvc.perform(post("/api/admin/timetable/entries")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(badEntry))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        // 结束周早于起始周 → 400
        String badWeeks = mapper.writeValueAsString(Map.of(
                "courseId", courseId, "dayOfWeek", 2, "slot", 0, "startWeek", 10, "endWeek", 2, "room", "x"));
        mvc.perform(post("/api/admin/timetable/entries")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(badWeeks))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));

        // 有效排课
        String entry = mapper.writeValueAsString(Map.of(
                "courseId", courseId, "dayOfWeek", 2, "slot", 0, "startWeek", 1, "endWeek", 16, "room", "2号综合楼704"));
        mvc.perform(post("/api/admin/timetable/entries")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(entry))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        // 公开聚合：H2 无设置 → totalWeeks=0，排课可见
        mvc.perform(get("/api/public/timetable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.semester.totalWeeks").value(0))
                .andExpect(jsonPath("$.data.semester.startDate").value(""))
                .andExpect(jsonPath("$.data.entries.length()").value(1))
                .andExpect(jsonPath("$.data.entries[0].room").value("2号综合楼704"));
    }

    @Test @Order(16)
    @DisplayName("课表 导入全量替换 + 删除课程级联")
    void timetableImportReplace() throws Exception {
        // 第一次导入 2 门课 3 条排课
        String first = mapper.writeValueAsString(Map.of(
                "courses", List.of(
                        Map.of("name", "课程A", "teacher", "张三", "credit", 4.0, "color", 1),
                        Map.of("name", "课程B", "teacher", "李四", "credit", 2.0, "color", 2)),
                "entries", List.of(
                        Map.of("courseIndex", 0, "dayOfWeek", 1, "slot", 0, "startWeek", 1, "endWeek", 8, "room", "A101"),
                        Map.of("courseIndex", 0, "dayOfWeek", 2, "slot", 1, "startWeek", 1, "endWeek", 8, "room", "A102"),
                        Map.of("courseIndex", 1, "dayOfWeek", 3, "slot", 2, "startWeek", 9, "endWeek", 16, "room", "B201"))));
        mvc.perform(post("/api/admin/timetable/import")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(first))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses").value(2))
                .andExpect(jsonPath("$.data.entries").value(3));

        mvc.perform(get("/api/public/timetable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses.length()").value(2))
                .andExpect(jsonPath("$.data.entries.length()").value(3));

        // 第二次导入 1 门课 0 排课 → 全量替换
        String second = mapper.writeValueAsString(Map.of(
                "courses", List.of(Map.of("name", "课程C", "teacher", "王五", "credit", 1.0, "color", 3)),
                "entries", List.of()));
        mvc.perform(post("/api/admin/timetable/import")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(second))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses").value(1))
                .andExpect(jsonPath("$.data.entries").value(0));

        mvc.perform(get("/api/public/timetable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses.length()").value(1))
                .andExpect(jsonPath("$.data.courses[0].name").value("课程C"))
                .andExpect(jsonPath("$.data.entries.length()").value(0));

        // 越界 courseIndex → 400 且回滚（旧数据保持不变）
        String bad = mapper.writeValueAsString(Map.of(
                "courses", List.of(Map.of("name", "课程D", "teacher", "赵六", "color", 1)),
                "entries", List.of(Map.of("courseIndex", 5, "dayOfWeek", 1, "slot", 0, "startWeek", 1, "endWeek", 2, "room", "D"))));
        mvc.perform(post("/api/admin/timetable/import")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(bad))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
        mvc.perform(get("/api/public/timetable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses[0].name").value("课程C"));

        // 删除课程级联删除排课
        String agg = mvc.perform(get("/api/admin/timetable")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
        long courseId = mapper.readTree(agg).get("data").get("courses").get(0).get("id").asLong();

        String entry = mapper.writeValueAsString(Map.of(
                "courseId", courseId, "dayOfWeek", 4, "slot", 3, "startWeek", 1, "endWeek", 2, "room", "C"));
        mvc.perform(post("/api/admin/timetable/entries")
                        .characterEncoding(StandardCharsets.UTF_8)
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(entry))
                .andExpect(status().isOk());

        mvc.perform(delete("/api/admin/timetable/courses/" + courseId)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));

        mvc.perform(get("/api/public/timetable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courses.length()").value(0))
                .andExpect(jsonPath("$.data.entries.length()").value(0));
    }
}
