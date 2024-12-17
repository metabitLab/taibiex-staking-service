package com.taibiex.stakingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "claim_record", indexes = {
        @Index(name = "user_idx", columnList = "user_address"),
})
public class ClaimRecord extends BaseEntity{

    @Column(name = "user_address", nullable = false, length = 100)
    private String userAddress;

    @Column(name = "token_address", nullable = false, length = 100)
    private String tokenAddress;

    @Column(name = "token_symbol", nullable = false, length = 100)
    private String tokenSymbol;

    @Column(name = "reward_amount", nullable = false, length = 100)
    private String rewardAmount;

    @Column(name = "main_net", nullable = false)
    private boolean mainNet;

    /**
     * 记录关联奖池奖励的id
     */
    @Column(name = "pool_reward_id", nullable = false, columnDefinition = " text")
    private String poolRewardId;

    @Column(name = "claimed", nullable = false)
    private boolean claimed;

    @Column(name = "tx_hash", length = 100)
    private String txHash;

}
