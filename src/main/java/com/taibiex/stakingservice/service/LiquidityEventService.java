package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.chain.contract.utils.CustomBeanUtils;
import com.taibiex.stakingservice.entity.LiquidityEvent;
import com.taibiex.stakingservice.repository.LiquidityEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class LiquidityEventService {

    private final LiquidityEventRepository liquidityEventRepository;

    public LiquidityEventService(LiquidityEventRepository liquidityEventRepository) {
        this.liquidityEventRepository = liquidityEventRepository;
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

        liquidityEventRepository.save(liquidityEvent);
    }
}
