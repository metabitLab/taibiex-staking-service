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
        @Index(name = "range_id_idx", columnList = "range_id"),
        @Index(name = "pool_idx", columnList = "pool")
})
@Data
public class UserPoolReward extends BaseEntity{

    @Column(name = "epoch", nullable = false)
    private long epoch;

    @Column(name = "user_address", nullable = false, length = 100)
    private String userAddress;

    @Column(name = "reward_amount", nullable = false, columnDefinition = "default 0")
    private String rewardAmount;

    @Column(name = "lp", nullable = false, length = 100, columnDefinition = "default 0")
    private String lp;

    @Column(name = "pool", nullable = false, length = 100)
    private String pool;

    @Column(name = "token_symbol", nullable = false, length = 100)
    private String tokenSymbol;

    @Column(name = "token_address", nullable = false, length = 100)
    private String tokenAddress;

    @Column(name = "claimed", nullable = false)
    private boolean claimed;

}
