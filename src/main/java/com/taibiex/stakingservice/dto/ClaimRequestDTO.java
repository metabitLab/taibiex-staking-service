package com.taibiex.stakingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.SchemaProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
@Schema(name = "ClaimRequestDTO", description = "Claim request data transfer object")
public class ClaimRequestDTO {

    @NotNull
    @SchemaProperty(name = "userAddress")
    private String userAddress;

    @NotNull
    @SchemaProperty(name = "key")
    private String key;

    @NotNull
    @SchemaProperty(name = "sign")
    private String sign;
}
