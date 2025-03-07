package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.EthLogsParser;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.common.constant.PoolMapSingleton;
import com.taibiex.stakingservice.common.utils.XIntervalOverlap;
import com.taibiex.stakingservice.config.ContractsConfig;
import com.taibiex.stakingservice.dto.TickInfoDTO;
import com.taibiex.stakingservice.entity.EpochRewardConfig;
import com.taibiex.stakingservice.entity.RewardPool;
import com.taibiex.stakingservice.entity.RewardPoolTickRange;
import com.taibiex.stakingservice.repository.RewardPoolRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ObjectUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class RewardPoolService {

    @Value("${app.total-reward-amount}")
    private String totalRewardAmount;

    @Autowired
    RewardPoolRepository rewardPoolRepository;

    @Autowired
    private EpochRewardConfigService epochRewardConfigService;

    @Autowired
    ContractsConfig contractsConfig;

    @Autowired
    private Web3jUtils web3jUtils;

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
            List<RewardPoolTickRange> rewardPoolTickRanges = rewardPool.getRewardPoolTickRanges();

            if (null != _poolAddress) {
                _poolAddress = _poolAddress.trim();
            }
            //注意 pool address  是根据 token0, token1, fee 生成，所以不用单独再过滤fee
            if (StringUtils.equalsIgnoreCase(_poolAddress, poolAddress)) {
                for (int j = 0; j < rewardPoolTickRanges.size(); j++) {
                    RewardPoolTickRange rewardPoolTickRange = rewardPoolTickRanges.get(j);
                    String _poolAddress1 = rewardPoolTickRange.getPool();
                    if (!StringUtils.equalsIgnoreCase(_poolAddress1, poolAddress)) {
                        continue;
                    }
                    String _tickLowerStr = rewardPoolTickRange.getTickLower();
                    String _tickUpperStr = rewardPoolTickRange.getTickUpper();

                    if (StringUtils.isEmpty(_tickLowerStr) || StringUtils.isEmpty(_tickUpperStr)
                            || StringUtils.isEmpty(_tickLowerStr.trim()) || StringUtils.isEmpty(_tickUpperStr.trim())) {
                        log.error("poolAddress: {} tickLower: {} or tickUpper {} is empty, ignore", poolAddress, _tickLowerStr, _tickUpperStr);
                        continue;
                    }

                    try {
                        BigInteger _tickLower = new BigInteger(_tickLowerStr);
                        BigInteger _tickUpper = new BigInteger(_tickUpperStr);

                        if (!XIntervalOverlap.areIntervalsOverlapping(new BigInteger[]{tickLower, tickUpper}, new BigInteger[]{_tickLower, _tickUpper})) {
                            continue;
                        }
                        return true;
                    } catch (Exception e) {
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
     * 获取奖池内不同tick的奖励金额
     */
    public Map<Long, BigInteger> getPoolTickRewardRatioMap(String poolAddress) {
        Map<Long, BigInteger> rewardMap = new HashMap<>();
        RewardPool pool = rewardPoolRepository.findByPool(poolAddress);
        if (pool == null) {
            return rewardMap;
        }
        EpochRewardConfig epochRewardConfig = epochRewardConfigService.getEpochRewardConfig();
        BigInteger epochRewardAmount = new BigInteger(epochRewardConfig.getRewardAmount());
        String fee = pool.getFee();
        for (RewardPoolTickRange rewardPoolTickRange : pool.getRewardPoolTickRanges()) {
            BigInteger rewardAmount = epochRewardAmount
                    .multiply(new BigInteger(fee)).divide(new BigInteger("10000"))
                    .multiply(rewardPoolTickRange.getRewardRatio()).divide(new BigInteger("10000"));
            rewardMap.put(rewardPoolTickRange.getId(), rewardAmount);
        }
        return rewardMap;
    }


    /**
     * 该函数非常慢，不要用，或者拿到数据后存到数据库表中以便下次使用，建议还是通过扫块方式来处理
     * uniswap V3 获取 指定pool 所有管理tick的 NonfungiblePositionManager合约的所有 tokenId
     *
     * @param poolAddress
     * @return
     * @throws Exception
     */
    public List<TickInfoDTO> getAllTicksForPool(String poolAddress) {

        ContractsConfig.ContractInfo nonFungiblePositionManagerCI = contractsConfig.getContractInfo("NonfungiblePositionManager");

        Event nonFungiblePositionManagerIncreaseLiquidityEvent = new ContractsEventBuilder().build(ContractsEventEnum.INCREASE_LIQUIDITY);

        List<TickInfoDTO> tickInfos = new ArrayList<>();
        List<String> tokenIds = new ArrayList<>();

        //"maximum [from, to] blocks distance: 10000"
        int MAX_BLOCK_DISTANCES = 5000;
        BigInteger lastBlock = web3jUtils.getBlockNumber(0);
        // 获取所有与该池相关的流动性事件
        // 示例：分批请求
        for (int currentBlock = 0; currentBlock < lastBlock.longValue(); currentBlock += MAX_BLOCK_DISTANCES) {

            BigInteger fromBlock = BigInteger.valueOf(currentBlock);
            BigInteger toBlock = BigInteger.valueOf(currentBlock + MAX_BLOCK_DISTANCES);

            toBlock = toBlock.min(lastBlock);

            EthLog ethlog = null;
            while (true)
                try {
                    ethlog = web3jUtils.filterEthLog(fromBlock, toBlock, nonFungiblePositionManagerIncreaseLiquidityEvent, nonFungiblePositionManagerCI.getAddress());
                    break;
                } catch (IOException e) {
                    log.info("getAllTicksForPool filterEthLog retrying ... by exception: ", e);
                }

            //EthLog ethlog = web3jUtils.filterEthLog(DefaultBlockParameterName.EARLIEST, DefaultBlockParameterName.LATEST,nonFungiblePositionManagerIncreaseLiquidityEvent,nonFungiblePositionManagerCI.getAddress());
            if (!ObjectUtils.isEmpty(ethlog) && !ethlog.getLogs().isEmpty()) {
                for (EthLog.LogResult logResult : ethlog.getLogs()) {

                    Log evLog = (Log) logResult.get();

//                String contractAddress = evLog.getAddress().toLowerCase(); //合约地址
                    //Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.INCREASE_LIQUIDITY_DESC);
                    //List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
                    List<String> topics = evLog.getTopics();
                    String tokenId = EthLogsParser.hexToBigInteger(topics.get(1)).toString();
                    if (!tokenIds.contains(tokenId)) {
                        List<Type> poolDetails = web3jUtils.getUniswapV3PoolDetails(tokenId);
                        //String operator =poolDetails.get(1).getValue().toString();
                        String token0 = poolDetails.get(2).getValue().toString();
                        String token1 = poolDetails.get(3).getValue().toString();
                        Integer fee = Integer.valueOf(poolDetails.get(4).getValue().toString()); // 100 就是 100/10000 = 0.01就是1%
                        String _poolAddress = web3jUtils.getPoolAddress(token0, token1, fee);

                        if (!StringUtils.equalsIgnoreCase(_poolAddress, poolAddress)) {
                            continue;
                        }
                        BigInteger tickLower = (BigInteger) poolDetails.get(5).getValue();
                        BigInteger tickUpper = (BigInteger) poolDetails.get(6).getValue();
                        tokenIds.add(tokenId);
                        tickInfos.add(TickInfoDTO.builder().tickLower(tickLower).tickUpper(tickUpper).tokenId(tokenId).build());
                    }
                }
            }
        }

        return tickInfos;
    }

    /**
     * 获取指定tick对应的价格，Uniswap V3 的 tick 值通常在 -887272 到 887272 之间。超出该范围可能会导致计算错误。
     *
     * @param tick
     * @return
     */
    public Double getPriceFromTick(Integer tick) {
        return Math.pow(1.0001, tick);
    }


}
