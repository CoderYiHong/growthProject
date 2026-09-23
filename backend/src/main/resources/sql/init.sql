-- ============================================================
-- 数据库初始化脚本（MySQL）
-- 首次部署时执行此脚本创建表结构
-- ============================================================

CREATE DATABASE IF NOT EXISTS growth DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE growth;

-- 项目表
CREATE TABLE IF NOT EXISTS project (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL COMMENT '项目名称',
    category    VARCHAR(30)   NOT NULL COMMENT '分类：mobile/fullstack/frontend/backend/ai/enterprise',
    cover       VARCHAR(255)  DEFAULT '' COMMENT '后台自定义封面URL',
    source_cover VARCHAR(500) DEFAULT '' COMMENT 'GitHub 默认封面URL',
    source_type VARCHAR(20)   DEFAULT '' COMMENT '来源类型：github',
    source_id   BIGINT        DEFAULT NULL COMMENT 'GitHub repository id',
    summary     VARCHAR(500)  DEFAULT '' COMMENT '项目简介',
    description TEXT          COMMENT '详细描述',
    tech_stack  VARCHAR(500)  DEFAULT '' COMMENT '技术栈，JSON数组',
    status      VARCHAR(20)   DEFAULT 'in_progress' COMMENT '状态：in_progress/completed',
    is_recommended TINYINT    DEFAULT 0 COMMENT '是否推荐',
    sort_order  INT           DEFAULT 0 COMMENT '排序',
    role        VARCHAR(50)   DEFAULT '' COMMENT '我的职责',
    dev_period  VARCHAR(50)   DEFAULT '' COMMENT '开发时间',
    demo_url    VARCHAR(255)  DEFAULT '' COMMENT '在线演示地址',
    source_url  VARCHAR(255)  DEFAULT '' COMMENT '源码地址',
    features    TEXT          COMMENT '核心功能，JSON数组',
    background  TEXT          COMMENT '项目背景',
    goals       TEXT          COMMENT '项目目标，JSON数组',
    architecture VARCHAR(500) DEFAULT '' COMMENT '技术架构',
    challenges  TEXT          COMMENT '遇到的问题，JSON数组',
    results     TEXT          COMMENT '项目成果，JSON数组',
    review      TEXT          COMMENT '项目复盘',
    visible     TINYINT       DEFAULT 1 COMMENT '是否可见',
    deleted     TINYINT       DEFAULT 0 COMMENT '逻辑删除',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='项目表';

-- 文章表
CREATE TABLE IF NOT EXISTS article (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200)  NOT NULL COMMENT '标题',
    summary       VARCHAR(500)  DEFAULT '' COMMENT '摘要',
    category      VARCHAR(30)   DEFAULT '' COMMENT '分类：frontend/backend/mobile/data/tools',
    cover         VARCHAR(255)  DEFAULT '' COMMENT '后台自定义封面',
    source_cover  VARCHAR(500)  DEFAULT '' COMMENT 'CSDN 原文默认封面',
    source_type   VARCHAR(20)   DEFAULT '' COMMENT '来源类型：csdn',
    source_id     BIGINT        DEFAULT NULL COMMENT 'CSDN 文章 id',
    tags          VARCHAR(300)  DEFAULT '' COMMENT '标签，逗号分隔',
    content       MEDIUMTEXT    COMMENT '正文（Markdown）',
    status        VARCHAR(20)   DEFAULT 'draft' COMMENT '状态：published/draft',
    is_recommended TINYINT      DEFAULT 0 COMMENT '是否推荐',
    is_hot        TINYINT       DEFAULT 0 COMMENT '是否热门',
    views         INT           DEFAULT 0 COMMENT '阅读量',
    read_time     VARCHAR(10)   DEFAULT '' COMMENT '阅读时长',
    author        VARCHAR(50)   DEFAULT 'YiHong' COMMENT '作者',
    source_url    VARCHAR(255)  DEFAULT '' COMMENT '原文链接',
    visible       TINYINT       DEFAULT 1,
    deleted       TINYINT       DEFAULT 0,
    create_time   DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文章表';

-- 成长阶段表
CREATE TABLE IF NOT EXISTS growth_stage (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    period      VARCHAR(50)   DEFAULT '' COMMENT '时间范围',
    title       VARCHAR(50)   NOT NULL COMMENT '阶段名称',
    subtitle    VARCHAR(100)  DEFAULT '' COMMENT '副标题',
    icon        VARCHAR(20)   DEFAULT 'seed' COMMENT '图标',
    description TEXT          COMMENT '描述',
    achievements TEXT         COMMENT '成果，JSON数组',
    skills      VARCHAR(300)  DEFAULT '' COMMENT '技能，逗号分隔',
    color       VARCHAR(20)   DEFAULT '#A8D8B9' COMMENT '主题色',
    sort_order  INT           DEFAULT 0,
    visible     TINYINT       DEFAULT 1,
    deleted     TINYINT       DEFAULT 0,
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='成长阶段表';

-- 技能表
CREATE TABLE IF NOT EXISTS skill (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    category    VARCHAR(30)   NOT NULL COMMENT '技能分类',
    name        VARCHAR(50)   NOT NULL COMMENT '技能名称',
    level       VARCHAR(20)   DEFAULT 'learning' COMMENT '掌握程度',
    percentage  INT           DEFAULT 0 COMMENT '熟练度百分比',
    sort_order  INT           DEFAULT 0,
    visible     TINYINT       DEFAULT 1,
    deleted     TINYINT       DEFAULT 0,
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='技能表';

-- 留言表
CREATE TABLE IF NOT EXISTS message (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)   NOT NULL COMMENT '姓名',
    email       VARCHAR(100)  NOT NULL COMMENT '邮箱',
    subject     VARCHAR(200)  DEFAULT '' COMMENT '主题',
    content     TEXT          NOT NULL COMMENT '留言内容',
    status      VARCHAR(20)   DEFAULT 'unread' COMMENT '状态：unread/read/replied',
    ip          VARCHAR(50)   DEFAULT '' COMMENT 'IP地址',
    deleted     TINYINT       DEFAULT 0,
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP,
    update_time DATETIME      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='留言表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS operation_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name   VARCHAR(50)   DEFAULT '' COMMENT '操作用户',
    action      VARCHAR(50)   DEFAULT '' COMMENT '操作类型',
    module      VARCHAR(50)   DEFAULT '' COMMENT '操作模块',
    detail      VARCHAR(500)  DEFAULT '' COMMENT '详情',
    ip          VARCHAR(50)   DEFAULT '' COMMENT 'IP',
    status      VARCHAR(20)   DEFAULT 'success' COMMENT 'success/failed',
    create_time DATETIME      DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 文章与项目不写入演示数据：首次启动后分别从 CSDN、GitHub 同步。

INSERT INTO growth_stage (period, title, subtitle, icon, description, skills, sort_order) VALUES
('2022—2023', '探索阶段', '编程入门与基础构建', 'seed', '开始接触编程世界，从 C 语言入门', 'C,Java,MySQL', 1),
('2023—2024', '实践阶段', '深入 HarmonyOS 与项目实践', 'sprout', '深入学习 HarmonyOS 应用开发', 'HarmonyOS,ArkTS,ArkUI', 2),
('2024—2025', '企业项目阶段', '参与企业级开发实践', 'tree', '参与企业实际项目开发', 'Spring Boot,Vue3', 3),
('2025—至今', '转型与成长', '全栈开发与 AI 探索', 'bigTree', '全面转向全栈开发方向', 'Vue3,Spring Boot,Python', 4);

INSERT INTO skill (category, name, level, percentage, sort_order) VALUES
('前端开发', 'Vue3', '掌握', 78, 1),
('前端开发', 'JavaScript', '掌握', 80, 2),
('后端开发', 'Java', '掌握', 75, 3),
('后端开发', 'Spring Boot', '掌握', 70, 4),
('移动开发', 'HarmonyOS', '掌握', 80, 5),
('数据与 AI', 'Python', '熟悉', 55, 6);

-- 网站设置表
CREATE TABLE IF NOT EXISTS site_setting (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key   VARCHAR(50)  NOT NULL COMMENT '设置键',
    setting_value TEXT         COMMENT '设置值',
    description   VARCHAR(200) DEFAULT '' COMMENT '说明',
    create_time   DATETIME     DEFAULT CURRENT_TIMESTAMP,
    update_time   DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='网站设置表';

-- 证书资质表
CREATE TABLE IF NOT EXISTS certificate (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(100) NOT NULL COMMENT '证书名称',
    category         VARCHAR(30)  DEFAULT '' COMMENT '分类',
    issuer           VARCHAR(100) DEFAULT '' COMMENT '颁发机构',
    issue_date       VARCHAR(20)  DEFAULT '' COMMENT '获得日期',
    description      TEXT         COMMENT '描述',
    credential_no    VARCHAR(100) DEFAULT '' COMMENT '证书编号',
    verification_url VARCHAR(255) DEFAULT '' COMMENT '验证链接',
    image_url        VARCHAR(255) DEFAULT '' COMMENT '证书图片',
    visible          TINYINT      DEFAULT 1 COMMENT '前台展示',
    featured         TINYINT      DEFAULT 0 COMMENT '推荐置顶',
    sort_order       INT          DEFAULT 0 COMMENT '排序',
    created_at       DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='证书资质表';

-- 课程表（v6 课表）
CREATE TABLE IF NOT EXISTS course (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher     VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '教师',
    title       VARCHAR(30)  NOT NULL DEFAULT '' COMMENT '职称（讲师/教授等，无则空）',
    credit      DECIMAL(4,2) NOT NULL DEFAULT 0.00 COMMENT '学分',
    total_hours INT          NOT NULL DEFAULT 0 COMMENT '总学时',
    color       TINYINT      NOT NULL DEFAULT 1 COMMENT '颜色 1-8（前端调色板索引）',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

CREATE TABLE IF NOT EXISTS schedule_entry (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT       NOT NULL COMMENT '课程id',
    day_of_week TINYINT      NOT NULL COMMENT '星期 1-7',
    slot        TINYINT      NOT NULL COMMENT '节次 0=第1-2节 1=第3-4节 2=第5-6节 3=第7-8节',
    start_week  INT          NOT NULL DEFAULT 1 COMMENT '起始周',
    end_week    INT          NOT NULL DEFAULT 1 COMMENT '结束周',
    room        VARCHAR(200) NOT NULL DEFAULT '' COMMENT '地点',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    created_at  DATETIME     DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (course_id),
    INDEX idx_cell (day_of_week, slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课记录';

-- 学期配置种子值（2026-2027 第1学期）
INSERT IGNORE INTO site_setting (setting_key, setting_value, description) VALUES
('semester_start_date', '2026-09-07', '学期开学日期（周一）'),
('semester_total_weeks', '19', '学期总周数'),
('semester_exam_start_week', '17', '考试周起始周');
