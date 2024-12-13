package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.utils.EpochUtil;
import com.taibiex.stakingservice.entity.*;
import com.taibiex.stakingservice.repository.RewardPoolRepository;
import com.taibiex.stakingservice.repository.UserStakingInfoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

@Service
public class UserStakingInfoService {

    @Value("${epoch.unit}")
    private String epochUnit;

    private final UserStakingInfoRepository userStakingInfoRepository;

    private final RewardPoolRepository rewardPoolRepository;

    private final EpochUtil epochUtil;

    public UserStakingInfoService(UserStakingInfoRepository userStakingInfoRepository,
                                  RewardPoolRepository rewardPoolRepository,
                                  EpochUtil epochUtil) {
        this.userStakingInfoRepository = userStakingInfoRepository;
        this.rewardPoolRepository = rewardPoolRepository;
        this.epochUtil = epochUtil;
    }

    public void liquidityEventHandler(LiquidityEvent liquidityEvent) {
        String txHash = liquidityEvent.getTxHash();
        List<UserStakingInfo> userStakingInfos = userStakingInfoRepository.findByTxHash(txHash);
        if (!userStakingInfos.isEmpty()){
            return;
        }
        BigInteger eventAmount = new BigInteger(liquidityEvent.getAmount());
        BigInteger eventAmount0 = new BigInteger(liquidityEvent.getAmount0());
        BigInteger eventAmount1 = new BigInteger(liquidityEvent.getAmount1());
        long eventEpoch = epochUtil.getEpoch(liquidityEvent.getCreateTime().getTime());
        RewardPool pool = rewardPoolRepository.findByPool(liquidityEvent.getPool());
        long rewardPoolTickRangeId = 0;
        for (RewardPoolTickRange rewardPoolTickRange : pool.getRewardPoolTickRanges()) {
            if (rewardPoolTickRange.getTickLower().equals(liquidityEvent.getTickLower()) && rewardPoolTickRange.getTickUpper().equals(liquidityEvent.getTickUpper())){
                rewardPoolTickRangeId = rewardPoolTickRange.getId();
            }
        }
        UserStakingInfo lastUserStakingInfo = userStakingInfoRepository.findFirstByUserAddressAndRangeIdOrderByIdDesc(liquidityEvent.getOwner(), rewardPoolTickRangeId);
        if (lastUserStakingInfo != null){
            BigInteger totalAmount = new BigInteger(lastUserStakingInfo.getTotalAmount());
            BigInteger totalAmount0 = new BigInteger(lastUserStakingInfo.getTotalAmount0());
            BigInteger totalAmount1 = new BigInteger(lastUserStakingInfo.getTotalAmount1());
            long lastEventEpoch = lastUserStakingInfo.getEpoch();
            BigInteger stakingAmount = new BigInteger(lastUserStakingInfo.getStakingAmount());
            BigInteger epochTotalAmount;
            BigInteger epochTotalAmount0;
            BigInteger epochTotalAmount1;
            if (liquidityEvent.getType() == 1){
                epochTotalAmount = eventAmount.add(totalAmount);
                epochTotalAmount0 = eventAmount0.add(totalAmount0);
                epochTotalAmount1 = eventAmount1.add(totalAmount1);
            } else {
                epochTotalAmount = totalAmount.subtract(eventAmount);
                epochTotalAmount0 = totalAmount0.subtract(eventAmount0);
                epochTotalAmount1 = totalAmount1.subtract(eventAmount1);
            }
            if (eventEpoch > lastEventEpoch){
                long epochEndTime = epochUtil.getEpochEndTime(lastEventEpoch);
                BigInteger epochStakingAmount = totalAmount.multiply(BigInteger.valueOf(epochEndTime - lastUserStakingInfo.getCreateTime().getTime())).add(stakingAmount);
                UserStakingInfo stakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, lastEventEpoch, epochTotalAmount, epochTotalAmount0, epochTotalAmount1, epochStakingAmount);
                userStakingInfoRepository.save(stakingInfo);
                for (long i = lastEventEpoch + 1; i < eventEpoch; i++){
                    BigInteger totalStakingAmount = totalAmount.multiply(new BigInteger(epochUnit));
                    UserStakingInfo userStakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, i, epochTotalAmount, epochTotalAmount0, epochTotalAmount1, totalStakingAmount);
                    userStakingInfoRepository.save(userStakingInfo);
                }
                long epochStartTime = epochUtil.getEpochStartTime(eventEpoch);
                BigInteger totalStakingAmount = totalAmount.multiply(BigInteger.valueOf(liquidityEvent.getCreateTime().getTime() - epochStartTime));
                UserStakingInfo newUserStakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, eventEpoch, epochTotalAmount, epochTotalAmount0, epochTotalAmount1, totalStakingAmount);
                userStakingInfoRepository.save(newUserStakingInfo);
            } else if (eventEpoch == lastEventEpoch){
                BigInteger totalStakingAmount = totalAmount.multiply(BigInteger.valueOf(liquidityEvent.getCreateTime().getTime() - lastUserStakingInfo.getCreateTime().getTime())).add(stakingAmount);
                UserStakingInfo newUserStakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, eventEpoch, epochTotalAmount, epochTotalAmount0, epochTotalAmount1, totalStakingAmount);
                newUserStakingInfo.setCreateTime(liquidityEvent.getCreateTime());
                newUserStakingInfo.setLastUpdateTime(liquidityEvent.getLastUpdateTime());
                userStakingInfoRepository.save(newUserStakingInfo);
            }
        } else {
            UserStakingInfo newUserStakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, eventEpoch, eventAmount, eventAmount0, eventAmount1, BigInteger.ZERO);
            newUserStakingInfo.setCreateTime(liquidityEvent.getCreateTime());
            newUserStakingInfo.setLastUpdateTime(liquidityEvent.getLastUpdateTime());
            userStakingInfoRepository.save(newUserStakingInfo);
        }
    }

    private UserStakingInfo createUserStakingInfo(LiquidityEvent liquidityEvent, long rewardPoolTickRangeId, String txHash, long epoch, BigInteger totalAmount, BigInteger totalAmount0, BigInteger totalAmount1, BigInteger stakingAmount) {
        UserStakingInfo userStakingInfo = new UserStakingInfo();
        userStakingInfo.setStakingAmount(stakingAmount.toString());
        userStakingInfo.setTotalAmount(totalAmount.toString());
        userStakingInfo.setTotalAmount0(totalAmount0.toString());
        userStakingInfo.setTotalAmount1(totalAmount1.toString());
        userStakingInfo.setTxHash(txHash);
        userStakingInfo.setUserAddress(liquidityEvent.getOwner());
        userStakingInfo.setEpoch(epoch);
        userStakingInfo.setRangeId(rewardPoolTickRangeId);
        userStakingInfo.setPool(liquidityEvent.getPool());
        return userStakingInfo;
    }

    public List<UserStakingInfo> getUserStakingInfosByEpoch(long epoch, String userAddress, String pool) {
        long epochEndTime = epochUtil.getEpochEndTime(epoch);
        return getUserStakingInfosByEndTime(epochEndTime, userAddress, pool);
    }

    public List<UserStakingInfo> getUserStakingInfosByEndTime(long endTime, String userAddress, String pool) {
        long epoch = epochUtil.getEpoch(endTime);
        long epochStartTime = epochUtil.getEpochStartTime(epoch);
        List<UserStakingInfo> userStakingInfos = new ArrayList<>();
        List<UserStakingInfo> userStakingInfoList = userStakingInfoRepository.findNewestByRangeId(epoch, userAddress, pool);
        for (UserStakingInfo userStakingInfo : userStakingInfoList) {
            BigInteger totalAmount = new BigInteger(userStakingInfo.getTotalAmount());
            BigInteger totalAmount0 = new BigInteger(userStakingInfo.getTotalAmount0());
            BigInteger totalAmount1 = new BigInteger(userStakingInfo.getTotalAmount1());
            BigInteger stakingAmount = new BigInteger(userStakingInfo.getStakingAmount());
            long lastEpoch = userStakingInfo.getEpoch();
            if (lastEpoch < epoch){
                UserStakingInfo stakingInfo = new UserStakingInfo();
                stakingInfo.setId(userStakingInfo.getId());
                stakingInfo.setEpoch(epoch);
                stakingInfo.setPool(userStakingInfo.getPool());
                stakingInfo.setRangeId(userStakingInfo.getRangeId());
                stakingInfo.setTotalAmount(totalAmount.toString());
                stakingInfo.setTotalAmount0(totalAmount0.toString());
                stakingInfo.setTotalAmount1(totalAmount1.toString());
                stakingInfo.setStakingAmount(totalAmount.multiply(BigInteger.valueOf(endTime - epochStartTime)).toString());
                stakingInfo.setTxHash(userStakingInfo.getTxHash());
                stakingInfo.setUserAddress(userStakingInfo.getUserAddress());
                userStakingInfos.add(stakingInfo);
            } else if (lastEpoch == epoch){
                userStakingInfo.setStakingAmount(stakingAmount.add(totalAmount.multiply(BigInteger.valueOf(endTime - userStakingInfo.getCreateTime().getTime()))).toString());
                userStakingInfos.add(userStakingInfo);
            }
        }
        return userStakingInfos;
    }
}
