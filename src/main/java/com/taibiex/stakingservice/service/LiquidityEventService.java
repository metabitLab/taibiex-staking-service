package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.chain.contract.utils.CustomBeanUtils;
import com.taibiex.stakingservice.entity.ActivityUser;
import com.taibiex.stakingservice.entity.LiquidityEvent;
import com.taibiex.stakingservice.entity.RewardPool;
import com.taibiex.stakingservice.repository.LiquidityEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LiquidityEventService {

    private final LiquidityEventRepository liquidityEventRepository;

    private final UserStakingInfoService userStakingInfoService;

    private final TickRangeStakingInfoService tickRangeStakingInfoService;

    private final ActivityUserService activityUserService;

    private final RewardPoolService rewardPoolService;

    public LiquidityEventService(LiquidityEventRepository liquidityEventRepository,
                                 UserStakingInfoService userStakingInfoService,
                                 TickRangeStakingInfoService tickRangeStakingInfoService,
                                 ActivityUserService activityUserService,
                                 RewardPoolService rewardPoolService) {
        this.liquidityEventRepository = liquidityEventRepository;
        this.userStakingInfoService = userStakingInfoService;
        this.tickRangeStakingInfoService = tickRangeStakingInfoService;
        this.activityUserService = activityUserService;
        this.rewardPoolService = rewardPoolService;
    }

    @Transactional
    public void save(LiquidityEvent liquidityEvent) {
        //注意：这里数据库存的交易hash中不能和logIndex一起存，因为情况2时这里有可能Pool和NonfungiblePositionManager添加流动性事件共同维护此记录(记录才能完全)
        LiquidityEvent m = liquidityEventRepository.findByTxHashAndType(liquidityEvent.getTxHash(), liquidityEvent.getType());
        if (m != null) {

            //for update by id
            liquidityEvent.setId(m.getId());

            //copyProperties(Object source, Object target)
            CustomBeanUtils.copyNonNullAndNonEmptyProperties(liquidityEvent, m);

            liquidityEventRepository.save(m);
            return;

            //log.info("BurnAndDecreaseLiquidity save ignore:  record existed! txHash: {}", burnAndDecreaseLiquidity.getTxHash());
            //return;
        }

        List<RewardPool> rewardPools = rewardPoolService.findAll();
        Set<String> set = rewardPools.stream().map(RewardPool::getPool).collect(Collectors.toSet());
        if (set.contains(liquidityEvent.getPool())){
            liquidityEventRepository.save(liquidityEvent);
            userStakingInfoService.liquidityEventHandler(liquidityEvent);
            tickRangeStakingInfoService.liquidityEventHandler(liquidityEvent);
            ActivityUser activityUser = new ActivityUser();
            activityUser.setUserAddress(liquidityEvent.getOwner());
            activityUserService.save(activityUser);
        }
    }
}
