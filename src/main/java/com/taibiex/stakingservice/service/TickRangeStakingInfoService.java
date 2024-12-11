package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.repository.TickRangeStakingInfoRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class TickRangeStakingInfoService {

    private final TickRangeStakingInfoRepository tickRangeStakingInfoRepository;

    public TickRangeStakingInfoService(TickRangeStakingInfoRepository tickRangeStakingInfoRepository) {
        this.tickRangeStakingInfoRepository = tickRangeStakingInfoRepository;
    }
}
