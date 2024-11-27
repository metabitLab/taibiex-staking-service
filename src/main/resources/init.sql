show variables like '%time_zone%';
set global time_zone = '+0:00'; # 修改为utc，正0时区，世界统一时间
SET time_zone = '+0:00';
# 修改为utc，正0时区，世界统一时间

-- time_zone说明mysql使用system的时区，system_time_zone说明system使用CST时区
-- 立即生效
flush privileges;

CREATE DATABASE IF NOT EXISTS `taibiex_staking`;
