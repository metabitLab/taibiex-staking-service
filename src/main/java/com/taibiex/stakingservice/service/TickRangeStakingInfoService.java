package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.utils.EpochUtil;
import com.taibiex.stakingservice.entity.*;
import com.taibiex.stakingservice.repository.RewardPoolRepository;
import com.taibiex.stakingservice.repository.TickRangeStakingInfoRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Service
public class TickRangeStakingInfoService {

    @Value("${epoch.unit}")
    private String epochUnit;

    private final EpochUtil epochUtil;

    private final TickRangeStakingInfoRepository tickRangeStakingInfoRepository;

    private final RewardPoolRepository rewardPoolRepository;

    public TickRangeStakingInfoService(EpochUtil epochUtil,
                                       TickRangeStakingInfoRepository tickRangeStakingInfoRepository,
                                       RewardPoolRepository rewardPoolRepository) {
        this.epochUtil = epochUtil;
        this.tickRangeStakingInfoRepository = tickRangeStakingInfoRepository;
        this.rewardPoolRepository = rewardPoolRepository;
    }

    public void liquidityEventHandler(LiquidityEvent liquidityEvent) {
        String txHash = liquidityEvent.getTxHash();
        List<TickRangeStakingInfo> tickRangeStakingInfos = tickRangeStakingInfoRepository.findByTxHash(txHash);
        if (!tickRangeStakingInfos.isEmpty()){
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
        TickRangeStakingInfo lastTickRangeStakingINfo = tickRangeStakingInfoRepository.findFirstByPoolAndRangeIdOrderByIdDesc(liquidityEvent.getPool(), rewardPoolTickRangeId);
        if (lastTickRangeStakingINfo != null){
            BigInteger totalAmount = new BigInteger(lastTickRangeStakingINfo.getTotalAmount());
            BigInteger totalAmount0 = new BigInteger(lastTickRangeStakingINfo.getTotalAmount0());
            BigInteger totalAmount1 = new BigInteger(lastTickRangeStakingINfo.getTotalAmount1());
            long lastEventEpoch = lastTickRangeStakingINfo.getEpoch();
            BigInteger stakingAmount = new BigInteger(lastTickRangeStakingINfo.getStakingAmount());
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
                BigInteger epochStakingAmount = totalAmount.multiply(BigInteger.valueOf(epochEndTime - lastTickRangeStakingINfo.getCreateTime().getTime())).add(stakingAmount);
                TickRangeStakingInfo stakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, lastEventEpoch, epochTotalAmount, epochTotalAmount0, epochTotalAmount1, epochStakingAmount);
                tickRangeStakingInfoRepository.save(stakingInfo);
                for (long i = lastEventEpoch + 1; i < eventEpoch; i++){
                    BigInteger totalStakingAmount = totalAmount.multiply(new BigInteger(epochUnit));
                    TickRangeStakingInfo tickRangeStakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, i, epochTotalAmount, epochTotalAmount0, epochTotalAmount1, totalStakingAmount);
                    tickRangeStakingInfoRepository.save(tickRangeStakingInfo);
                }
                long epochStartTime = epochUtil.getEpochStartTime(eventEpoch);
                BigInteger totalStakingAmount = totalAmount.multiply(BigInteger.valueOf(liquidityEvent.getCreateTime().getTime() - epochStartTime));
                TickRangeStakingInfo newUserStakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, eventEpoch, epochTotalAmount, epochTotalAmount0, epochTotalAmount1, totalStakingAmount);
                tickRangeStakingInfoRepository.save(newUserStakingInfo);
            } else if (eventEpoch == lastEventEpoch){
                BigInteger totalStakingAmount = totalAmount.multiply(BigInteger.valueOf(liquidityEvent.getCreateTime().getTime() - lastTickRangeStakingINfo.getCreateTime().getTime())).add(stakingAmount);
                TickRangeStakingInfo newUserStakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, eventEpoch, epochTotalAmount, epochTotalAmount0, epochTotalAmount1, totalStakingAmount);
                newUserStakingInfo.setCreateTime(liquidityEvent.getCreateTime());
                newUserStakingInfo.setLastUpdateTime(liquidityEvent.getLastUpdateTime());
                tickRangeStakingInfoRepository.save(newUserStakingInfo);
            }
        } else {
            TickRangeStakingInfo tickRangeStakingInfo = createUserStakingInfo(liquidityEvent, rewardPoolTickRangeId, txHash, eventEpoch, eventAmount, eventAmount0, eventAmount1, BigInteger.ZERO);
            tickRangeStakingInfo.setCreateTime(liquidityEvent.getCreateTime());
            tickRangeStakingInfo.setLastUpdateTime(liquidityEvent.getLastUpdateTime());
            tickRangeStakingInfoRepository.save(tickRangeStakingInfo);
        }
    }

    private TickRangeStakingInfo createUserStakingInfo(LiquidityEvent liquidityEvent, long rewardPoolTickRangeId, String txHash, long epoch, BigInteger totalAmount, BigInteger totalAmount0, BigInteger totalAmount1, BigInteger stakingAmount) {
        TickRangeStakingInfo tickRangeStakingInfo = new TickRangeStakingInfo();
        tickRangeStakingInfo.setStakingAmount(stakingAmount.toString());
        tickRangeStakingInfo.setTotalAmount(totalAmount.toString());
        tickRangeStakingInfo.setTotalAmount0(totalAmount0.toString());
        tickRangeStakingInfo.setTotalAmount1(totalAmount1.toString());
        tickRangeStakingInfo.setTxHash(txHash);
        tickRangeStakingInfo.setEpoch(epoch);
        tickRangeStakingInfo.setRangeId(rewardPoolTickRangeId);
        tickRangeStakingInfo.setPool(liquidityEvent.getPool());
        return tickRangeStakingInfo;
    }

    public List<TickRangeStakingInfo> getTickRangeStakingInfosByEpoch(long epoch, String pool) {
        long epochEndTime = epochUtil.getEpochEndTime(epoch);
        return getTickRangeStakingInfosByEndTime(epochEndTime, pool);
    }

    public Map<Long, TickRangeStakingInfo> getTickRangeStakingInfoMapByEpoch(long epoch, String pool) {
        List<TickRangeStakingInfo> tickRangeStakingInfoList = getTickRangeStakingInfosByEpoch(epoch, pool);
        Map<Long, TickRangeStakingInfo> map = new HashMap<>();
        for (TickRangeStakingInfo tickRangeStakingInfo : tickRangeStakingInfoList) {
            map.put(tickRangeStakingInfo.getRangeId(), tickRangeStakingInfo);
        }
        return map;
    }

    public List<TickRangeStakingInfo> getTickRangeStakingInfosByEndTime(long endTime, String pool) {
        long epoch = epochUtil.getEpoch(endTime);
        long epochStartTime = epochUtil.getEpochStartTime(epoch);
        List<TickRangeStakingInfo> tickRangeStakingInfoList = new ArrayList<>();
        List<TickRangeStakingInfo> rangeStakingInfos = tickRangeStakingInfoRepository.findNewestByEpoch(epoch, pool);
        for (TickRangeStakingInfo rangeStakingInfo : rangeStakingInfos) {
            BigInteger totalAmount = new BigInteger(rangeStakingInfo.getTotalAmount());
            BigInteger totalAmount0 = new BigInteger(rangeStakingInfo.getTotalAmount0());
            BigInteger totalAmount1 = new BigInteger(rangeStakingInfo.getTotalAmount1());
            BigInteger stakingAmount = new BigInteger(rangeStakingInfo.getStakingAmount());
            long lastEpoch = rangeStakingInfo.getEpoch();
            if (lastEpoch < epoch){
                TickRangeStakingInfo tickRangeStakingInfo = new TickRangeStakingInfo();
                tickRangeStakingInfo.setId(rangeStakingInfo.getId());
                tickRangeStakingInfo.setEpoch(epoch);
                tickRangeStakingInfo.setPool(rangeStakingInfo.getPool());
                tickRangeStakingInfo.setRangeId(rangeStakingInfo.getRangeId());
                tickRangeStakingInfo.setTotalAmount(totalAmount.toString());
                tickRangeStakingInfo.setTotalAmount0(totalAmount0.toString());
                tickRangeStakingInfo.setTotalAmount1(totalAmount1.toString());
                tickRangeStakingInfo.setStakingAmount(totalAmount.multiply(BigInteger.valueOf(endTime - epochStartTime)).toString());
                tickRangeStakingInfo.setTxHash(rangeStakingInfo.getTxHash());
                tickRangeStakingInfoList.add(tickRangeStakingInfo);
            } else if (lastEpoch == epoch){
                rangeStakingInfo.setStakingAmount(stakingAmount.add(totalAmount.multiply(BigInteger.valueOf(endTime - rangeStakingInfo.getCreateTime().getTime()))).toString());
                tickRangeStakingInfoList.add(rangeStakingInfo);
            }
        }
        return tickRangeStakingInfoList;
    }
}
