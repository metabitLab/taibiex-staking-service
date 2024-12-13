package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.constant.PoolMapSingleton;
import com.taibiex.stakingservice.common.utils.XIntervalOverlap;
import com.taibiex.stakingservice.entity.RewardPool;
import com.taibiex.stakingservice.entity.RewardPoolTickRange;
import com.taibiex.stakingservice.repository.RewardPoolRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
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


    /**
     * 判断Pool地址是否在奖池中，此函数不判断 tick是否在奖池中
     *
     * @param poolAddress
     * @return
     */
    public Boolean poolInRewardPool(String poolAddress) {
        if (StringUtils.isEmpty(poolAddress) || StringUtils.isEmpty(poolAddress.trim())) {
            return false;
        }
        poolAddress = poolAddress.trim();

        List<RewardPool> rewardPools = findAll();
        if (null == rewardPools) {
            //没有配置过滤地址，就是全部都要
            return true;
        }

        for (int i = 0; i < rewardPools.size(); i++) {
            RewardPool rewardPool = rewardPools.get(i);
            String _poolAddress = rewardPool.getPool();
            if (null != _poolAddress) {
                _poolAddress = _poolAddress.trim();
            }
            //注意 pool address  是根据 token0, token1, fee 生成，所以不用单独再过滤fee
            if (StringUtils.equalsIgnoreCase(_poolAddress, poolAddress)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 判断Pool地址和[tickLower, tickUpper] 是否在奖池中，此函数不判断 tick是否在奖池中
     *
     * @param poolAddress
     * @param tickLower
     * @param tickUpper
     * @return
     */
    public Boolean poolAndTickInRewardPool(String poolAddress, BigInteger tickLower, BigInteger tickUpper) {
        if (StringUtils.isEmpty(poolAddress) || StringUtils.isEmpty(poolAddress.trim())) {
            return false;
        }
        poolAddress = poolAddress.trim();

        List<RewardPool> rewardPools = findAll();
        if (null == rewardPools) {
            //没有配置过滤地址，就是全部都要
            return true;
        }

        for (int i = 0; i < rewardPools.size(); i++) {
            RewardPool rewardPool = rewardPools.get(i);
            String _poolAddress = rewardPool.getPool();
            List<RewardPoolTickRange>  rewardPoolTickRanges = rewardPool.getRewardPoolTickRanges();

            if (null != _poolAddress) {
                _poolAddress = _poolAddress.trim();
            }
            //注意 pool address  是根据 token0, token1, fee 生成，所以不用单独再过滤fee
            if (StringUtils.equalsIgnoreCase(_poolAddress, poolAddress)) {
                for (int j = 0; j < rewardPoolTickRanges.size(); j++) {
                    RewardPoolTickRange rewardPoolTickRange = rewardPoolTickRanges.get(i);
                    String _poolAddress1 = rewardPoolTickRange.getPool();
                    if (!StringUtils.equalsIgnoreCase(_poolAddress1, poolAddress)) {
                        continue;
                    }
                    String _tickLowerStr = rewardPoolTickRange.getTickLower();
                    String _tickUpperStr =  rewardPoolTickRange.getTickUpper();

                    if(StringUtils.isEmpty(_tickLowerStr) || StringUtils.isEmpty(_tickUpperStr)
                    || StringUtils.isEmpty(_tickLowerStr.trim()) ||   StringUtils.isEmpty(_tickUpperStr.trim())){
                        log.error("poolAddress: {} tickLower: {} or tickUpper {} is empty, ignore", poolAddress, _tickLowerStr, _tickUpperStr);
                        continue;
                    }

                    try{
                        BigInteger _tickLower = new BigInteger(_tickLowerStr);
                        BigInteger _tickUpper = new BigInteger(_tickUpperStr);

                        if(!XIntervalOverlap.areIntervalsOverlapping(new BigInteger[]{tickLower, tickUpper},new BigInteger[]{_tickLower, _tickUpper}))
                        {
                            continue;
                        }
                        return  true;
                    }
                    catch (Exception e)
                    {
                        log.error(" Error configuring tickLower, please reconfigure!\npoolAddress: {} tickLower: {} or tickUpper {}, ignore. Detail: ", poolAddress, _tickLowerStr, _tickUpperStr, e);
                    }

                }//for (int j = 0; j < rewardPoolTickRanges.size(); j++)
            }
        }

        return false;
    }


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
     *
     * @return poolAddress -> rewardRatio
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
        if (pool == null) {
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
        if (pool == null) {
            return rewardMap;
        }
        String fee = pool.getFee();
        pool.getRewardPoolTickRanges().forEach(rewardPoolTickRange -> rewardMap.put(rewardPoolTickRange.getId(), rewardPoolTickRange.getRewardRatio().multiply(new BigInteger(fee)).divide(new BigInteger("10000"))));
        return rewardMap;
    }
}
