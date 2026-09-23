-- ============================================================
-- 回滚脚本 v2 — 恢复 site_setting 迁移前状态
-- 前提：migrate_v2.sql 执行前已创建 site_setting_backup_v2 备份表
-- 执行：mysql -u <user> -p growth < rollback_v2.sql
-- ============================================================
-- 注意：DDL（DROP/ADD INDEX）在 MySQL 中隐式提交事务
-- 本脚本应在业务低峰期执行

-- Step 1: 删除唯一约束
ALTER TABLE site_setting DROP INDEX uk_setting_key;

-- Step 2: 从备份恢复原始数据
DELETE FROM site_setting;
INSERT INTO site_setting SELECT * FROM site_setting_backup_v2;

-- Step 3: 清理备份表（可选，确认数据正确后再执行）
-- DROP TABLE site_setting_backup_v2;

-- Step 4: 验证
SELECT 'ROLLBACK_V2_INFO' AS step,
       COUNT(*) AS restored_rows
FROM site_setting;
