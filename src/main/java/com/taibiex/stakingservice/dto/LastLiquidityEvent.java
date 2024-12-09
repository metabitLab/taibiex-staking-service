package com.taibiex.stakingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LastLiquidityEvent {

    private long epoch;

    private String poolAddress;

    private BigInteger stakingAmount0;

    private BigInteger stakingAmount1;

    private Timestamp eventTime;

}
