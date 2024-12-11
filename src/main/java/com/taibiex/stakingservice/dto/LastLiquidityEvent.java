package com.taibiex.stakingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;

@Data
public class LastLiquidityEvent {

    private long epoch;

    private String poolAddress;

    private BigInteger amount;

    private BigInteger amount0;

    private BigInteger amount1;

    private BigInteger stakingAmount;

    private Timestamp eventTime;

    public LastLiquidityEvent(long epoch, String poolAddress, BigInteger amount, BigInteger stakingAmount, Timestamp eventTime) {
        this.epoch = epoch;
        this.poolAddress = poolAddress;
        this.amount = amount;
        this.stakingAmount = stakingAmount;
        this.eventTime = eventTime;
    }

    public LastLiquidityEvent(long epoch, String poolAddress, BigInteger amount, Timestamp eventTime) {
        this.epoch = epoch;
        this.poolAddress = poolAddress;
        this.amount = amount;
        this.eventTime = eventTime;
    }
}
