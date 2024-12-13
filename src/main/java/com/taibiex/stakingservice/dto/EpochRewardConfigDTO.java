package com.taibiex.stakingservice.dto;

import com.taibiex.stakingservice.entity.EpochRewardConfig;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class EpochRewardConfigDTO {

    @NotNull
    private long epoch;

    @NotNull
    private String rewardAmount;

    @NotNull
    private String tokenSymbol;

    @NotNull
    private String tokenAddress;

    @NotNull
    private boolean mainNet;

    public EpochRewardConfig toEntity() {
        EpochRewardConfig epochRewardConfig = new EpochRewardConfig();
        epochRewardConfig.setEpoch(epoch);
        epochRewardConfig.setRewardAmount(rewardAmount);
        epochRewardConfig.setTokenSymbol(tokenSymbol);
        epochRewardConfig.setTokenAddress(tokenAddress);
        epochRewardConfig.setMainNet(mainNet);
        return epochRewardConfig;
    }
}
