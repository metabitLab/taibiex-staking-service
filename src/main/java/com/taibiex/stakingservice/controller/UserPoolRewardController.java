package com.taibiex.stakingservice.controller;

import com.taibiex.stakingservice.common.bean.ResponseResult;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.common.constant.ResultEnum;
import com.taibiex.stakingservice.dto.ClaimRequestDTO;
import com.taibiex.stakingservice.dto.UserRewardDTO;
import com.taibiex.stakingservice.service.ClaimRecordService;
import com.taibiex.stakingservice.service.UserPoolRewardService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;


@Log4j2
@RestController
@RequestMapping("reward")
@Tag(name = "Staking reward")
public class UserPoolRewardController {

    private final UserPoolRewardService userPoolRewardService;

    private final ClaimRecordService claimRecordService;

    public UserPoolRewardController(UserPoolRewardService userPoolRewardService,
                                    ClaimRecordService claimRecordService) {
        this.userPoolRewardService = userPoolRewardService;
        this.claimRecordService = claimRecordService;
    }

    @GetMapping("userAddress")
    public ResponseResult<Page<UserRewardDTO>> getUserPoolRewardByUserAddress(String userAddress, Integer pageNumber, Integer pageSize) {
        return ResponseResult.success(userPoolRewardService.getUserPoolRewards(userAddress, pageNumber, pageSize));
    }

    @GetMapping("total")
    public ResponseResult getTotalReward(String userAddress) {
        return ResponseResult.success(userPoolRewardService.getTotalReward(userAddress));
    }

    @GetMapping("checkClaimable")
    public ResponseResult checkClaimable(String userAddress) {
        return ResponseResult.success(userPoolRewardService.checkClaimable(userAddress));
    }

    @PostMapping("claim")
    public ResponseResult claim(@RequestBody ClaimRequestDTO claimRequestDTO) {
        try {
            boolean check = Web3jUtils.isSignatureValid(claimRequestDTO.getUserAddress(), claimRequestDTO.getSign(), claimRequestDTO.getKey());
            if (!check) {
                return ResponseResult.fail(ResultEnum.ERROR_PARAMS.code, "sign error");
            }
        } catch (Exception e) {
            log.info("isSignatureValid exception: ", e);
            return ResponseResult.fail(ResultEnum.ERROR_PARAMS.code, "sign error");
        }
        claimRecordService.claim(claimRequestDTO.getUserAddress());
        return ResponseResult.success(null);
    }
}
