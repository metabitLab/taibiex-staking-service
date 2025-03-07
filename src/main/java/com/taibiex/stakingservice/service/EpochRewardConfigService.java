package com.taibiex.stakingservice.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.taibiex.stakingservice.common.utils.RedisService;
import com.taibiex.stakingservice.entity.EpochRewardConfig;
import com.taibiex.stakingservice.repository.EpochRewardConfigRepository;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Log4j2
@Service
public class EpochRewardConfigService {

    private final RedisService redisService;

    private final EpochRewardConfigRepository epochRewardConfigRepository;

    public EpochRewardConfigService(RedisService redisService, EpochRewardConfigRepository epochRewardConfigRepository) {
        this.redisService = redisService;
        this.epochRewardConfigRepository = epochRewardConfigRepository;
    }

    public void save(EpochRewardConfig epochRewardConfig) {
        long epoch = epochRewardConfig.getEpoch();
        EpochRewardConfig rewardConfig = epochRewardConfigRepository.findByEpoch(epoch);
        if (rewardConfig != null){
            rewardConfig.setRewardAmount(epochRewardConfig.getRewardAmount());
            rewardConfig.setTokenSymbol(epochRewardConfig.getTokenSymbol());
            rewardConfig.setTokenAddress(epochRewardConfig.getTokenAddress());
            rewardConfig.setMainNet(epochRewardConfig.isMainNet());
            epochRewardConfigRepository.save(rewardConfig);
        } else
            epochRewardConfigRepository.save(epochRewardConfig);
    }

    public EpochRewardConfig getEpochRewardConfig() {
        return epochRewardConfigRepository.findFirstOrderByEpochDesc();
    }

    public EpochRewardConfig getEpochRewardConfigByEpoch(long epoch) {
        String key = "epoch_reward_config_" + epoch;
        try {
            Object redisValue = redisService.get(key);
            if (ObjectUtils.isNotEmpty(redisValue) && !"null".equals(redisValue.toString())) {
                String v = redisValue.toString();
                return JSONObject.parseObject(v, EpochRewardConfig.class);
            }
        }catch (Exception e){
            log.error("getEpochRewardConfigByEpoch redis read error", e.fillInStackTrace());
        }
        EpochRewardConfig byEpochLessThanEqualOrderByEpochDesc = epochRewardConfigRepository.findByEpochLessThanEqualOrderByEpochDesc(epoch);
        try {
            String pvoStr = JSON.toJSONString(byEpochLessThanEqualOrderByEpochDesc, SerializerFeature.WriteNullStringAsEmpty);
            redisService.set(pvoStr, pvoStr, 60, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.error("getEpochRewardConfigByEpoch redis write error：{}", e.getMessage());
        }
        return byEpochLessThanEqualOrderByEpochDesc;
    }

}
