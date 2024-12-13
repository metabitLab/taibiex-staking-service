package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.utils.EpochUtil;
import com.taibiex.stakingservice.common.utils.RedisService;
import com.taibiex.stakingservice.entity.*;
import com.taibiex.stakingservice.repository.ActivityUserRepository;
import com.taibiex.stakingservice.repository.UserPoolRewardRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class UserPoolRewardService {

    private static final String USER_POOL_REWARD_LOCK_KEY = "USER_POOL_REWARD_TASK";

    private final RedisService redisService;

    private final EpochUtil epochUtil;

    private final UserPoolRewardRepository userPoolRewardRepository;

    private final EpochRewardConfigService epochRewardConfigService;

    public final ActivityUserRepository activityUserRepository;

    public final TickRangeStakingInfoService tickRangeStakingInfoService;

    private final UserStakingInfoService userStakingInfoService;

    public final RewardPoolService rewardPoolService;

    public UserPoolRewardService(RedisService redisService, EpochUtil epochUtil,
                                 UserPoolRewardRepository userPoolRewardRepository,
                                 EpochRewardConfigService epochRewardConfigService,
                                 ActivityUserRepository activityUserRepository,
                                 TickRangeStakingInfoService tickRangeStakingInfoService,
                                 UserStakingInfoService userStakingInfoService,
                                 RewardPoolService rewardPoolService) {
        this.redisService = redisService;
        this.epochUtil = epochUtil;
        this.userPoolRewardRepository = userPoolRewardRepository;
        this.epochRewardConfigService = epochRewardConfigService;
        this.activityUserRepository = activityUserRepository;
        this.tickRangeStakingInfoService = tickRangeStakingInfoService;
        this.userStakingInfoService = userStakingInfoService;
        this.rewardPoolService = rewardPoolService;
    }

    @Scheduled(fixedDelayString = "60000")
    public void generateUserPoolReward() {
        try {
            if (!redisService.setNx(USER_POOL_REWARD_LOCK_KEY, USER_POOL_REWARD_LOCK_KEY + System.currentTimeMillis())) {
                log.warn("UserPoolRewardService generateUserPoolReward locked");
                return;
            }
            long lastEpoch = epochUtil.getLastEpoch();
            List<UserPoolReward> userPoolRewards = userPoolRewardRepository.findByEpoch(lastEpoch);
            if (!userPoolRewards.isEmpty()) {
                return;
            }
            EpochRewardConfig epochRewardConfig = epochRewardConfigService.getEpochRewardConfig();
            List<RewardPool> rewardPools = rewardPoolService.findAll();
            List<ActivityUser> allUser = activityUserRepository.findAll();
            for (ActivityUser activityUser : allUser) {
                String userAddress = activityUser.getUserAddress();

                for (RewardPool rewardPool : rewardPools) {
                    BigInteger totalAmount = BigInteger.ZERO;
                    BigInteger rewardAmount = BigInteger.ZERO;
                    BigInteger totalAmount0 = BigInteger.ZERO;
                    BigInteger totalAmount1 = BigInteger.ZERO;
                    BigInteger totalStakingAmount = BigInteger.ZERO;
                    String pool = rewardPool.getPool();
                    Map<Long, BigInteger> poolTickRewardRatioMap = rewardPoolService.getPoolTickRewardRatioMap(pool);
                    Map<Long, TickRangeStakingInfo> stakingInfoMapByEpoch = tickRangeStakingInfoService.getTickRangeStakingInfoMapByEpoch(lastEpoch, pool);
                    List<UserStakingInfo> userStakingInfos = userStakingInfoService.getUserStakingInfosByEpoch(lastEpoch, userAddress, pool);
                    for (UserStakingInfo userStakingInfo : userStakingInfos) {
                        String userStakingAmount = userStakingInfo.getStakingAmount();
                        totalStakingAmount = totalStakingAmount.add(new BigInteger(userStakingAmount));
                        BigInteger reward = poolTickRewardRatioMap.get(userStakingInfo.getRangeId());
                        TickRangeStakingInfo tickRangeStakingInfo = stakingInfoMapByEpoch.get(userStakingInfo.getRangeId());
                        if (tickRangeStakingInfo == null) {
                            continue;
                        }
                        BigInteger tickTotalStakingAmount = new BigInteger(tickRangeStakingInfo.getStakingAmount());
                        if (tickTotalStakingAmount.compareTo(BigInteger.ZERO) == 0){
                            rewardAmount = BigInteger.ZERO;
                        } else {
                            rewardAmount = rewardAmount.add(reward.multiply(totalStakingAmount).divide(tickTotalStakingAmount));
                        }
                        totalAmount = totalAmount.add(new BigInteger(tickRangeStakingInfo.getTotalAmount()));
                        totalAmount0 = totalAmount0.add(new BigInteger(tickRangeStakingInfo.getTotalAmount0()));
                        totalAmount1 = totalAmount1.add(new BigInteger(tickRangeStakingInfo.getTotalAmount1()));
                    }
                    UserPoolReward userPoolReward = new UserPoolReward();
                    userPoolReward.setEpoch(lastEpoch);
                    userPoolReward.setUserAddress(userAddress);
                    userPoolReward.setPool(pool);
                    userPoolReward.setRewardAmount(rewardAmount.toString());
                    userPoolReward.setLp(totalAmount.toString());
                    userPoolReward.setClaimed(false);
                    userPoolReward.setTokenAddress("1");
                    userPoolReward.setTokenSymbol("1");
                    userPoolReward.setTokenSymbol(epochRewardConfig.getTokenSymbol());
                    userPoolReward.setTokenAddress(epochRewardConfig.getTokenAddress());
                    userPoolReward.setMainNet(epochRewardConfig.isMainNet());
                    userPoolRewardRepository.save(userPoolReward);
                }
            }
        } catch (Exception e) {
            log.error("UserPoolRewardService generateUserPoolReward error", e);
        } finally {
            redisService.del(USER_POOL_REWARD_LOCK_KEY);
        }
    }

    public List<UserPoolReward> getUserPoolRewards(String userAddress) {
        return userPoolRewardRepository.findByUserAddressOrderByEpochDesc(userAddress);
    }

}
