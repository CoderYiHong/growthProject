-- ============================================================
-- v4 CSDN / GitHub 来源同步字段（可重复执行）
-- 执行前请先备份 growth 数据库
-- ============================================================

-- 用存储过程实现幂等：仅在列不存在时添加
DROP PROCEDURE IF EXISTS add_column_if_not_exists;

DELIMITER //
CREATE PROCEDURE add_column_if_not_exists(
    IN tbl_name VARCHAR(64),
    IN col_name VARCHAR(64),
    IN col_def  VARCHAR(512)
)
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.COLUMNS
        WHERE TABLE_SCHEMA = DATABASE()
        AND TABLE_NAME = tbl_name
        AND COLUMN_NAME = col_name
    ) THEN
        SET @sql = CONCAT('ALTER TABLE ', tbl_name, ' ADD COLUMN ', col_name, ' ', col_def);
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END //
DELIMITER ;

CALL add_column_if_not_exists('article', 'source_cover', "VARCHAR(500) DEFAULT '' COMMENT 'CSDN 原文默认封面' AFTER cover");
CALL add_column_if_not_exists('article', 'source_type',  "VARCHAR(20)  DEFAULT '' COMMENT '来源类型：csdn' AFTER source_cover");
CALL add_column_if_not_exists('article', 'source_id',    "BIGINT DEFAULT NULL COMMENT 'CSDN 文章 id' AFTER source_type");

CALL add_column_if_not_exists('project', 'source_cover', "VARCHAR(500) DEFAULT '' COMMENT 'GitHub 默认封面' AFTER cover");
CALL add_column_if_not_exists('project', 'source_type',  "VARCHAR(20)  DEFAULT '' COMMENT '来源类型：github' AFTER source_cover");
CALL add_column_if_not_exists('project', 'source_id',    "BIGINT DEFAULT NULL COMMENT 'GitHub repository id' AFTER source_type");

DROP PROCEDURE IF EXISTS add_column_if_not_exists;

-- 已同步过的 CSDN 记录可直接识别；无来源链接的旧演示文章仍保持空来源，
-- 因而不会再出现在后台来源列表或网站前台。
UPDATE article
SET source_type = 'csdn',
    source_id = CAST(SUBSTRING_INDEX(source_url, '/', -1) AS UNSIGNED)
WHERE source_url LIKE 'https://blog.csdn.net/%/article/details/%'
  AND (source_type IS NULL OR source_type = '');

-- 项目不批量标记。只有 GitHub API 确认返回的仓库才会在首次同步时标记为 github，
-- 这样旧的本地演示项目不会被误当成真实仓库展示。
