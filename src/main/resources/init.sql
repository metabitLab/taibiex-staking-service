show variables like '%time_zone%';
set global time_zone = '+0:00'; # 修改为utc，正0时区，世界统一时间
SET time_zone = '+0:00';
# 修改为utc，正0时区，世界统一时间

-- time_zone说明mysql使用system的时区，system_time_zone说明system使用CST时区
-- 立即生效
flush privileges;

CREATE DATABASE IF NOT EXISTS `taibiex_staking`;

/*test data*/
INSERT INTO taibiex_staking.reward_pools_config (create_time,last_update_time,fee,pool,token0,token1) VALUES
    ('2024-12-04 23:22:42','2024-12-04 23:22:42','100','0x497244841295B8941086D2271bB3a5bB1e8277B4','0x2ccad515c13df2178f6960304ae0dbe0428e8d28','0xcdc10593a66185aaa206665c5083ac51ad935f91');
INSERT INTO taibiex_staking.reward_pools_tick_range_config (create_time,last_update_time,pool,reward_ratio,tick_lower,tick_upper,reward_pool_id) VALUES
    ('2024-12-05 08:31:30','2024-12-05 08:31:30','0x497244841295B8941086D2271bB3a5bB1e8277B4',1000,'-296','304',1);
