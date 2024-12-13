package com.taibiex.stakingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity
@Table(name = "epoch_reward_config", indexes = {
        @jakarta.persistence.Index(name = "epoch_idx", columnList = "epoch"),
})
public class EpochRewardConfig extends BaseEntity{

    @Column(name = "epoch")
    private long epoch;

    @Column(name = "reward_amount")
    private String rewardAmount;

    @Column(name = "token_symbol")
    private String tokenSymbol;

    @Column(name = "token_address")
    private String tokenAddress;

    @Column(name = "main_net")
    private boolean mainNet;

}
