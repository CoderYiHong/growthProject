-- ============================================================
-- v5 学习计划持久化
-- 将前端 localStorage 中的学习计划、子任务、打卡记录迁移到数据库
-- ============================================================

CREATE TABLE IF NOT EXISTS study_plan (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    category    VARCHAR(30)  NOT NULL DEFAULT '技术学习' COMMENT '分类',
    type        VARCHAR(30)  DEFAULT '' COMMENT '类型（技术线/认证考试等）',
    name        VARCHAR(200) NOT NULL COMMENT '计划名称',
    status      VARCHAR(20)  NOT NULL DEFAULT 'pending' COMMENT '状态',
    start_date  VARCHAR(20)  DEFAULT '' COMMENT '开始日期',
    end_date    VARCHAR(20)  DEFAULT '' COMMENT '截止日期',
    planned_hours  DOUBLE   DEFAULT 0 COMMENT '计划时长(h)',
    actual_hours   DOUBLE   DEFAULT 0 COMMENT '实际时长(h)',
    progress       INT      DEFAULT 0 COMMENT '进度 0-100',
    importance     INT      DEFAULT 3 COMMENT '重要程度 1-5',
    note           VARCHAR(500) DEFAULT '' COMMENT '备注',
    subject        VARCHAR(50)  DEFAULT '' COMMENT '考研科目',
    chapters       VARCHAR(200) DEFAULT '' COMMENT '章节',
    questions_done   INT DEFAULT 0 COMMENT '已做题数',
    questions_total  INT DEFAULT 0 COMMENT '总题数',
    accuracy         INT DEFAULT 0 COMMENT '正确率%',
    study_round     INT DEFAULT 1 COMMENT '第几轮复习',
    last_study_time DATETIME NULL COMMENT '最近学习时间',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='学习计划';

CREATE TABLE IF NOT EXISTS plan_group (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id     BIGINT NOT NULL COMMENT '所属计划',
    name        VARCHAR(100) NOT NULL COMMENT '系列名称',
    sort_order  INT DEFAULT 0,
    start_date  VARCHAR(20) DEFAULT '',
    end_date    VARCHAR(20) DEFAULT '',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_plan (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务系列';

CREATE TABLE IF NOT EXISTS plan_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    group_id        BIGINT NOT NULL COMMENT '所属系列',
    plan_id         BIGINT NOT NULL COMMENT '所属计划（冗余便于查询）',
    title           VARCHAR(300) NOT NULL COMMENT '子任务标题',
    status          VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '状态',
    est_pomodoros   INT DEFAULT 1 COMMENT '预计番茄数',
    actual_pomodoros INT DEFAULT 0 COMMENT '实际番茄数',
    weight          INT DEFAULT 1 COMMENT '权重 1-5',
    sort_order      INT DEFAULT 0,
    start_date      VARCHAR(20) DEFAULT '',
    end_date        VARCHAR(20) DEFAULT '',
    created_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_group (group_id),
    INDEX idx_plan  (plan_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='子任务';

CREATE TABLE IF NOT EXISTS checkin_record (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    plan_id     BIGINT NOT NULL COMMENT '所属计划',
    item_id     BIGINT NULL COMMENT '关联子任务',
    date        VARCHAR(30) NOT NULL COMMENT '打卡日期',
    duration    INT DEFAULT 0 COMMENT '专注时长(分钟)',
    content     VARCHAR(500) DEFAULT '' COMMENT '学习内容',
    note        VARCHAR(500) DEFAULT '' COMMENT '学习心得',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_plan_date (plan_id, date),
    INDEX idx_date (date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='打卡记录';
