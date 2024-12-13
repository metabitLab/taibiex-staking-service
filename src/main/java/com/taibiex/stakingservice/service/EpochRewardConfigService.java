package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.entity.EpochRewardConfig;
import com.taibiex.stakingservice.repository.EpochRewardConfigRepository;
import org.springframework.stereotype.Service;

@Service
public class EpochRewardConfigService {

    private final EpochRewardConfigRepository epochRewardConfigRepository;

    public EpochRewardConfigService(EpochRewardConfigRepository epochRewardConfigRepository) {
        this.epochRewardConfigRepository = epochRewardConfigRepository;
    }

    public void save(EpochRewardConfig epochRewardConfig) {
        long epoch = epochRewardConfig.getEpoch();
        EpochRewardConfig rewardConfig = epochRewardConfigRepository.findByEpoch(epoch);
        if (rewardConfig != null){
            rewardConfig.setRewardAmount(epochRewardConfig.getRewardAmount());
            rewardConfig.setTokenSymbol(epochRewardConfig.getTokenSymbol());
            rewardConfig.setTokenAddress(epochRewardConfig.getTokenAddress());
            rewardConfig.setMainNet(epochRewardConfig.isMainNet());
            epochRewardConfigRepository.save(rewardConfig);
        } else
            epochRewardConfigRepository.save(epochRewardConfig);
    }

    public EpochRewardConfig getEpochRewardConfig() {
        return epochRewardConfigRepository.findFirstOrderByEpochDesc();
    }

}
