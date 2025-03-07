package com.taibiex.stakingservice.controller;

import com.taibiex.stakingservice.common.bean.ResponseResult;
import com.taibiex.stakingservice.dto.EpochRewardConfigDTO;
import com.taibiex.stakingservice.service.EpochRewardConfigService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/epochReward")
public class EpochRewardConfigController {

    private final EpochRewardConfigService epochRewardConfigService;

    public EpochRewardConfigController(EpochRewardConfigService epochRewardConfigService) {
        this.epochRewardConfigService = epochRewardConfigService;
    }

    @PostMapping("/create")
    public ResponseResult create(@RequestBody EpochRewardConfigDTO epochRewardConfigDTO){
        epochRewardConfigService.save(epochRewardConfigDTO.toEntity());
        return ResponseResult.success(null);
    }
}
