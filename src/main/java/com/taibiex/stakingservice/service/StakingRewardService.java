package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.utils.EpochUtil;
import com.taibiex.stakingservice.dto.LastLiquidityEvent;
import com.taibiex.stakingservice.dto.StakingAmountDTO;
import com.taibiex.stakingservice.dto.StakingAmountRatio;
import com.taibiex.stakingservice.dto.EpochTotalStakingAmount;
import com.taibiex.stakingservice.entity.*;
import com.taibiex.stakingservice.repository.LiquidityEventRepository;
import com.taibiex.stakingservice.repository.RewardPoolRepository;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.*;

@Service
@Log4j2
public class StakingRewardService {

    @Value("${epoch.startTimestamp}")
    private String startTimestamp;

    @Value("${app.total-reward-amount}")
    private String totalRewardAmount;

    private final RewardPoolRepository rewardPoolRepository;

    private final RewardPoolService rewardPoolService;

    private final LiquidityEventRepository liquidityEventRepository;

    private final EpochUtil epochUtil;

    public StakingRewardService(RewardPoolRepository rewardPoolRepository,
                                RewardPoolService rewardPoolService,
                                LiquidityEventRepository liquidityEventRepository,
                                EpochUtil epochUtil) {
        this.rewardPoolRepository = rewardPoolRepository;
        this.rewardPoolService = rewardPoolService;
        this.liquidityEventRepository = liquidityEventRepository;
        this.epochUtil = epochUtil;
    }

    /**
     * 获取用户奖励
     * @param userAddress 用户地址
     * @return 用户奖励
     */
    public List<StakingAmountDTO> getUserReward(String userAddress, String poolAddress) {
        //声明一个存储池内每个epoch的质押量
        Map<Long, StakingAmountDTO> totalAmountMap = new HashMap<>();
        //获取奖池内不同tick占总奖励的奖励比率, key为tickId，value为该tick的奖励比率
        Map<Long, BigInteger> poolTickRewardRatioMap = rewardPoolService.getPoolTickRewardRatioMap(poolAddress);
        //获取用户在奖池不同tick中的质押量占比列表，key为tickId，value为该epoch内用户在该奖池下的质押量
        Map<Long, List<StakingAmountRatio>> userTickRewardMap = getUserTickRewardMap(userAddress, poolAddress);
        for (Long l : poolTickRewardRatioMap.keySet()) {
            List<StakingAmountRatio> stakingAmountRatios = userTickRewardMap.get(l);
            for (StakingAmountRatio stakingAmountRatio : stakingAmountRatios) {
                long epoch = stakingAmountRatio.getEpoch();
                StakingAmountDTO rewardAmount = totalAmountMap.get(epoch);
                BigInteger val = poolTickRewardRatioMap.get(l);
                if (rewardAmount == null){
                    rewardAmount = new StakingAmountDTO();
                    rewardAmount.setEpoch(epoch);
                    rewardAmount.setAmount0(stakingAmountRatio.getAmount0());
                    rewardAmount.setAmount1(stakingAmountRatio.getAmount1());
                    rewardAmount.setRewardAmount(new BigInteger(totalRewardAmount).multiply(val).multiply(stakingAmountRatio.getStakingAmountRatio()).divide(new BigInteger("100000000")));
                    totalAmountMap.put(epoch, rewardAmount);
                } else {
                    rewardAmount.setAmount0(rewardAmount.getAmount0().add(stakingAmountRatio.getAmount0()));
                    rewardAmount.setAmount1(rewardAmount.getAmount1().add(stakingAmountRatio.getAmount1()));
                    rewardAmount.setRewardAmount(rewardAmount.getRewardAmount().add(new BigInteger(totalRewardAmount).multiply(val).multiply(stakingAmountRatio.getStakingAmountRatio()).divide(new BigInteger("100000000"))));
                }
            }
        }
        return totalAmountMap.values().stream().sorted(Comparator.comparingLong(StakingAmountDTO::getEpoch)).toList();
    }

    /**
     * 获取用户在奖池不同tick中的质押量占比
     * @param userAddress 用户地址
     * @param poolAddress 奖池地址
     *
     * @return Long, List<StakingAmountRatio>>
     * <tickRangeId, List<StakingAmountRatio>>
     */
    public Map<Long, List<StakingAmountRatio>> getUserTickRewardMap(String userAddress, String poolAddress) {
        RewardPool pool = rewardPoolRepository.findByPool(poolAddress);
        if (pool == null){
            return new HashMap<>();
        }
        List<RewardPoolTickRange> rewardPoolTickRanges = pool.getRewardPoolTickRanges();

        Map<Long, List<StakingAmountRatio>> rewardMap = new HashMap<>();
        for (RewardPoolTickRange rewardPoolTickRange : rewardPoolTickRanges) {
            String tickLower = rewardPoolTickRange.getTickLower();
            String tickUpper = rewardPoolTickRange.getTickUpper();
            Map<String, Map<String, List<LastLiquidityEvent>>> poolRewardMap = getPoolRewardMap(poolAddress, tickLower, rewardPoolTickRange.getTickUpper());
            Map<String, List<LastLiquidityEvent>> tickRewardMap = poolRewardMap.get(userAddress);
            if (tickRewardMap == null){
                rewardMap.put(rewardPoolTickRange.getId(), new ArrayList<>());
            }
            String tickKey = tickLower + "-" + rewardPoolTickRange.getTickUpper();

            List<LastLiquidityEvent> lastLiquidityEvents = tickRewardMap.get(tickKey);

            //获取poolAddress 对应tickLower - tickUpper 区间内，每个epoch的质押量
            Map<Long, EpochTotalStakingAmount> poolTickTotalAmount = getPoolTickTotalAmount(poolAddress, tickLower, tickUpper);
            List<StakingAmountRatio> stakingAmountRatioList = new ArrayList<>();
            for (LastLiquidityEvent lastLiquidityEvent : lastLiquidityEvents) {
                long epoch = lastLiquidityEvent.getEpoch();
                EpochTotalStakingAmount epochTotalStakingAmount = poolTickTotalAmount.get(epoch);
                if (epochTotalStakingAmount != null){
                    StakingAmountRatio stakingAmountRatio = new StakingAmountRatio();
                    stakingAmountRatio.setEpoch(epoch);
                    stakingAmountRatio.setStakingAmountRatio(epochTotalStakingAmount.getAmount().multiply(BigInteger.valueOf(10000)).divide(epochTotalStakingAmount.getTotalStakingAmount().multiply(BigInteger.valueOf(10000))));
                    stakingAmountRatioList.add(stakingAmountRatio);
                }
                rewardMap.put(rewardPoolTickRange.getId(), stakingAmountRatioList);
            }
        }
        return rewardMap;
    }

    /**
     * 计算奖池不同tick总的质押量
     *
     * epoch - TotalStakingAmount
     */
    public Map<Long, EpochTotalStakingAmount> getPoolTickTotalAmount(String poolAddress, String tickLower, String tickUpper) {
        long previousEpoch = epochUtil.getLastEpoch();
        long previousEpochEndTime = epochUtil.getLastEpochEndTime();

        List<LiquidityEvent> liquidises =
                liquidityEventRepository.findAllByPoolAndTickLowerAndTickUpperAndCreateTimeBeforeOrderByCreateTime(poolAddress, tickLower, tickUpper, new Timestamp(previousEpochEndTime));

        List<LiquidityEvent> liquidityEvents = new ArrayList<>();
        liquidityEvents.addAll(liquidises);
        liquidityEvents.sort(Comparator.comparing(LiquidityEvent::getCreateTime));
        Map<Long, EpochTotalStakingAmount> totalAmountMap = new HashMap<>();
        List<EpochTotalStakingAmount> epochTotalStakingAmountList = new ArrayList<>();

        for (LiquidityEvent liquidityEvent : liquidityEvents) {
//            burnEventTotalStakingHandler(liquidityEvent, totalAmountMap, epochTotalStakingAmountList);
        }
        EpochTotalStakingAmount epochTotalStakingAmount = totalAmountMap.get(previousEpoch);
        epochTotalStakingAmount.setTotalStakingAmount(epochTotalStakingAmount.getTotalStakingAmount()
                .add(epochTotalStakingAmount.getAmount().multiply(BigInteger.valueOf(previousEpochEndTime - epochTotalStakingAmount.getEventTime().getTime()))));
        return totalAmountMap;
    }

    //TODO:
    private void mintEventTotalStakingHandler(LiquidityEvent liquidityEvent,  Map<Long, EpochTotalStakingAmount> totalAmountMap, List<EpochTotalStakingAmount> epochTotalStakingAmountList) {
        BigInteger eventAmount = new BigInteger(liquidityEvent.getAmount0());
        Timestamp eventCreateTime = liquidityEvent.getCreateTime();
        // 事件epoch
        Long eventEpoch = epochUtil.getEpoch(eventCreateTime.getTime());
        EpochTotalStakingAmount epochTotalStakingAmount = totalAmountMap.get(eventEpoch);
        if (epochTotalStakingAmount == null) {
            epochTotalStakingAmount = new EpochTotalStakingAmount(eventEpoch, eventAmount, BigInteger.ZERO, eventCreateTime);
            epochTotalStakingAmountList.add(epochTotalStakingAmount);
            totalAmountMap.put(eventEpoch, epochTotalStakingAmount);
        } else {
            EpochTotalStakingAmount lastStakingAmount = epochTotalStakingAmountList.get(epochTotalStakingAmountList.size() - 1);
            BigInteger lastAmount = lastStakingAmount.getAmount();
            BigInteger totalStakingAmount = lastStakingAmount.getTotalStakingAmount();
            long lastEventEpoch = epochUtil.getEpoch(lastStakingAmount.getEventTime().getTime());
            long epochEndTime = epochUtil.getEpochEndTime(lastEventEpoch);
            long eventEpochStartTime = epochUtil.getEpochStartTime(eventEpoch);
            if (eventEpoch > lastEventEpoch) {
                epochTotalStakingAmount = new EpochTotalStakingAmount();
                epochTotalStakingAmount.setAmount(lastAmount.add(eventAmount));
                epochTotalStakingAmount.setTotalStakingAmount(totalStakingAmount.add(eventAmount.multiply(BigInteger.valueOf(epochEndTime - lastStakingAmount.getEventTime().getTime()))));
                for (long i = lastEventEpoch + 1; i < eventEpoch; i++) {
                    EpochTotalStakingAmount totalAmount = new EpochTotalStakingAmount();
                    totalAmount.setAmount(lastAmount.add(eventAmount));
                    totalAmount.setEpoch(i);
                    totalAmount.setEventTime(lastStakingAmount.getEventTime());
                    totalAmount.setTotalStakingAmount(eventAmount.multiply(BigInteger.valueOf(epochEndTime - lastStakingAmount.getEventTime().getTime())));
                    totalAmountMap.put(i, totalAmount);
                }
                EpochTotalStakingAmount totalAmount = new EpochTotalStakingAmount();
                totalAmount.setAmount(eventAmount.add(lastAmount));
                totalAmount.setTotalStakingAmount(lastAmount.multiply(BigInteger.valueOf(eventCreateTime.getTime() - eventEpochStartTime)));
                totalAmount.setEventTime(eventCreateTime);
                totalAmount.setEpoch(eventEpoch);
                epochTotalStakingAmountList.add(totalAmount);
                totalAmountMap.put(eventEpoch, epochTotalStakingAmount);
            }
        }
    }

    //TODO:
    private void burnEventTotalStakingHandler(LiquidityEvent liquidityEvent, LiquidityEvent liquidityChange, Map<Long, EpochTotalStakingAmount> totalAmountMap, List<EpochTotalStakingAmount> epochTotalStakingAmountList) {
        BigInteger eventAmount = new BigInteger(liquidityChange.getAmount0());
        Timestamp eventCreateTime = liquidityEvent.getCreateTime();
        // 事件epoch
        Long eventEpoch = epochUtil.getEpoch(eventCreateTime.getTime());
        EpochTotalStakingAmount epochTotalStakingAmount = totalAmountMap.get(eventEpoch);
        if (epochTotalStakingAmount == null) {
            epochTotalStakingAmount = new EpochTotalStakingAmount(eventEpoch, eventAmount, BigInteger.ZERO, eventCreateTime);
            epochTotalStakingAmountList.add(epochTotalStakingAmount);
            totalAmountMap.put(eventEpoch, epochTotalStakingAmount);
        } else {
            EpochTotalStakingAmount lastStakingAmount = epochTotalStakingAmountList.get(epochTotalStakingAmountList.size() - 1);
            BigInteger lastAmount = lastStakingAmount.getAmount();
            BigInteger totalStakingAmount = lastStakingAmount.getTotalStakingAmount();
            long lastEventEpoch = epochUtil.getEpoch(lastStakingAmount.getEventTime().getTime());
            long epochEndTime = epochUtil.getEpochEndTime(lastEventEpoch);
            long eventEpochStartTime = epochUtil.getEpochStartTime(eventEpoch);
            if (eventEpoch > lastEventEpoch) {
                epochTotalStakingAmount.setAmount(lastAmount.add(eventAmount));
                epochTotalStakingAmount.setTotalStakingAmount(totalStakingAmount.add(eventAmount.multiply(BigInteger.valueOf(epochEndTime - lastStakingAmount.getEventTime().getTime()))));
                for (long i = lastEventEpoch + 1; i < eventEpoch; i++) {
                    EpochTotalStakingAmount totalAmount = new EpochTotalStakingAmount();
                    totalAmount.setAmount(lastAmount.add(eventAmount));
                    totalAmount.setEpoch(i);
                    totalAmount.setEventTime(lastStakingAmount.getEventTime());
                    totalAmount.setTotalStakingAmount(eventAmount.multiply(BigInteger.valueOf(epochEndTime - lastStakingAmount.getEventTime().getTime())));
                    totalAmountMap.put(i, totalAmount);
                }
                EpochTotalStakingAmount totalAmount = new EpochTotalStakingAmount();
                totalAmount.setAmount(eventAmount.divide(lastAmount));
                totalAmount.setTotalStakingAmount(lastAmount.multiply(BigInteger.valueOf(eventCreateTime.getTime() - eventEpochStartTime)));
                totalAmount.setEventTime(eventCreateTime);
                totalAmount.setEpoch(eventEpoch);
                epochTotalStakingAmountList.add(totalAmount);
                totalAmountMap.put(eventEpoch, epochTotalStakingAmount);
            }
        }
    }

    /**
     * 获取奖池不同tick用户的质押量
     * @param poolAddress
     * @param tickLower
     * @param tickUpper
     *
     * <userAddress, <tickLower-tickUpper, List<LastLiquidityEvent>>>
     */
    public Map<String, Map<String, List<LastLiquidityEvent>>> getPoolRewardMap(String poolAddress, String tickLower, String tickUpper) {

        long lastEpoch = epochUtil.getLastEpoch();
        long lastEpochEndTime = epochUtil.getLastEpochEndTime();

        Map<String, Map<String, List<LastLiquidityEvent>>> rewardMap = new HashMap<>();

       List<LiquidityEvent> liquidises =
                liquidityEventRepository.findAllByPoolAndTickLowerAndTickUpperAndCreateTimeBeforeOrderByCreateTime(poolAddress, tickLower, tickUpper, new Timestamp(lastEpochEndTime));

        List<LiquidityEvent> liquidityEvents = new ArrayList<>();
        liquidityEvents.addAll(liquidises);
        liquidityEvents.sort(Comparator.comparing(LiquidityEvent::getCreateTime));

        liquidityEvents.forEach(liquidityEvent -> {
            //TODO:这里需处理mint和burn
            burnLiquidityEventHandler(poolAddress, tickLower, tickUpper, liquidityEvent, rewardMap);
        });

        //处理最后一次事件到上一期结束之间的质押量
        rewardMap.forEach((userAddress, userRewardMap) -> userRewardMap.forEach((tickLowerTickUpper, lastLiquidityEvents) -> {
            LastLiquidityEvent lastLiquidityEvent = lastLiquidityEvents.get(lastLiquidityEvents.size() - 1);
            if (lastLiquidityEvent.getEventTime().getTime() > lastEpochEndTime){
                long lastEventEpoch = lastLiquidityEvent.getEpoch();
                BigInteger lastEventAmount = lastLiquidityEvent.getAmount();
                BigInteger lastEventAmount0 = lastLiquidityEvent.getAmount0();
                BigInteger lastEventAmount1 = lastLiquidityEvent.getAmount1();
                long epochEndTime = epochUtil.getEpochEndTime(lastEventEpoch);
                if (lastEpochEndTime > lastEventEpoch){
                    lastLiquidityEvent.setStakingAmount(lastLiquidityEvent.getStakingAmount().add(lastEventAmount.multiply(BigInteger.valueOf(epochEndTime - lastLiquidityEvent.getEventTime().getTime()))));
                    for (long i = lastEventEpoch + 1; i <= lastEpoch; i++) {
                        LastLiquidityEvent e = new LastLiquidityEvent(i, poolAddress, lastEventAmount, lastLiquidityEvent.getEventTime());
                        e.setStakingAmount(lastEventAmount.multiply(epochUtil.getEpochUnit()));
                        e.setAmount(lastEventAmount);
                        e.setAmount0(lastEventAmount0);
                        e.setAmount1(lastEventAmount1);
                        lastLiquidityEvents.add(e);
                    }
                    LastLiquidityEvent e = new LastLiquidityEvent(lastEpoch, poolAddress, lastEventAmount, lastLiquidityEvent.getEventTime());
                    e.setStakingAmount(lastEventAmount.multiply(epochUtil.getEpochUnit()));
                    e.setAmount(lastEventAmount);
                    e.setAmount0(lastEventAmount0);
                    e.setAmount1(lastEventAmount1);
                    lastLiquidityEvents.add(e);
                } else if (lastEpoch == lastEventEpoch){
                    lastLiquidityEvent.setAmount(lastEventAmount);
                    lastLiquidityEvent.setAmount0(lastEventAmount0);
                    lastLiquidityEvent.setAmount1(lastEventAmount1);
                    lastLiquidityEvent.setStakingAmount(lastLiquidityEvent.getStakingAmount().add(lastEventAmount.multiply(BigInteger.valueOf(lastEpochEndTime - lastLiquidityEvent.getEventTime().getTime()))));
                }
            }
        }));
        return rewardMap;
    }

    private void mintAndIncreaseLiquidityEventHandler(String poolAddress, String tickLower, String tickUpper, LiquidityEvent mintAndIncreaseLiquidity, Map<String, Map<String, List<LastLiquidityEvent>>> rewardMap) {
        String userAddress = mintAndIncreaseLiquidity.getSender();
        BigInteger amount = new BigInteger(mintAndIncreaseLiquidity.getAmount());
        BigInteger amount0 = new BigInteger(mintAndIncreaseLiquidity.getAmount0());
        BigInteger amount1 = new BigInteger(mintAndIncreaseLiquidity.getAmount1());

        Timestamp createTime = mintAndIncreaseLiquidity.getCreateTime();
        // 事件epoch
        long epoch = epochUtil.getEpoch(createTime.getTime());
        String tickLowerTickUpper = tickLower + "-" + tickUpper;
        // 用户质押信息
        Map<String, List<LastLiquidityEvent>> userRewardMap = rewardMap.get(userAddress);

        if (userRewardMap == null){
            // 初始化用户质押信息
            //<epoch-tickLower-tickUpper, List<LastLiquidityEvent>>
            Map<String, List<LastLiquidityEvent>> lastLiquidityEventMap = new HashMap<>();
            LastLiquidityEvent event = new LastLiquidityEvent(epoch, poolAddress, amount, BigInteger.ZERO, createTime);
            event.setAmount0(amount0);
            event.setAmount1(amount1);
            lastLiquidityEventMap.put(tickLowerTickUpper, List.of(event));
            rewardMap.put(userAddress, lastLiquidityEventMap);
        } else {
            List<LastLiquidityEvent> lastLiquidityEvents = userRewardMap.get(tickLowerTickUpper);
            if (lastLiquidityEvents == null){
                lastLiquidityEvents = new ArrayList<>();
                LastLiquidityEvent e = new LastLiquidityEvent(epoch, poolAddress, amount, BigInteger.ZERO, createTime);
                e.setAmount0(amount0);
                e.setAmount1(amount1);
                lastLiquidityEvents.add(e);
                userRewardMap.put(tickLowerTickUpper, lastLiquidityEvents);
            } else {
                LastLiquidityEvent lastLiquidityEvent = lastLiquidityEvents.get(lastLiquidityEvents.size() - 1);
                long lastEventEpoch = lastLiquidityEvent.getEpoch();
                BigInteger lastEventAmount = lastLiquidityEvent.getAmount();
                BigInteger lastEventAmount0 = lastLiquidityEvent.getAmount0();
                BigInteger lastEventAmount1 = lastLiquidityEvent.getAmount1();
                long epochEndTime = epochUtil.getEpochEndTime(lastEventEpoch);
                long epochStartTime = epochUtil.getEpochStartTime(epoch);
                if (epoch > lastEventEpoch){
                    lastLiquidityEvent.setAmount(lastEventAmount.add(amount));
                    lastLiquidityEvent.setAmount0(lastEventAmount0.add(amount0));
                    lastLiquidityEvent.setAmount1(lastEventAmount1.add(amount1));
                    lastLiquidityEvent.setStakingAmount(lastLiquidityEvent.getStakingAmount().add(lastEventAmount.multiply(BigInteger.valueOf(epochEndTime - lastLiquidityEvent.getEventTime().getTime()))));
                    for (long i = lastEventEpoch + 1; i < epoch; i++) {
                        LastLiquidityEvent e = new LastLiquidityEvent(i, poolAddress, lastEventAmount, lastEventAmount.multiply(epochUtil.getEpochUnit()), createTime);
                        e.setAmount(lastEventAmount.add(amount));
                        e.setAmount0(lastEventAmount0.add(amount0));
                        e.setAmount1(lastEventAmount1.add(amount1));
                        lastLiquidityEvents.add(e);
                    }
                    LastLiquidityEvent e = new LastLiquidityEvent(epoch, poolAddress, lastEventAmount, createTime);
                    e.setStakingAmount(lastEventAmount.multiply(BigInteger.valueOf(createTime.getTime() - epochStartTime)));
                    e.setAmount(lastEventAmount.add(amount));
                    e.setAmount0(lastEventAmount0.add(amount0));
                    e.setAmount1(lastEventAmount1.add(amount1));
                    lastLiquidityEvents.add(e);
                } else if (epoch == lastEventEpoch){
                    lastLiquidityEvent.setAmount(lastEventAmount.add(amount));
                    lastLiquidityEvent.setAmount0(lastEventAmount0.add(amount0));
                    lastLiquidityEvent.setAmount1(lastEventAmount1.add(amount1));
                    lastLiquidityEvent.setStakingAmount(lastLiquidityEvent.getStakingAmount().add(lastEventAmount.multiply(BigInteger.valueOf(createTime.getTime() - lastLiquidityEvent.getEventTime().getTime()))));
                } else {
                    log.error("mint liquidity event handler error, txhash:{}", mintAndIncreaseLiquidity.getTxHash());
                }
            }
        }
    }

    private void burnLiquidityEventHandler(String poolAddress, String tickLower, String tickUpper, LiquidityEvent liquidityEvent, Map<String, Map<String, List<LastLiquidityEvent>>> rewardMap) {
        String userAddress = liquidityEvent.getSender();
        BigInteger eventAmount = new BigInteger(liquidityEvent.getAmount());
        BigInteger eventAmount0 = new BigInteger(liquidityEvent.getAmount0());
        BigInteger eventAmount1 = new BigInteger(liquidityEvent.getAmount1());

        Timestamp createTime = liquidityEvent.getCreateTime();
        // 事件epoch
        long epoch = epochUtil.getEpoch(createTime.getTime());
        String tickLowerTickUpper = tickLower + "-" + tickUpper;
        // 用户质押信息
        Map<String, List<LastLiquidityEvent>> userRewardMap = rewardMap.get(userAddress);

        List<LastLiquidityEvent> lastLiquidityEvents = userRewardMap.get(tickLowerTickUpper);

        LastLiquidityEvent lastLiquidityEvent = lastLiquidityEvents.get(lastLiquidityEvents.size() - 1);
        long lastEventEpoch = lastLiquidityEvent.getEpoch();
        BigInteger lastEventAmount = lastLiquidityEvent.getAmount();
        BigInteger lastEventAmount0 = lastLiquidityEvent.getAmount0();
        BigInteger lastEventAmount1 = lastLiquidityEvent.getAmount1();
        long epochEndTime = epochUtil.getEpochEndTime(lastEventEpoch);
        long epochStartTime = epochUtil.getEpochStartTime(epoch);
        if (epoch > lastEventEpoch){
            lastLiquidityEvent.setStakingAmount(lastLiquidityEvent.getStakingAmount().add(
                    lastEventAmount.multiply(BigInteger.valueOf(epochEndTime - lastLiquidityEvent.getEventTime().getTime()))));
            lastLiquidityEvent.setAmount(lastEventAmount.subtract(eventAmount));
            lastLiquidityEvent.setAmount0(lastEventAmount0.subtract(eventAmount0));
            lastLiquidityEvent.setAmount1(lastEventAmount1.subtract(eventAmount1));
            for (long i = lastEventEpoch + 1; i < epoch; i++) {
                LastLiquidityEvent e = new LastLiquidityEvent(i, poolAddress, lastEventAmount, createTime);
                e.setStakingAmount(lastEventAmount.multiply(epochUtil.getEpochUnit()));
                e.setAmount(lastEventAmount.subtract(eventAmount0));
                e.setAmount0(lastEventAmount0.subtract(eventAmount0));
                e.setAmount1(lastEventAmount1.subtract(eventAmount1));
                lastLiquidityEvents.add(e);
            }
            LastLiquidityEvent e = new LastLiquidityEvent(epoch, poolAddress, lastEventAmount, createTime);
            e.setStakingAmount(lastEventAmount.multiply(BigInteger.valueOf(createTime.getTime() - epochStartTime)));
            e.setAmount(lastEventAmount.subtract(eventAmount0));
            e.setAmount0(lastEventAmount0.subtract(eventAmount0));
            e.setAmount1(lastEventAmount1.subtract(eventAmount1));
            lastLiquidityEvents.add(e);
        } else if (epoch == lastEventEpoch){
            lastLiquidityEvent.setAmount(lastLiquidityEvent.getAmount().subtract(eventAmount0));
            lastLiquidityEvent.setAmount0(lastLiquidityEvent.getAmount0().subtract(eventAmount0));
            lastLiquidityEvent.setAmount1(lastLiquidityEvent.getAmount1().subtract(eventAmount1));
            lastLiquidityEvent.setStakingAmount(lastLiquidityEvent.getStakingAmount().add(lastEventAmount.multiply(BigInteger.valueOf(createTime.getTime() - lastLiquidityEvent.getEventTime().getTime()))));
        }
    }
}
