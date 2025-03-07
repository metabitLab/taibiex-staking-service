package com.taibiex.stakingservice.dto;


import com.taibiex.stakingservice.common.hibernate.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * claim是领取解锁的token，不是领取奖励. claim那个得等7天才能解锁才能claim
 */

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "ClaimStakingDTO 获取已领取解锁的token的列表")
public class ClaimStakingDTO extends BaseDTO {

    @Schema(description = "交易hash")
    private String txHash;

    @Schema(description = "用户地址")
    @NotBlank(message = "用户地址不能为空")
    private String userAddress;

    @Schema(description = "claim单笔解质押时的索引，不传该参数或为空字符串时 表示查询单币claim所有的已领取记录")
    private  String claimIndex;

}

