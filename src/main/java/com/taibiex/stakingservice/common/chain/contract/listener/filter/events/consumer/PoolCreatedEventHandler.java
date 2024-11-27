package com.taibiex.stakingservice.common.chain.contract.listener.filter.events.consumer;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.EthLogsParser;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.entity.PoolCreated;
import com.taibiex.stakingservice.service.PoolCreatedService;
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
public class PoolCreatedEventHandler {

    private static PoolCreatedService poolCreatedService;
    private static Web3jUtils web3jUtils;

    public PoolCreatedEventHandler(PoolCreatedService poolCreatedService, Web3jUtils web3jUtils) {
        PoolCreatedEventHandler.poolCreatedService = poolCreatedService;
        PoolCreatedEventHandler.web3jUtils = web3jUtils;
    }

    public static void descPoolCreatedEvent(Log evLog) {
        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.POOL_CREATED_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
        List<String> topics = evLog.getTopics();

        if (!CollectionUtils.isEmpty(args)) {
            PoolCreated poolCreated = new PoolCreated();
            String transactionHash = evLog.getTransactionHash();
            Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());
            poolCreated.setTxHash(transactionHash);
            poolCreated.setToken0(EthLogsParser.hexToAddress(topics.get(1)));
            poolCreated.setToken1(EthLogsParser.hexToAddress(topics.get(2)));
            poolCreated.setFee(EthLogsParser.hexToBigInteger(topics.get(3)).toString());
            poolCreated.setTickSpacing(args.get(0).getValue().toString());
            poolCreated.setPool(args.get(1).getValue().toString());
            poolCreated.setCreateTime(eventHappenedTimeStamp);
            poolCreated.setLastUpdateTime(eventHappenedTimeStamp);
            poolCreatedService.save(poolCreated);
        } else if (!CollectionUtils.isEmpty(topics)) {
            String from = EthLogsParser.hexToAddress(topics.get(1));
            String to = EthLogsParser.hexToAddress(topics.get(2));
            BigInteger tokenId = EthLogsParser.hexToBigInteger(topics.get(3));
            log.info("descPoolCreatedEvent from = {}\n to = {} \n tokenId = {}", from, to, tokenId);
        }
    }
}
