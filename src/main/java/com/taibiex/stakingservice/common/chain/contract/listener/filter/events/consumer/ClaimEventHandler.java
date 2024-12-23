package com.taibiex.stakingservice.common.chain.contract.listener.filter.events.consumer;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.EthLogsParser;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.common.utils.HashUtil;
import com.taibiex.stakingservice.config.ContractsConfig;
import com.taibiex.stakingservice.entity.ClaimEvent;
import com.taibiex.stakingservice.service.ClaimService;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.core.methods.response.Log;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        //0xB9f2218e03cF0753e2FEE2A1B6B5718a12b42E4b

        String userAddress = EthLogsParser.hexToAddress(topics.get(1));

        //List<String>  claimIndexLists = (List<String>) args.get(0).getValue();
        ArrayList<String> claimIndexLists = (ArrayList<String>) ((List<Uint256>) args.get(0).getValue()).stream().map(landId -> landId.getValue().toString()).collect(Collectors.toList());

        String stakingAmount = args.get(1).getValue().toString();

        ClaimEvent claimEvent = new ClaimEvent();

//        Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
//        transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setOwner(ethTransaction.getFrom()));

        claimEvent.setTxHash(transactionHash);
        claimEvent.setCreateTime(eventHappenedTimeStamp);
        claimEvent.setLastUpdateTime(eventHappenedTimeStamp);
        claimEvent.setAmount(stakingAmount);
        claimEvent.setUserAddress(userAddress);
        //为 ,连接的字符串 表示一笔claim之前所有已解质押并且已解锁的奖励。如:1,2,3 单个值时，表示claim的单笔已解质押已解锁的奖励, 如: 5
        String claimIndexListString = String.join(",", claimIndexLists);
        claimEvent.setClaimIndex(claimIndexListString);
        //varchar 大于255就不让加索引了, 所以增加一个hash字段，主要是为了唯一索引使用, 值为claim_index的 SHA-256 哈希值
        String hash = HashUtil.getSHA256Hash(claimIndexListString);
        claimEvent.setClaimIndexHash(hash);

        claimService.userClaimHandler(claimEvent);
    }

    public static void descClaimIndexEvent(Log evLog) throws IOException {
        //event ClaimIndex(address indexed user, uint256 index, uint256 amount0)

        String transactionHash = evLog.getTransactionHash();
        Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());

        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.CLAIM_INDEX_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());

        List<String> topics = evLog.getTopics();

        String stakingPoolContractAddress = evLog.getAddress();
        //0xB9f2218e03cF0753e2FEE2A1B6B5718a12b42E4b

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
        //为 ,连接的字符串 表示一笔claim之前所有已解质押并且已解锁的奖励。如:1,2,3 单个值时，表示claim的单笔已解质押已解锁的奖励, 如: 5
        claimEvent.setClaimIndex(claimIndex);
        //varchar 大于255就不让加索引了, 所以增加一个hash字段，主要是为了唯一索引使用, 值为claim_index的 SHA-256 哈希值
        String hash = HashUtil.getSHA256Hash(claimIndex);
        claimEvent.setClaimIndexHash(hash);
        claimService.userClaimIndexHandler(claimEvent);

    }

}
