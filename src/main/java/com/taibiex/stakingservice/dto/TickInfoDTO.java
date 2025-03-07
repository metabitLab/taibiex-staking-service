package com.taibiex.stakingservice.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import java.math.BigInteger;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "TickInfo")
public class TickInfoDTO{

    @Schema(description = "tokenId")
    private String tokenId;

    @Schema(description = "tickLower")
    private BigInteger tickLower;

    @Schema(description = "tickUpper")
    private  BigInteger tickUpper;

}

