package com.taibiex.stakingservice.controller;

import com.taibiex.stakingservice.common.bean.ResponseResult;
import com.taibiex.stakingservice.dto.UserRewardDTO;
import com.taibiex.stakingservice.service.UserPoolRewardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("reward")
@Tag(name = "Staking reward")
public class UserPoolRewardController {

    private final UserPoolRewardService userPoolRewardService;

    public UserPoolRewardController(UserPoolRewardService userPoolRewardService) {
        this.userPoolRewardService = userPoolRewardService;
    }

    @GetMapping("userAddress")
    public ResponseResult<List<UserRewardDTO>> getUserPoolRewardByUserAddress(String userAddress) {
        return ResponseResult.success(userPoolRewardService.getUserPoolRewards(userAddress));
    }
}
