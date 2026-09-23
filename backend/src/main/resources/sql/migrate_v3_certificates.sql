-- ============================================================
-- 迁移 v3 — 证书资质表
-- 执行：mysql -u <user> -p growth < migrate_v3_certificates.sql
-- 幂等：可重复执行
-- ============================================================

CREATE TABLE IF NOT EXISTS certificate (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    name            VARCHAR(100) NOT NULL,
    category        VARCHAR(30)  DEFAULT '厂商认证',
    issuer          VARCHAR(100) DEFAULT '',
    issue_date      VARCHAR(30)  DEFAULT '',
    description     TEXT,
    credential_no   VARCHAR(100) DEFAULT '',
    verification_url VARCHAR(255) DEFAULT '',
    image_url       VARCHAR(255) DEFAULT '',
    visible         INT          DEFAULT 1,
    featured        INT          DEFAULT 0,
    sort_order      INT          DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 迁移现有数据（幂等）
INSERT IGNORE INTO certificate (id, name, category, issuer, issue_date, description, visible, featured, sort_order) VALUES
(1, 'HCIP-HarmonyOS Application Developer', '厂商认证', '华为技术有限公司', '2025-09', '华为认证 HarmonyOS 应用开发者高级工程师，具备 HarmonyOS 应用设计与开发能力', 1, 0, 1),
(2, '大学英语四级 (CET-4)', '语言能力', '教育部教育考试院', '2024-06', '全国大学英语四级考试，具备基本英语听说读写能力', 1, 0, 2),
(3, '普通话等级证书 (二级甲等)', '语言能力', '国家语言文字工作委员会', '2024-03', '普通话水平测试二级甲等，具备规范汉字书写和普通话表达能力', 1, 0, 3);
