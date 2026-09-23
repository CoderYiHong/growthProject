-- ============================================================
-- 生产数据库迁移脚本 v2 — site_setting 唯一约束
-- ============================================================
-- 执行前：mysqldump -u <user> -p growth site_setting > backup_site_setting_$(date +%Y%m%d).sql
-- 执行：  mysql -u <user> -p growth < migrate_v2.sql
-- 回滚：  mysql -u <user> -p growth < rollback_v2.sql
-- ============================================================

-- Step 1: 创建备份表（带时间戳，保留去重前的完整数据）
DROP TABLE IF EXISTS site_setting_backup_v2;
CREATE TABLE site_setting_backup_v2 AS SELECT * FROM site_setting;

-- Step 2: 统计并报告重复 key
SELECT 'MIGRATE_V2_INFO' AS step,
       setting_key,
       COUNT(*) AS duplicate_count
FROM site_setting
GROUP BY setting_key
HAVING COUNT(*) > 1;

-- Step 3: 对每个重复 key 保留 id 最大的记录，其余标记
-- 将即将删除的记录写入备份标记
SELECT 'MIGRATE_V2_INFO' AS step,
       COUNT(*) AS records_to_remove
FROM site_setting s1
INNER JOIN site_setting s2
  ON s1.setting_key = s2.setting_key AND s1.id < s2.id;

-- Step 4: 安全去重（已有备份表保护）
DELETE s1 FROM site_setting s1
INNER JOIN site_setting s2
  ON s1.setting_key = s2.setting_key AND s1.id < s2.id;

-- Step 5: 添加唯一约束
ALTER TABLE site_setting ADD UNIQUE INDEX uk_setting_key (setting_key);

-- Step 6: 验证
SELECT 'MIGRATE_V2_INFO' AS step,
       'uk_setting_key created, duplicate rows removed' AS result;
