package com.taibiex.stakingservice.common.chain.contract.listener.impl;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.consumer.*;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.common.constant.PoolMapSingleton;
import com.taibiex.stakingservice.config.ContractsConfig;
import com.taibiex.stakingservice.entity.ContractOffset;
import com.taibiex.stakingservice.entity.PoolCreated;
import com.taibiex.stakingservice.entity.RewardPool;
import com.taibiex.stakingservice.service.ContractOffsetService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindException;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.crypto.Keys;
import org.web3j.protocol.core.methods.response.EthLog;
import org.web3j.protocol.core.methods.response.Log;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BlockEventListener {

    public static Logger logger = LoggerFactory.getLogger(BlockEventListener.class);

    private final static BigInteger STEP = new BigInteger("10");

    public static final String BLOCK_CONTRACT_FLAG = "BLOCK_CONTRACT_FLAG";

    @Autowired
    ContractsConfig contractsConfig;

    @Autowired
    private Web3jUtils web3jUtils;

    @Autowired
    private ContractOffsetService contractOffsetService;

    @Value("${contracts.start}")
    public String scannerContractStart;

    private Map<String, Event> topicAndContractAddr2EventMap = new HashMap<>();

    private Map<String, Method> topicAndContractAddr2CallBackMap = new HashMap<>();


    @Value("${contracts.enabled}")
    private boolean enabled;


    public void start(Integer delayBlocks, Set<String> enablesTaskNames, Set<String> disableTaskNames) throws InterruptedException, NoSuchMethodException {
        if (ObjectUtils.isEmpty(delayBlocks)) {
            delayBlocks = 0;
        }

        if (!enabled) {
            log.info("Delay" + delayBlocks + "_" + "BlockEventListener is disabled! ........");
            return;
        }
        logger.info("Delay" + delayBlocks + "_" + "BlockEventListener start");
        initialize(enablesTaskNames, disableTaskNames);
        blocksEventScanner(delayBlocks);
        logger.info("Delay" + delayBlocks + "_" + "BlockEventListener end");
    }

    private boolean isTaskEnable(Set<String> enablesTaskNames, Set<String> disableTaskNames, String curTaskName) {
        curTaskName = curTaskName.toLowerCase();

        boolean disableTaskNamesIsNull = ObjectUtils.isEmpty(disableTaskNames);
        boolean enablesTaskNamesIsNull = ObjectUtils.isEmpty(enablesTaskNames);

        if (disableTaskNamesIsNull && enablesTaskNamesIsNull) {
            return true;
        }
        else if (!disableTaskNamesIsNull && !enablesTaskNamesIsNull) {
            return true;
        } else if (!disableTaskNamesIsNull && !disableTaskNames.contains(curTaskName)) {
            return true;
        } else if (!enablesTaskNamesIsNull && enablesTaskNames.contains(curTaskName)) {
            return true;
        }
        return false;

    }

    public void initialize(Set<String> enablesTaskNames, Set<String> disableTaskNames) throws NoSuchMethodException {
        if (ObjectUtils.isEmpty(enablesTaskNames)) {
            enablesTaskNames = new HashSet<>();
        } else {
            enablesTaskNames = enablesTaskNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
        }

        if (ObjectUtils.isEmpty(disableTaskNames)) {
            disableTaskNames = new HashSet<>();
        } else {
            disableTaskNames = disableTaskNames.stream().map(String::toLowerCase).collect(Collectors.toSet());
        }


        topicAndContractAddr2EventMap.clear();
        topicAndContractAddr2CallBackMap.clear();

        ContractsConfig.ContractInfo stakingPool = contractsConfig.getContractInfo("StakingPool");

        if (isTaskEnable(enablesTaskNames, disableTaskNames, stakingPool.getName()) && stakingPool.getEnabled()) {

            Event stakingEvent = new ContractsEventBuilder().build(ContractsEventEnum.STAKING);
            String topicEventStaking = EventEncoder.encode(stakingEvent).toLowerCase();
            topicAndContractAddr2EventMap.put(topicEventStaking + "_" + stakingPool.getAddress(), stakingEvent);
            topicAndContractAddr2CallBackMap.put(topicEventStaking + "_" + stakingPool.getAddress(), StakingEventHandler.class.getMethod("descStakingEvent", Log.class));

            Event unStakingEvent = new ContractsEventBuilder().build(ContractsEventEnum.UN_STAKING);
            String topicEventUnStaking = EventEncoder.encode(unStakingEvent).toLowerCase();
            topicAndContractAddr2EventMap.put(topicEventUnStaking + "_" + stakingPool.getAddress(), unStakingEvent);
            topicAndContractAddr2CallBackMap.put(topicEventUnStaking + "_" + stakingPool.getAddress(), StakingEventHandler.class.getMethod("descUnStakingEvent", Log.class));

            Event claimEvent = new ContractsEventBuilder().build(ContractsEventEnum.CLAIM);
            String topicEventClaim = EventEncoder.encode(claimEvent).toLowerCase();
            topicAndContractAddr2EventMap.put(topicEventClaim + "_" + stakingPool.getAddress(), claimEvent);
            topicAndContractAddr2CallBackMap.put(topicEventClaim + "_" + stakingPool.getAddress(), ClaimEventHandler.class.getMethod("descClaimEvent", Log.class));

            Event claimIndexEvent = new ContractsEventBuilder().build(ContractsEventEnum.CLAIM_INDEX);
            String topicEventClaimIndex = EventEncoder.encode(claimIndexEvent).toLowerCase();
            topicAndContractAddr2EventMap.put(topicEventClaimIndex + "_" + stakingPool.getAddress(), claimIndexEvent);
            topicAndContractAddr2CallBackMap.put(topicEventClaimIndex + "_" + stakingPool.getAddress(), ClaimEventHandler.class.getMethod("descClaimIndexEvent", Log.class));

        }


        Map<String, RewardPool> sharedMap = PoolMapSingleton.getSharedMap();
        for (Map.Entry<String, RewardPool> entry : sharedMap.entrySet()) {

            String poolAddress = entry.getKey();

            if(StringUtils.isEmpty(poolAddress) || StringUtils.isBlank(poolAddress))
            {
                continue;
            }

            poolAddress = poolAddress.toLowerCase();

            Event poolMintEvent = new ContractsEventBuilder().build(ContractsEventEnum.MINT);
                String poolTopicEventMint = EventEncoder.encode(poolMintEvent).toLowerCase();
                topicAndContractAddr2EventMap.put(poolTopicEventMint + "_" + poolAddress, poolMintEvent);
                topicAndContractAddr2CallBackMap.put(poolTopicEventMint + "_" + poolAddress, MintEventHandler.class.getMethod("descPoolMintEvent", Log.class));

            Event poolBurnEvent = new ContractsEventBuilder().build(ContractsEventEnum.BURN);
            String poolTopicEventBurn = EventEncoder.encode(poolBurnEvent).toLowerCase();
            topicAndContractAddr2EventMap.put(poolTopicEventBurn + "_" + poolAddress, poolBurnEvent);
            topicAndContractAddr2CallBackMap.put(poolTopicEventBurn + "_" + poolAddress, MintEventHandler.class.getMethod("descPoolBurnEvent", Log.class));

            ContractsConfig.ContractInfo contractInfo = new ContractsConfig.ContractInfo();
            contractInfo.setName("pool" + poolAddress);
            contractInfo.setEnabled(true);
            contractInfo.setAddress(poolAddress);

            //动态增加从数据库中获取的合约地址来扫描
            contractsConfig.addContractAddress(contractInfo);
        }

        /**
         * NonfungiblePositionManager 关联同一交易hash中 Mint和IncreaseLiquidity事件，目前没法通过其他方式关联，只能通过同意交易hash关联起来
         *
         * tx_hash -> mint1和IncreaseLiquidity1 -> tokenId ->pool地址 ： 得到最终需要的关联关系： pool地址 -> tokenId 和 mint和IncreaseLiquidity中参数
         */

        ContractsConfig.ContractInfo nonFungiblePositionManagerCI = contractsConfig.getContractInfo("NonfungiblePositionManager");

        Event nonFungiblePositionManagerDecreaseLiquidityEvent = new ContractsEventBuilder().build(ContractsEventEnum.DECREASE_LIQUIDITY);
        String nonFungiblePositionManagerTopicEventDecreaseLiquidity = EventEncoder.encode(nonFungiblePositionManagerDecreaseLiquidityEvent).toLowerCase();
        topicAndContractAddr2EventMap.put(nonFungiblePositionManagerTopicEventDecreaseLiquidity + "_" + nonFungiblePositionManagerCI.getAddress(), nonFungiblePositionManagerDecreaseLiquidityEvent);
        topicAndContractAddr2CallBackMap.put(nonFungiblePositionManagerTopicEventDecreaseLiquidity + "_" + nonFungiblePositionManagerCI.getAddress(), MintEventHandler.class.getMethod("descDecreaseLiquidityEvent", Log.class));

        Event nonFungiblePositionManagerIncreaseLiquidityEvent = new ContractsEventBuilder().build(ContractsEventEnum.INCREASE_LIQUIDITY);
        String nonFungiblePositionManagerTopicEventIncreaseLiquidity = EventEncoder.encode(nonFungiblePositionManagerIncreaseLiquidityEvent).toLowerCase();
        topicAndContractAddr2EventMap.put(nonFungiblePositionManagerTopicEventIncreaseLiquidity + "_" + nonFungiblePositionManagerCI.getAddress(), nonFungiblePositionManagerIncreaseLiquidityEvent);
        topicAndContractAddr2CallBackMap.put(nonFungiblePositionManagerTopicEventIncreaseLiquidity + "_" + nonFungiblePositionManagerCI.getAddress(), MintEventHandler.class.getMethod("descIncreaseLiquidityEvent", Log.class));




    }


    public void blocksEventScanner(Integer delayBlocks) throws InterruptedException, NoSuchMethodException {

        ContractOffset contractOffset = contractOffsetService.findByContractAddress("Delay" + delayBlocks + "_" + BLOCK_CONTRACT_FLAG);
        BigInteger start;
        if (contractOffset == null) {
            start = new BigInteger(scannerContractStart);
        } else {
            start = contractOffset.getBlockOffset();
            if (ObjectUtils.isEmpty(start) || start.compareTo(BigInteger.ZERO) == 0) {
                start = new BigInteger(scannerContractStart);
            }
        }

        logger.info("Delay" + delayBlocks + "_" + "scan all nft albums run() selectMonitorState : " + start);

        BigInteger now = web3jUtils.getBlockNumber(delayBlocks);

        if (start.compareTo(now) >= 0) {
            logger.info("Delay" + delayBlocks + "_" + "scan all nft albums run() return start > now: " + start + " > " + now);
            return;
        }

        while (true) {

            logger.info("Delay" + delayBlocks + "_" + "blocksEventScanner run -------------------");
            if (now.compareTo(BigInteger.ZERO) == 0) {
                logger.info("Delay" + delayBlocks + "_" + "scan all nft albums run() return  now is Zero");
                break;
            }

            BigInteger end = start.add(STEP).compareTo(now) > 0 ? now : start.add(STEP);

            logger.info("Delay" + delayBlocks + "_" + "blocksEventScanner run block [" + start + "," + end + "] ");


            filterEvents(delayBlocks, start, end);

            start = end;

            updateOffset(delayBlocks, end);

            if (end.compareTo(now) >= 0) {
                logger.info("Delay" + delayBlocks + "_" + "scan all nft albums run() return  end > now: " + end + " > " + now);
                break;
            } else {
                initialize(null, null);
            }
            TimeUnit.MILLISECONDS.sleep(100);
        }
    }

    private void filterEvents(Integer delayBlocks, BigInteger start, BigInteger end) {


        List<Event> events = new ArrayList<>(topicAndContractAddr2EventMap.values());

        try {
            EthLog ethlog = web3jUtils.getEthLogs(start, end, events, contractsConfig.getEnabledContractAddresses()/*can be null */);
            logger.info("Delay" + delayBlocks + "_" + "filterEvents size: " + ethlog.getLogs().size());
            if (!ObjectUtils.isEmpty(ethlog) && ethlog.getLogs().size() > 0) {
                eventDispatcher(delayBlocks, ethlog);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void eventDispatcher(Integer delayBlocks, EthLog logs) {
        for (EthLog.LogResult logResult : logs.getLogs()) {

            Log log = (Log) logResult.get();

            String contractAddress = log.getAddress().toLowerCase(); //合约地址

            String topic = null;
            try {
                topic = log.getTopics().get(0).toLowerCase();
            } catch (Exception e) {
                continue;
            }

            String topicAddress = topic + "_" + contractAddress;
            Method callBackMethod = topicAndContractAddr2CallBackMap.get(topicAddress);
            if (null == callBackMethod) {
                continue;
            }
            try {
                //https://stackoverflow.com/questions/4480334/how-to-call-a-method-stored-in-a-hashmap-java
                // Method format must be: static void functionName(Log, Album)
                logger.info("Delay" + delayBlocks + "_" + "eventDispatcher call function: {} ", callBackMethod.getName());
                callBackMethod.invoke(null, log);

            }
            catch (Exception e) {
                e.printStackTrace();

                String message = e.getMessage();
                try {
                    message = ((InvocationTargetException) e).getTargetException().getMessage();
                }
                catch (Exception e1)
                {

                }
                if(message.contains("Duplicate entry"))
                {
                    //错误不处理 模拟 insert ignore into, 此处错误可忽略
                    logger.info("Delay" + delayBlocks + "_" + "Event handler  insert failed DuplicateKeyException function: {}, message:{}", callBackMethod.getName(), e.getMessage());
                }
                else{
                    logger.info("Delay" + delayBlocks + "_" + "Event handler Exception function: {}, message: {}", callBackMethod.getName(), message );
                }
            }

        }

    }

    private void updateOffset(Integer delayBlocks, BigInteger offset) {

        String contractAddress = "Delay" + delayBlocks + "_" + BLOCK_CONTRACT_FLAG;

        ContractOffset contractOffset = contractOffsetService.findByContractAddress(contractAddress);
        if (null == contractOffset) {
            contractOffset = new ContractOffset();
            contractOffset.setContractAddress(contractAddress);
            contractOffset.setContractName("ALL_CONTRACTS");
            contractOffset.setRecordedAt(new Timestamp(new Date().getTime()));
        }
        contractOffset.setBlockOffset(offset);
        contractOffsetService.update(contractOffset);
    }

}
