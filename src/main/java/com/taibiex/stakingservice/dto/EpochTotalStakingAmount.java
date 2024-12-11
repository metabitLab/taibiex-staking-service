package com.taibiex.stakingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EpochTotalStakingAmount {

    private long epoch;
    private BigInteger amount;
    private BigInteger totalStakingAmount;
    private Timestamp eventTime;

}
