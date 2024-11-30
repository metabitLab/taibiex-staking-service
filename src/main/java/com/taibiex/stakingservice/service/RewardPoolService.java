package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.constant.PoolMapSingleton;
import com.taibiex.stakingservice.entity.RewardPool;
import com.taibiex.stakingservice.repository.RewardPoolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
public class RewardPoolService {

    @Autowired
    RewardPoolRepository rewardPoolRepository;



    public List<RewardPool> findAll() {
        return rewardPoolRepository.findAll();
    }

    @Transactional
    public void save(RewardPool rewardPool) {
        RewardPool p = rewardPoolRepository.findByPool(rewardPool.getPool());
        if (p != null) {
            log.info("RewardPool save: pool {} existed, ignore", rewardPool);
            return;
        }
        PoolMapSingleton.put(rewardPool.getPool(), rewardPool);
        rewardPoolRepository.save(rewardPool);
    }
}
