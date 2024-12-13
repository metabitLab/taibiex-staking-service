package com.taibiex.stakingservice.dto;

import lombok.Data;

@Data
public class UserRewardDTO {

    private String userAddress;

    private long epoch;

    private String rewardAmount;

    private String lp;

    private String epochRewardAmount;

    private String tokenSymbol;

    private String tokenAddress;

    private boolean claimed;

}
