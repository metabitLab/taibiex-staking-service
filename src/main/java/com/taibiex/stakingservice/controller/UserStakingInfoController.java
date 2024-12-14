package com.taibiex.stakingservice.controller;

import com.taibiex.stakingservice.common.bean.ResponseResult;
import com.taibiex.stakingservice.common.constant.ResultEnum;
import com.taibiex.stakingservice.dto.ClaimStakingDTO;
import com.taibiex.stakingservice.dto.SPStakingDTO;
import com.taibiex.stakingservice.entity.ClaimEvent;
import com.taibiex.stakingservice.entity.SPStaking;
import com.taibiex.stakingservice.service.ClaimService;
import com.taibiex.stakingservice.service.SpStakingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/staking")
@Tag(name = "Staking Info")
public class UserStakingInfoController {

    @Autowired
    SpStakingService spStakingService;

    @Autowired
    ClaimService claimService;

    protected HttpServletRequest request;

    protected HttpServletResponse response;

    @ModelAttribute //https://segmentfault.com/a/1190000020087615
    public void initParam(HttpServletRequest request, HttpServletResponse response) throws IOException {
        this.request = request;
        this.response = response;
    }


    @Operation(summary = "获取质押/解质押记录")
    @PostMapping("/list")
    public ResponseResult<Page<SPStaking>> getStakingList(@Valid @RequestBody SPStakingDTO spStakingDTO) {

        Page<SPStaking> stakingPage = spStakingService.getStakingList(spStakingDTO);

        //log.info("分页查询第:[{}]页,pageSize:[{}],共有:[{}]数据,共有:[{}]页", spStakingDTO.getPageNumber(), spStakingDTO.getPageablePageSize(), actorPage.getTotalElements(), actorPage.getTotalPages());
        //List<SPStaking> stakingListBySpecification = actorPage.getContent();

        return new ResponseResult<>(ResultEnum.SUCCESS, stakingPage);
    }

    @Operation(summary = "获取已领取Claim记录")
    @PostMapping("/claimed/list")
    public ResponseResult<Page<ClaimEvent>> getClaimedList(@Valid @RequestBody ClaimStakingDTO claimStakingDTO) {

        Page<ClaimEvent> stakingPage = claimService.getClaimedList(claimStakingDTO);

        //log.info("分页查询第:[{}]页,pageSize:[{}],共有:[{}]数据,共有:[{}]页", spStakingDTO.getPageNumber(), spStakingDTO.getPageablePageSize(), actorPage.getTotalElements(), actorPage.getTotalPages());
        //List<SPStaking> stakingListBySpecification = actorPage.getContent();

        return new ResponseResult<>(ResultEnum.SUCCESS, stakingPage);
    }
}
