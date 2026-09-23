-- ============================================================
-- v6 课程表（课表）
-- 课程 course + 排课 schedule_entry，学期配置复用 site_setting
-- 三个 key：semester_start_date / semester_total_weeks / semester_exam_start_week
-- ============================================================

CREATE TABLE IF NOT EXISTS course (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL COMMENT '课程名称',
    teacher     VARCHAR(50)  NOT NULL DEFAULT '' COMMENT '教师',
    title       VARCHAR(30)  NOT NULL DEFAULT '' COMMENT '职称（讲师/教授等，无则空）',
    credit      DECIMAL(4,2) NOT NULL DEFAULT 0.00 COMMENT '学分',
    total_hours INT          NOT NULL DEFAULT 0 COMMENT '总学时',
    color       TINYINT      NOT NULL DEFAULT 1 COMMENT '颜色 1-8（前端调色板索引）',
    sort_order  INT          NOT NULL DEFAULT 0 COMMENT '排序',
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
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
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_course (course_id),
    INDEX idx_cell (day_of_week, slot)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='排课记录';
