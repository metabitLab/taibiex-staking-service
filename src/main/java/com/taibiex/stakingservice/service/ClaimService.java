package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.entity.ClaimEvent;
import com.taibiex.stakingservice.entity.SPStaking;
import com.taibiex.stakingservice.repository.ClaimRepository;
import com.taibiex.stakingservice.repository.SpStakingRepository;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * claim是领取解锁的token，不是领取奖励. claim那个得等7天才能解锁才能claim
 * claim是一次性领取所有解锁了的本金，claimIndex是选定哪一期解锁. 和奖励没关系
 * (已解锁的)解质押事件(一笔提取所有的解质押事件) 或 提取某一笔质押事件
 */
@Slf4j
@Service
public class ClaimService {

    @Resource
    private ClaimRepository claimRepository;

    @Transactional
    public void save(ClaimEvent claimEvent) {
        //注意：这里数据库存的交易hash中不能和logIndex一起存，因为情况2时这里有可能Pool和NonfungiblePositionManager添加流动性事件共同维护此记录(记录才能完全)
        ClaimEvent m = claimRepository.findByTxHashAndUserAddressAndClaimIndex(claimEvent.getTxHash(), claimEvent.getUserAddress(), claimEvent.getClaimIndex());

        if (m != null) {
            log.info("ClaimService save ignore:  record existed! txHash: {} ,user: {}, claimIndex: {}", claimEvent.getTxHash(), claimEvent.getUserAddress(), claimEvent.getClaimIndex());
            return;
        }

        claimRepository.save(claimEvent);
    }
}
