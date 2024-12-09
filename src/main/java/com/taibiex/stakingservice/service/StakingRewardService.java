package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.utils.EpochUtil;
import com.taibiex.stakingservice.dto.LastLiquidityEvent;
import com.taibiex.stakingservice.entity.BurnAndDecreaseLiquidity;
import com.taibiex.stakingservice.entity.LiquidityEvent;
import com.taibiex.stakingservice.entity.MintAndIncreaseLiquidity;
import com.taibiex.stakingservice.entity.RewardPool;
import com.taibiex.stakingservice.repository.BurnAndDecreaseLiquidityRepository;
import com.taibiex.stakingservice.repository.MintAndIncreaseLiquidityRepository;
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

    private final RewardPoolRepository rewardPoolRepository;

    private final MintAndIncreaseLiquidityRepository mintAndIncreaseLiquidityRepository;

    private final BurnAndDecreaseLiquidityRepository burnAndDecreaseLiquidityRepository;

    private final EpochUtil epochUtil;

    public StakingRewardService(RewardPoolRepository rewardPoolRepository,
                                MintAndIncreaseLiquidityRepository mintAndIncreaseLiquidityRepository,
                                BurnAndDecreaseLiquidityRepository burnAndDecreaseLiquidityRepository,
                                EpochUtil epochUtil) {
        this.rewardPoolRepository = rewardPoolRepository;
        this.mintAndIncreaseLiquidityRepository = mintAndIncreaseLiquidityRepository;
        this.burnAndDecreaseLiquidityRepository = burnAndDecreaseLiquidityRepository;
        this.epochUtil = epochUtil;
    }

    /**
     * 获取用户奖励
     * @param userAddress 用户地址
     * @return 用户奖励
     */
    public String getUserReward(String userAddress, String poolAddress) {

        List<MintAndIncreaseLiquidity> mintAndIncreaseLiquidities = mintAndIncreaseLiquidityRepository.findAllBySenderOrderByCreateTime(userAddress);

        List<BurnAndDecreaseLiquidity> burnAndDecreaseLiquidities = burnAndDecreaseLiquidityRepository.findAllBySenderOrderByCreateTime(userAddress);

        List<LiquidityEvent> liquidityEvents = new ArrayList<>();

        liquidityEvents.addAll(mintAndIncreaseLiquidities);

        liquidityEvents.addAll(burnAndDecreaseLiquidities);

        liquidityEvents.sort(Comparator.comparing(LiquidityEvent::getCreateTime));

        Map<String, String> epochRewardMap = new HashMap<>();

        liquidityEvents.forEach(liquidityEvent -> {
            if (liquidityEvent instanceof BurnAndDecreaseLiquidity)
            String poolAddress = liquidityEvent.getPool();
        });

        List<RewardPool> rewardPools = rewardPoolRepository.findAll();
        return rewardPoolRepository.findByPool(poolAddress).getReward();
    }

    /**
     * 获取奖池所有用户的质押量
     * @param poolAddress
     * @return
     * userAddress, amount
     */
    public Map<String, String> getPoolRewardMap(String poolAddress, long epoch) {
        long currentEpoch = epochUtil.getCurrentEpoch();
        Map<String, String> rewardMap = new HashMap<>();
        if (epoch >= currentEpoch){
            return rewardMap;
        }
        long epochStartTime = epochUtil.getEpochStartTime(epoch);
        long epochEndTime = epochUtil.getEpochEndTime(epoch);
        List<MintAndIncreaseLiquidity> mintAndIncreaseLiquidises = mintAndIncreaseLiquidityRepository.findAllByPoolAndCreateTimeBetween(poolAddress, epochStartTime, epochEndTime);
        List<BurnAndDecreaseLiquidity> burnAndDecreaseLiquidises = burnAndDecreaseLiquidityRepository.findAllByPoolAndCreateTimeBetween(poolAddress, epochStartTime, epochEndTime);
        List<LiquidityEvent> liquidityEvents = new ArrayList<>();
        liquidityEvents.addAll(mintAndIncreaseLiquidises);
        liquidityEvents.addAll(burnAndDecreaseLiquidises);
        liquidityEvents.sort(Comparator.comparing(LiquidityEvent::getCreateTime));
        liquidityEvents.forEach(liquidityEvent -> {
            if (liquidityEvent instanceof BurnAndDecreaseLiquidity){
                BurnAndDecreaseLiquidity burnAndDecreaseLiquidity = (BurnAndDecreaseLiquidity) liquidityEvent;
                String tokenId = burnAndDecreaseLiquidity.getTokenId();
                String userAddress = burnAndDecreaseLiquidity.getOwner();
                String amount = burnAndDecreaseLiquidity.getAmount();
                String reward = rewardMap.get(userAddress);
                if (reward == null){
                    rewardMap.put(userAddress, amount);
                }else {
                    rewardMap.put(userAddress, reward.add(amount));
                }
            }
        });

    }

    /**
     * 获取奖池不同tick所有用户的质押量
     * @param poolAddress
     * @param tickLower
     * @param tickUpper
     *
     * <userAddress, <tickLower-tickUpper, List<LastLiquidityEvent>>>
     */
    public Map<String, Map<String, LastLiquidityEvent>> getPoolRewardMap(String poolAddress, String tickLower, String tickUpper) {

        Map<String, Map<String, List<LastLiquidityEvent>>> rewardMap = new HashMap<>();

        List<MintAndIncreaseLiquidity> mintAndIncreaseLiquidises = mintAndIncreaseLiquidityRepository.findAllByPoolAndTickLowerAndTickUpperOrderByCreateTime(poolAddress, tickLower, tickUpper);
        List<BurnAndDecreaseLiquidity> burnAndDecreaseLiquidises = burnAndDecreaseLiquidityRepository.findAllByPoolAndTickLowerAndTickUpperOrderByCreateTime(poolAddress, tickLower, tickUpper);

        List<LiquidityEvent> liquidityEvents = new ArrayList<>();
        liquidityEvents.addAll(mintAndIncreaseLiquidises);
        liquidityEvents.addAll(burnAndDecreaseLiquidises);
        liquidityEvents.sort(Comparator.comparing(LiquidityEvent::getCreateTime));

        liquidityEvents.forEach(liquidityEvent -> {
            if (liquidityEvent instanceof MintAndIncreaseLiquidity mintAndIncreaseLiquidity){
                String userAddress = mintAndIncreaseLiquidity.getSender();
                BigInteger amount0 = new BigInteger(mintAndIncreaseLiquidity.getAmount0());
                BigInteger amount1 = new BigInteger(mintAndIncreaseLiquidity.getAmount1());

                Timestamp createTime = mintAndIncreaseLiquidity.getCreateTime();
                // 事件epoch
                long epoch = epochUtil.getEpoch(createTime.getTime());

                Map<String, List<LastLiquidityEvent>> userRewardMap = rewardMap.get(userAddress); // 用户质押信息

                String tickLowerTickUpper = tickLower + "-" + tickUpper;
                if (userRewardMap == null){
                    // 初始化用户质押信息
                    Map<String, List<LastLiquidityEvent>> lastLiquidityEventMap = new HashMap<>(); //<epoch-tickLower-tickUpper, List<LastLiquidityEvent>>
                    lastLiquidityEventMap.put(tickLowerTickUpper, List.of(new LastLiquidityEvent(epoch, poolAddress, amount0, amount1, createTime)));
                    rewardMap.put(userAddress, lastLiquidityEventMap);
                } else {
                    List<LastLiquidityEvent> lastLiquidityEvents = userRewardMap.get(tickLowerTickUpper);
                    if (lastLiquidityEvents == null){
                        lastLiquidityEvents = new ArrayList<>();
                        lastLiquidityEvents.add(new LastLiquidityEvent(epoch, poolAddress, BigInteger.ZERO, BigInteger.ZERO, createTime));
                    } else {
                        LastLiquidityEvent lastLiquidityEvent = lastLiquidityEvents.get(lastLiquidityEvents.size() - 1);
                        long lastEpoch = lastLiquidityEvent.getEpoch();
                        if (epoch > lastEpoch){
                            long epochEndTime = epochUtil.getEpochEndTime(epoch);
                            lastLiquidityEvent.setStakingAmount0(lastLiquidityEvent.getStakingAmount0().add(amount0.multiply(BigInteger.valueOf(epochEndTime - lastLiquidityEvent.getEventTime().getTime()))));
                            lastLiquidityEvent.setStakingAmount1(lastLiquidityEvent.getStakingAmount1().add(amount1.multiply(BigInteger.valueOf(epochEndTime - lastLiquidityEvent.getEventTime().getTime()))));
                            for (long i = epoch + 1; i < lastEpoch; i++) {
                                lastLiquidityEvents.add(new LastLiquidityEvent(i, poolAddress, BigInteger.ZERO, BigInteger.ZERO, new Timestamp(epochUtil.getEpochEndTime(i)))
                                )
                            }
                        }
                    }
                }
            } else if (liquidityEvent instanceof BurnAndDecreaseLiquidity){
                BurnAndDecreaseLiquidity burnAndDecreaseLiquidity = (BurnAndDecreaseLiquidity) liquidityEvent;
                String userAddress = burnAndDecreaseLiquidity.getOwner();
                String amount = burnAndDecreaseLiquidity.getAmount();
                Timestamp createTime = burnAndDecreaseLiquidity.getCreateTime();
            }
        });
    }
}
