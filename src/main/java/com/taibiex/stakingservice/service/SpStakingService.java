package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.entity.LiquidityEvent;
import com.taibiex.stakingservice.entity.SPStaking;
import com.taibiex.stakingservice.repository.SpStakingRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 单币质押/解质押事件记录
 */
@Slf4j
@Service
public class SpStakingService {

    @Resource
    private SpStakingRepository spStakingRepository;

    @Transactional
    public void save(SPStaking spStaking) {
        //注意：这里数据库存的交易hash中不能和logIndex一起存，因为情况2时这里有可能Pool和NonfungiblePositionManager添加流动性事件共同维护此记录(记录才能完全)
        SPStaking m = spStakingRepository.findByTxHashAndType(spStaking.getTxHash(), spStaking.getType());
        if (m != null) {
            log.info("SpStakingService save ignore:  record existed! txHash: {} ,type: {}", spStaking.getTxHash(), spStaking.getType());
            return;
        }

        spStakingRepository.save(spStaking);
    }
}
