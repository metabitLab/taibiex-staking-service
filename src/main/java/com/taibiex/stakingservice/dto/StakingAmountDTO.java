package com.taibiex.stakingservice.dto;

import lombok.Data;

import java.math.BigInteger;

@Data
public class StakingAmountDTO {

    private long epoch;

    private BigInteger rewardAmount;

    private BigInteger amount0;

    private BigInteger amount1;
}
