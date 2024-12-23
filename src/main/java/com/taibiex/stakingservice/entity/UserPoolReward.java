package com.taibiex.stakingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 用户奖池的奖励信息
 */
@Entity
@Table(name = "user_pool_reward", indexes = {
        @Index(name = "user_idx", columnList = "user_address"),
        @Index(name = "pool_idx", columnList = "pool"),
        @Index(name = "epoch_idx", columnList = "epoch"),
        @Index(name = "create_time_idx", columnList = "create_time"),
        @Index(name = "last_update_time_idx", columnList = "last_update_time")
})
@Data
public class UserPoolReward extends BaseEntity{

    @Column(name = "epoch", nullable = false)
    private long epoch;

    @Column(name = "user_address", nullable = false, length = 100)
    private String userAddress;

    @Column(name = "reward_amount", nullable = false, columnDefinition = " varchar(255) default '0'")
    private String rewardAmount;

    @Column(name = "lp", nullable = false, length = 100, columnDefinition = " varchar(255) default '0'")
    private String lp;

    @Column(name = "pool", nullable = false, length = 100)
    private String pool;

    @Column(name = "token_symbol", nullable = false, length = 100)
    private String tokenSymbol;

    @Column(name = "token_address", nullable = false, length = 100)
    private String tokenAddress;

    @Column(name = "main_net", nullable = false)
    private boolean mainNet;

    @Column(name = "claimed", nullable = false)
    private boolean claimed;

}
