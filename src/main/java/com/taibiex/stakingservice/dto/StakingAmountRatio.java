package com.taibiex.stakingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StakingAmountRatio {

    private long epoch;

    private BigInteger stakingAmountRatio;

    private BigInteger amount0;

    private BigInteger amount1;

}
