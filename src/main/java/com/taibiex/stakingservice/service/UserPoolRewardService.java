package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.repository.UserPoolRewardRepository;
import org.springframework.stereotype.Service;

@Service
public class UserPoolRewardService {

    private final UserPoolRewardRepository userPoolRewardRepository;

    public UserPoolRewardService(UserPoolRewardRepository userPoolRewardRepository) {
        this.userPoolRewardRepository = userPoolRewardRepository;
    }


}
