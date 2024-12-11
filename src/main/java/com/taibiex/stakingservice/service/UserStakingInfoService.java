package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.repository.UserStakingInfoRepository;
import org.springframework.stereotype.Service;

@Service
public class UserStakingInfoService {

    private final UserStakingInfoRepository userStakingInfoRepository;

    public UserStakingInfoService(UserStakingInfoRepository userStakingInfoRepository) {
        this.userStakingInfoRepository = userStakingInfoRepository;
    }

    public void LiquidityEventHandler(String txHash) {
        // TODO: 2023/4/26
    }
}
