-- MySQL 初始数据（Spring Boot 启动时自动执行）
-- INSERT IGNORE 避免重复插入

-- 文章与项目只从 CSDN / GitHub 同步，不再插入演示数据。

-- 成长阶段
INSERT IGNORE INTO growth_stage (id, period, title, subtitle, icon, description, skills, sort_order) VALUES
(1, '2022-2023', '探索阶段', '编程入门与基础构建', 'seed', '开始接触编程世界，从 C 语言入门', 'C,Java,MySQL', 1),
(2, '2023-2024', '实践阶段', '深入 HarmonyOS 与项目实践', 'sprout', '深入学习 HarmonyOS 应用开发', 'HarmonyOS,ArkTS,ArkUI', 2),
(3, '2024-2025', '企业项目阶段', '参与企业级开发实践', 'tree', '参与企业实际项目开发', 'Spring Boot,Vue3', 3),
(4, '2025-至今', '转型与成长', '全栈开发与 AI 探索', 'bigTree', '全面转向全栈开发方向', 'Vue3,Spring Boot,Python', 4);

-- 技能
INSERT IGNORE INTO skill (id, category, name, level, percentage, sort_order) VALUES
(1, '前端开发', 'Vue3', '掌握', 78, 1),
(2, '前端开发', 'JavaScript', '掌握', 80, 2),
(3, '后端开发', 'Java', '掌握', 75, 3),
(4, '后端开发', 'Spring Boot', '掌握', 70, 4),
(5, '移动开发', 'HarmonyOS', '掌握', 80, 5),
(6, '数据与 AI', 'Python', '熟悉', 55, 6);

-- 证书资质
INSERT IGNORE INTO certificate (id, name, category, issuer, issue_date, description, visible, featured, sort_order) VALUES
(1, 'HCIP-HarmonyOS Application Developer', '厂商认证', '华为技术有限公司', '2025-09', '华为认证 HarmonyOS 应用开发者高级工程师', 1, 0, 1),
(2, '大学英语四级 (CET-4)', '语言能力', '教育部教育考试院', '2024-06', '全国大学英语四级考试', 1, 0, 2),
(3, '普通话等级证书 (二级甲等)', '语言能力', '国家语言文字工作委员会', '2024-03', '普通话水平测试二级甲等', 1, 0, 3);
