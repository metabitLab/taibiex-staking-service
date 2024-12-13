package com.taibiex.stakingservice.common.chain.contract.listener.filter.events.consumer;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.EthLogsParser;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.config.ContractsConfig;
import com.taibiex.stakingservice.entity.LiquidityEvent;
import com.taibiex.stakingservice.entity.SPStaking;
import com.taibiex.stakingservice.repository.SpStakingRepository;
import com.taibiex.stakingservice.service.LiquidityEventService;
import com.taibiex.stakingservice.service.RewardPoolService;
import com.taibiex.stakingservice.service.SpStakingService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;
import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 单币质押
 */
@Slf4j
@Component
public class StakingEventHandler {

    static Web3jUtils web3jUtils;

    static ContractsConfig contractsConfig;

    static SpStakingService spStakingService;

    public StakingEventHandler(SpStakingService spStakingService, Web3jUtils web3jUtils, ContractsConfig contractsConfig) {
        StakingEventHandler.spStakingService = spStakingService;
        StakingEventHandler.web3jUtils = web3jUtils;
        StakingEventHandler.contractsConfig = contractsConfig;
    }

    public static void descStakingEvent(Log evLog) throws IOException {
        //event Stake(address indexed user, uint256 amount0)

        String transactionHash = evLog.getTransactionHash();
        Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());

        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.STAKING_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());

        List<String> topics = evLog.getTopics();

        String stakingPoolContractAddress = evLog.getAddress();
        //0x06CB38541B139E24b2e95c88c9195BC8e4cA5774

        String userAddress = EthLogsParser.hexToAddress(topics.get(1));

        String stakingAmount = args.get(0).getValue().toString();

        SPStaking spStaking = new SPStaking();

//        Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
//        transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setOwner(ethTransaction.getFrom()));

        spStaking.setTxHash(transactionHash);
        spStaking.setCreateTime(eventHappenedTimeStamp);
        spStaking.setLastUpdateTime(eventHappenedTimeStamp);
        spStaking.setAmount(stakingAmount);
        spStaking.setUserAddress(userAddress);
        //质押类型: 1.质押  0.解除质押"
        spStaking.setType((short) 1);
        //解除质押后，需要等到这个块才能解锁(claim), 质押时，该字段为空字符串''
        spStaking.setUnlockBlock("");
        spStakingService.save(spStaking);

    }

    public static void descUnStakingEvent(Log evLog) throws IOException {
        //event Unstake(address indexed user, uint256 amount0, uint256 unlockBlock)

        String transactionHash = evLog.getTransactionHash();
        Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());

        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.UN_STAKING_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());

        List<String> topics = evLog.getTopics();

        String stakingPoolContractAddress = evLog.getAddress();
        //0x06CB38541B139E24b2e95c88c9195BC8e4cA5774

        String userAddress = EthLogsParser.hexToAddress(topics.get(1));

        String stakingAmount = args.get(0).getValue().toString();

        String unlockBlock = args.get(1).getValue().toString();

        SPStaking spStaking = new SPStaking();

//        Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
//        transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setOwner(ethTransaction.getFrom()));

        spStaking.setTxHash(transactionHash);
        spStaking.setCreateTime(eventHappenedTimeStamp);
        spStaking.setLastUpdateTime(eventHappenedTimeStamp);
        spStaking.setAmount(stakingAmount);
        spStaking.setUserAddress(userAddress);
        spStaking.setUnlockBlock(unlockBlock);

        //质押类型: 1.质押  0.解除质押"
        spStaking.setType((short) 0);

        spStakingService.save(spStaking);

    }

}
