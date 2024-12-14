package com.taibiex.stakingservice.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 单币质押/解质押事件记录
 */

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "SPStakingDTO 质押解质押请求")
public class SPStakingDTO extends BaseDTO {

    @Schema(description = "交易hash")
    private String txHash;

    @Schema(description = "用户地址")
    @NotBlank(message = "用户地址不能为空")
    private String userAddress;

    @Schema(description = "质押类型: 1.质押  0.解除质押. <0或不传该参数.全部 ")
    @NotNull
    @Builder.Default
    private Short type = -1;

}

