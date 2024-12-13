package com.taibiex.stakingservice.common.chain.contract.listener.filter.events.consumer;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.EthLogsParser;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.config.ContractsConfig;
import com.taibiex.stakingservice.entity.ClaimEvent;
import com.taibiex.stakingservice.service.ClaimService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;

/**
 * claim是领取解锁的token，不是领取奖励. claim那个得等7天才能解锁才能claim
 * claim是一次性领取所有解锁了的本金，claimIndex是选定哪一期解锁. 和奖励没关系
 *
 */
@Slf4j
@Component
public class ClaimEventHandler {

    static Web3jUtils web3jUtils;

    static ContractsConfig contractsConfig;

    static ClaimService claimService;

    public ClaimEventHandler(ClaimService claimService, Web3jUtils web3jUtils, ContractsConfig contractsConfig) {
        ClaimEventHandler.claimService = claimService;
        ClaimEventHandler.web3jUtils = web3jUtils;
        ClaimEventHandler.contractsConfig = contractsConfig;
    }

    public static void descClaimEvent(Log evLog) throws IOException {
        //event Claim(address indexed user, uint256 amount0)

        String transactionHash = evLog.getTransactionHash();
        Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());

        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.CLAIM_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());

        List<String> topics = evLog.getTopics();

        String stakingPoolContractAddress = evLog.getAddress();
        //0x06CB38541B139E24b2e95c88c9195BC8e4cA5774

        String userAddress = EthLogsParser.hexToAddress(topics.get(1));

        String stakingAmount = args.get(0).getValue().toString();

        ClaimEvent claimEvent = new ClaimEvent();

//        Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
//        transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setOwner(ethTransaction.getFrom()));

        claimEvent.setTxHash(transactionHash);
        claimEvent.setCreateTime(eventHappenedTimeStamp);
        claimEvent.setLastUpdateTime(eventHappenedTimeStamp);
        claimEvent.setAmount(stakingAmount);
        claimEvent.setUserAddress(userAddress);
        //claim单笔解质押时的索引，为 空字符串时 表示一笔claim所有
        claimEvent.setClaimIndex("");

        claimService.save(claimEvent);

    }

    public static void descClaimIndexEvent(Log evLog) throws IOException {
        //event ClaimIndex(address indexed user, uint256 index, uint256 amount0)

        String transactionHash = evLog.getTransactionHash();
        Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());

        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.CLAIM_INDEX_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());

        List<String> topics = evLog.getTopics();

        String stakingPoolContractAddress = evLog.getAddress();
        //0x06CB38541B139E24b2e95c88c9195BC8e4cA5774

        String userAddress = EthLogsParser.hexToAddress(topics.get(1));

        String claimIndex = args.get(0).getValue().toString();

        String stakingAmount = args.get(1).getValue().toString();

        ClaimEvent claimEvent = new ClaimEvent();

//        Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
//        transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setOwner(ethTransaction.getFrom()));

        claimEvent.setTxHash(transactionHash);
        claimEvent.setCreateTime(eventHappenedTimeStamp);
        claimEvent.setLastUpdateTime(eventHappenedTimeStamp);
        claimEvent.setAmount(stakingAmount);
        claimEvent.setUserAddress(userAddress);
        //claim单笔解质押时的索引，为 空字符串时 表示一笔claim所有
        claimEvent.setClaimIndex(claimIndex);

        claimService.save(claimEvent);

    }

}
