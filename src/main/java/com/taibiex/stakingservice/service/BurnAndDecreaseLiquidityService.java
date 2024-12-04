package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.chain.contract.utils.CustomBeanUtils;
import com.taibiex.stakingservice.entity.BurnAndDecreaseLiquidity;
import com.taibiex.stakingservice.entity.BurnAndDecreaseLiquidity;
import com.taibiex.stakingservice.repository.BurnAndDecreaseLiquidityRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class BurnAndDecreaseLiquidityService {

    private final BurnAndDecreaseLiquidityRepository burnAndDecreaseLiquidityRepository;

    public BurnAndDecreaseLiquidityService(BurnAndDecreaseLiquidityRepository burnAndDecreaseLiquidityRepository) {
        this.burnAndDecreaseLiquidityRepository = burnAndDecreaseLiquidityRepository;
    }

    @Transactional
    public void save(BurnAndDecreaseLiquidity burnAndDecreaseLiquidity) {
        //注意：这里数据库存的交易hash中不能和logIndex一起存，因为情况2时这里有可能Pool和NonfungiblePositionManager添加流动性事件共同维护此记录(记录才能完全)
        BurnAndDecreaseLiquidity m = burnAndDecreaseLiquidityRepository.findByTxHash(burnAndDecreaseLiquidity.getTxHash());
        if (m != null) {

            //for update by id
            burnAndDecreaseLiquidity.setId(m.getId());

            //copyProperties(Object source, Object target)
            CustomBeanUtils.copyNonNullAndNonEmptyProperties(burnAndDecreaseLiquidity, m);

            burnAndDecreaseLiquidityRepository.save(m);
            return;

            //log.info("BurnAndDecreaseLiquidity save ignore:  record existed! txHash: {}", burnAndDecreaseLiquidity.getTxHash());
            //return;
        }

        burnAndDecreaseLiquidityRepository.save(burnAndDecreaseLiquidity);
    }

    public long countBySender(String sender) {
        return burnAndDecreaseLiquidityRepository.countAllBySender(sender);
    }
}
