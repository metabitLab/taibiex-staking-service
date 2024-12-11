package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.constant.PoolMapSingleton;
import com.taibiex.stakingservice.entity.RewardPool;
import com.taibiex.stakingservice.repository.RewardPoolRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 获取所有池子对应的奖励比率
     * @return
     * poolAddress -> rewardRatio
     */
    public Map<String, String> getPoolRewardMap() {
        Map<String, String> rewardMap = new HashMap<>();
        rewardPoolRepository.findAll().forEach(rewardPool -> rewardMap.put(rewardPool.getPool(), rewardPool.getFee()));
        return rewardMap;
    }

    /**
     * 获取奖池内不同tick的奖励比率
     */
    public Map<String, BigInteger> getPoolTickRewardMap(String poolAddress) {
        Map<String, BigInteger> rewardMap = new HashMap<>();
        RewardPool pool = rewardPoolRepository.findByPool(poolAddress);
        if (pool == null){
            return rewardMap;
        }
        pool.getRewardPoolTickRanges().forEach(rewardPoolTickRange -> rewardMap.put(rewardPoolTickRange.getTickLower() + "-" + rewardPoolTickRange.getTickUpper(), rewardPoolTickRange.getRewardRatio()));
        return rewardMap;
    }

    /**
     * 获取奖池内不同tick占总奖励的奖励比率
     */
    public Map<Long, BigInteger> getPoolTickRewardRatioMap(String poolAddress) {
        Map<Long, BigInteger> rewardMap = new HashMap<>();
        RewardPool pool = rewardPoolRepository.findByPool(poolAddress);
        if (pool == null){
            return rewardMap;
        }
        String fee = pool.getFee();
        pool.getRewardPoolTickRanges().forEach(rewardPoolTickRange -> rewardMap.put(rewardPoolTickRange.getId(), rewardPoolTickRange.getRewardRatio().multiply(new BigInteger(fee)).divide(new BigInteger("10000"))));
        return rewardMap;
    }
}
