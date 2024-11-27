package com.taibiex.stakingservice.common.chain.contract.listener.filter.events.consumer;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.EthLogsParser;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.entity.Mint;
import com.taibiex.stakingservice.service.MintService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.EthTransaction;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.Transaction;

import java.io.IOException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class MintEventHandler {

    private static MintService mintService;
    private static Web3jUtils web3jUtils;

    public MintEventHandler(MintService mintService, Web3jUtils web3jUtils) {
        MintEventHandler.mintService = mintService;
        MintEventHandler.web3jUtils = web3jUtils;
    }

    public static void descMintEvent(Log evLog) throws IOException {
        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.MINT_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
        List<String> topics = evLog.getTopics();

        if (!CollectionUtils.isEmpty(args)) {
            Mint mint = new Mint();
            String transactionHash = evLog.getTransactionHash();
            Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());
            Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
            transaction.ifPresent(ethTransaction -> mint.setSender(ethTransaction.getFrom()));
            mint.setTxHash(transactionHash + "-" + evLog.getLogIndex());
            mint.setOwner(EthLogsParser.hexToAddress(topics.get(1)));
            mint.setTickLower(EthLogsParser.hexToBigInteger(topics.get(2)).toString());
            mint.setTickUpper(EthLogsParser.hexToBigInteger(topics.get(3)).toString());
            mint.setAmount(args.get(1).getValue().toString());
            mint.setAmount0(args.get(2).getValue().toString());
            mint.setAmount1(args.get(3).getValue().toString());
            mint.setCreateTime(eventHappenedTimeStamp);
            mint.setLastUpdateTime(eventHappenedTimeStamp);
            mintService.save(mint);
        }
    }
}
