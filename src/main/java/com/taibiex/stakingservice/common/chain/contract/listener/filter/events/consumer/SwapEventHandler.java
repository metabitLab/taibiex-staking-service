package com.taibiex.stakingservice.common.chain.contract.listener.filter.events.consumer;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.EthLogsParser;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.entity.Swap;
import com.taibiex.stakingservice.service.SwapService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.Log;

import java.math.BigInteger;
import java.sql.Timestamp;
import java.util.List;

@Slf4j
@Component
public class SwapEventHandler {

    private static SwapService swapService;
    private static Web3jUtils web3jUtils;

    public SwapEventHandler(SwapService swapService, Web3jUtils web3jUtils) {
        SwapEventHandler.swapService = swapService;
        SwapEventHandler.web3jUtils = web3jUtils;
    }

    public static void descSwapEvent(Log evLog) {
        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.SWAP_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
        List<String> topics = evLog.getTopics();

        if (!CollectionUtils.isEmpty(args)) {
            Swap swap = new Swap();
            String transactionHash = evLog.getTransactionHash();
            Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());
            swap.setTxHash(transactionHash + "-" + evLog.getLogIndex());
            swap.setSender(EthLogsParser.hexToAddress(topics.get(1)));
            swap.setRecipient(EthLogsParser.hexToAddress(topics.get(2)));
            swap.setAmount0(args.get(0).getValue().toString());
            swap.setAmount1(args.get(1).getValue().toString());
            swap.setSqrtPriceX96(args.get(2).getValue().toString());
            swap.setLiquidity(args.get(3).getValue().toString());
            swap.setTick(args.get(4).getValue().toString());
            swap.setCreateTime(eventHappenedTimeStamp);
            swap.setLastUpdateTime(eventHappenedTimeStamp);
            swapService.save(swap);
        } else if (!CollectionUtils.isEmpty(topics)) {
            String from = EthLogsParser.hexToAddress(topics.get(1));
            String to = EthLogsParser.hexToAddress(topics.get(2));
            BigInteger tokenId = EthLogsParser.hexToBigInteger(topics.get(3));
            log.info("descPoolCreatedEvent from = {}\n to = {} \n tokenId = {}", from, to, tokenId);
        }
    }
}
