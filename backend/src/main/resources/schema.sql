-- 自动建表（Spring Boot 启动时通过 spring.sql.init 执行）
-- 兼容 MySQL / H2（MySQL 兼容模式）
CREATE TABLE IF NOT EXISTS project (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100)  NOT NULL,
    category    VARCHAR(30)   NOT NULL,
    cover       VARCHAR(255)  DEFAULT '',
    source_cover VARCHAR(500) DEFAULT '',
    source_type VARCHAR(20)   DEFAULT '',
    source_id   BIGINT,
    summary     VARCHAR(500)  DEFAULT '',
    description TEXT,
    tech_stack  VARCHAR(500)  DEFAULT '',
    status      VARCHAR(20)   DEFAULT 'in_progress',
    is_recommended INT        DEFAULT 0,
    sort_order  INT           DEFAULT 0,
    role        VARCHAR(50)   DEFAULT '',
    dev_period  VARCHAR(50)   DEFAULT '',
    demo_url    VARCHAR(255)  DEFAULT '',
    source_url  VARCHAR(255)  DEFAULT '',
    features    TEXT,
    background  TEXT,
    goals       TEXT,
    architecture VARCHAR(500) DEFAULT '',
    challenges  TEXT,
    results     TEXT,
    review      TEXT,
    visible     INT           DEFAULT 1,
    deleted     INT           DEFAULT 0,
    create_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS article (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    title         VARCHAR(200)  NOT NULL,
    summary       VARCHAR(500)  DEFAULT '',
    category      VARCHAR(30)   DEFAULT '',
    cover         VARCHAR(255)  DEFAULT '',
    source_cover  VARCHAR(500)  DEFAULT '',
    source_type   VARCHAR(20)   DEFAULT '',
    source_id     BIGINT,
    tags          VARCHAR(300)  DEFAULT '',
    content       TEXT,
    status        VARCHAR(20)   DEFAULT 'draft',
    is_recommended INT          DEFAULT 0,
    is_hot        INT           DEFAULT 0,
    views         INT           DEFAULT 0,
    read_time     VARCHAR(10)   DEFAULT '',
    author        VARCHAR(50)   DEFAULT 'YiHong',
    source_url    VARCHAR(255)  DEFAULT '',
    visible       INT           DEFAULT 1,
    deleted       INT           DEFAULT 0,
    create_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS growth_stage (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    period      VARCHAR(50)   DEFAULT '',
    title       VARCHAR(50)   NOT NULL,
    subtitle    VARCHAR(100)  DEFAULT '',
    icon        VARCHAR(20)   DEFAULT 'seed',
    description TEXT,
    achievements TEXT,
    skills      VARCHAR(300)  DEFAULT '',
    color       VARCHAR(20)   DEFAULT '#A8D8B9',
    sort_order  INT           DEFAULT 0,
    visible     INT           DEFAULT 1,
    deleted     INT           DEFAULT 0,
    create_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS skill (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    category    VARCHAR(30)   NOT NULL,
    name        VARCHAR(50)   NOT NULL,
    level       VARCHAR(20)   DEFAULT 'learning',
    percentage  INT           DEFAULT 0,
    sort_order  INT           DEFAULT 0,
    visible     INT           DEFAULT 1,
    deleted     INT           DEFAULT 0,
    create_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS message (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(50)   NOT NULL,
    email       VARCHAR(100)  NOT NULL,
    subject     VARCHAR(200)  DEFAULT '',
    content     TEXT          NOT NULL,
    status      VARCHAR(20)   DEFAULT 'unread',
    ip          VARCHAR(50)   DEFAULT '',
    deleted     INT           DEFAULT 0,
    create_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS site_setting (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    setting_key   VARCHAR(100)  NOT NULL,
    setting_value TEXT,
    description   VARCHAR(255)  DEFAULT '',
    create_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_setting_key (setting_key)
);

-- 为已存在的表补加唯一约束（仅当约束不存在时执行）
-- ALTER TABLE site_setting ADD UNIQUE INDEX IF NOT EXISTS uk_setting_key (setting_key);

CREATE TABLE IF NOT EXISTS operation_log (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_name   VARCHAR(50)   DEFAULT '',
    action      VARCHAR(50)   DEFAULT '',
    module      VARCHAR(50)   DEFAULT '',
    detail      VARCHAR(500)  DEFAULT '',
    ip          VARCHAR(50)   DEFAULT '',
    status      VARCHAR(20)   DEFAULT 'success',
    create_time TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

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
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 课程表（v6）
CREATE TABLE IF NOT EXISTS course (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    teacher     VARCHAR(50)  NOT NULL DEFAULT '',
    title       VARCHAR(30)  NOT NULL DEFAULT '',
    credit      DECIMAL(4,2) NOT NULL DEFAULT 0.00,
    total_hours INT          NOT NULL DEFAULT 0,
    color       TINYINT      NOT NULL DEFAULT 1,
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS schedule_entry (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    course_id   BIGINT       NOT NULL,
    day_of_week TINYINT      NOT NULL,
    slot        TINYINT      NOT NULL,
    start_week  INT          NOT NULL DEFAULT 1,
    end_week    INT          NOT NULL DEFAULT 1,
    room        VARCHAR(200) NOT NULL DEFAULT '',
    sort_order  INT          NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_schedule_course (course_id),
    INDEX idx_schedule_cell (day_of_week, slot)
);

-- 注意：MySQL 不支持 CREATE INDEX IF NOT EXISTS，索引已并入上面的建表语句
-- （表已存在时 CREATE TABLE IF NOT EXISTS 会整体跳过，保持幂等）。
-- 老库如需补索引，执行一次：
-- ALTER TABLE schedule_entry ADD INDEX idx_schedule_course (course_id), ADD INDEX idx_schedule_cell (day_of_week, slot);
