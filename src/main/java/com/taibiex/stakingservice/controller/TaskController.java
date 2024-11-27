package com.taibiex.stakingservice.controller;

import com.taibiex.stakingservice.common.bean.ResponseResult;
import com.taibiex.stakingservice.dto.SwapTaskRequestDTO;
import com.taibiex.stakingservice.service.MintService;
import com.taibiex.stakingservice.service.SwapService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("task")
@Tag(name = "Task")
public class TaskController {

    private final SwapService swapService;

    private final MintService mintService;

    public TaskController(SwapService swapService, MintService mintService) {
        this.swapService = swapService;
        this.mintService = mintService;
    }

    @Operation(summary = "Check swap task")
    @PostMapping("checkSwapTask")
    public ResponseResult swapTask(@RequestBody @Validated SwapTaskRequestDTO request) {

        long swap = swapService.countAllByRecipient(request.getUserAddress().toLowerCase());

        long mint = mintService.countBySender(request.getUserAddress().toLowerCase());

        return ResponseResult.success(swap >= 3 || mint >= 1);
    }
}
