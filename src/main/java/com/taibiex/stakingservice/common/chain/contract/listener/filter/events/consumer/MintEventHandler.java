package com.taibiex.stakingservice.common.chain.contract.listener.filter.events.consumer;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.EthLogsParser;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.entity.MintAndIncreaseLiquidity;
import com.taibiex.stakingservice.service.MintAndIncreaseLiquidityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class MintEventHandler {

    private static MintAndIncreaseLiquidityService mintAndIncreaseLiquidityService;
    private static Web3jUtils web3jUtils;

    public MintEventHandler(MintAndIncreaseLiquidityService mintAndIncreaseLiquidityService, Web3jUtils web3jUtils) {
        MintEventHandler.mintAndIncreaseLiquidityService = mintAndIncreaseLiquidityService;
        MintEventHandler.web3jUtils = web3jUtils;
    }

    public static void descPoolMintEvent(Log evLog) throws IOException {
        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.MINT_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
        List<String> topics = evLog.getTopics();


        if (!CollectionUtils.isEmpty(args)) {
            MintAndIncreaseLiquidity mintAndIncreaseLiquidity = new MintAndIncreaseLiquidity();
            String transactionHash = evLog.getTransactionHash();
            Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());
            Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
            transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setSender(ethTransaction.getFrom()));
            mintAndIncreaseLiquidity.setTxHash(transactionHash + "-" + evLog.getLogIndex());
            mintAndIncreaseLiquidity.setOwner(EthLogsParser.hexToAddress(topics.get(1)));
            mintAndIncreaseLiquidity.setTickLower(EthLogsParser.hexToBigInteger(topics.get(2)).toString());
            mintAndIncreaseLiquidity.setTickUpper(EthLogsParser.hexToBigInteger(topics.get(3)).toString());
            mintAndIncreaseLiquidity.setAmount(args.get(1).getValue().toString());
            mintAndIncreaseLiquidity.setAmount0(args.get(2).getValue().toString());
            mintAndIncreaseLiquidity.setAmount1(args.get(3).getValue().toString());
            mintAndIncreaseLiquidity.setCreateTime(eventHappenedTimeStamp);
            mintAndIncreaseLiquidity.setLastUpdateTime(eventHappenedTimeStamp);
            mintAndIncreaseLiquidityService.save(mintAndIncreaseLiquidity);
        }
    }

    public static void descPoolBurnEvent(Log evLog) throws IOException {
        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.BURN_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
        List<String> topics = evLog.getTopics();


        if (!CollectionUtils.isEmpty(args)) {
            MintAndIncreaseLiquidity mintAndIncreaseLiquidity = new MintAndIncreaseLiquidity();
            String transactionHash = evLog.getTransactionHash();
            Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());
            Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
            transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setSender(ethTransaction.getFrom()));
            mintAndIncreaseLiquidity.setTxHash(transactionHash + "-" + evLog.getLogIndex());
            mintAndIncreaseLiquidity.setOwner(EthLogsParser.hexToAddress(topics.get(1)));
            mintAndIncreaseLiquidity.setTickLower(EthLogsParser.hexToBigInteger(topics.get(2)).toString());
            mintAndIncreaseLiquidity.setTickUpper(EthLogsParser.hexToBigInteger(topics.get(3)).toString());
            mintAndIncreaseLiquidity.setAmount(args.get(1).getValue().toString());
            mintAndIncreaseLiquidity.setAmount0(args.get(2).getValue().toString());
            mintAndIncreaseLiquidity.setAmount1(args.get(3).getValue().toString());
            mintAndIncreaseLiquidity.setCreateTime(eventHappenedTimeStamp);
            mintAndIncreaseLiquidity.setLastUpdateTime(eventHappenedTimeStamp);
            mintAndIncreaseLiquidityService.save(mintAndIncreaseLiquidity);
        }
    }



    public static void descIncreaseLiquidityEvent(Log evLog) throws IOException {
        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.INCREASE_LIQUIDITY_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
        List<String> topics = evLog.getTopics();


        if (!CollectionUtils.isEmpty(args)) {
            MintAndIncreaseLiquidity mintAndIncreaseLiquidity = new MintAndIncreaseLiquidity();
            String transactionHash = evLog.getTransactionHash();
            Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());
            Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
            transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setSender(ethTransaction.getFrom()));
            mintAndIncreaseLiquidity.setTxHash(transactionHash + "-" + evLog.getLogIndex());
            mintAndIncreaseLiquidity.setOwner(EthLogsParser.hexToAddress(topics.get(1)));
            mintAndIncreaseLiquidity.setTickLower(EthLogsParser.hexToBigInteger(topics.get(2)).toString());
            mintAndIncreaseLiquidity.setTickUpper(EthLogsParser.hexToBigInteger(topics.get(3)).toString());
            mintAndIncreaseLiquidity.setAmount(args.get(1).getValue().toString());
            mintAndIncreaseLiquidity.setAmount0(args.get(2).getValue().toString());
            mintAndIncreaseLiquidity.setAmount1(args.get(3).getValue().toString());
            mintAndIncreaseLiquidity.setCreateTime(eventHappenedTimeStamp);
            mintAndIncreaseLiquidity.setLastUpdateTime(eventHappenedTimeStamp);
            mintAndIncreaseLiquidityService.save(mintAndIncreaseLiquidity);
        }
    }

    public static void descDecreaseLiquidityEvent(Log evLog) throws IOException {
        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.DECREASE_LIQUIDITY_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
        List<String> topics = evLog.getTopics();


        if (!CollectionUtils.isEmpty(args)) {
            MintAndIncreaseLiquidity mintAndIncreaseLiquidity = new MintAndIncreaseLiquidity();
            String transactionHash = evLog.getTransactionHash();
            Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());
            Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
            transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setSender(ethTransaction.getFrom()));
            mintAndIncreaseLiquidity.setTxHash(transactionHash + "-" + evLog.getLogIndex());
            mintAndIncreaseLiquidity.setOwner(EthLogsParser.hexToAddress(topics.get(1)));
            mintAndIncreaseLiquidity.setTickLower(EthLogsParser.hexToBigInteger(topics.get(2)).toString());
            mintAndIncreaseLiquidity.setTickUpper(EthLogsParser.hexToBigInteger(topics.get(3)).toString());
            mintAndIncreaseLiquidity.setAmount(args.get(1).getValue().toString());
            mintAndIncreaseLiquidity.setAmount0(args.get(2).getValue().toString());
            mintAndIncreaseLiquidity.setAmount1(args.get(3).getValue().toString());
            mintAndIncreaseLiquidity.setCreateTime(eventHappenedTimeStamp);
            mintAndIncreaseLiquidity.setLastUpdateTime(eventHappenedTimeStamp);
            mintAndIncreaseLiquidityService.save(mintAndIncreaseLiquidity);
        }
    }
}
