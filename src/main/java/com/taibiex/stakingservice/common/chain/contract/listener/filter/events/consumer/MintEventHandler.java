package com.taibiex.stakingservice.common.chain.contract.listener.filter.events.consumer;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.ContractsEventEnum;
import com.taibiex.stakingservice.common.chain.contract.listener.filter.events.impl.ContractsEventBuilder;
import com.taibiex.stakingservice.common.chain.contract.utils.EthLogsParser;
import com.taibiex.stakingservice.common.chain.contract.utils.Web3jUtils;
import com.taibiex.stakingservice.config.ContractsConfig;
import com.taibiex.stakingservice.entity.MintAndIncreaseLiquidity;
import com.taibiex.stakingservice.service.MintAndIncreaseLiquidityService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

@Slf4j
@Component
public class MintEventHandler {

    private static MintAndIncreaseLiquidityService mintAndIncreaseLiquidityService;
    private static Web3jUtils web3jUtils;


    static ContractsConfig contractsConfig;

    public MintEventHandler(MintAndIncreaseLiquidityService mintAndIncreaseLiquidityService, Web3jUtils web3jUtils, ContractsConfig contractsConfig) {
        MintEventHandler.mintAndIncreaseLiquidityService = mintAndIncreaseLiquidityService;
        MintEventHandler.web3jUtils = web3jUtils;
        MintEventHandler.contractsConfig = contractsConfig;
    }

    /**
     *  Mint和Burn一样，只有在Pool合约有，NonfungiblePositionManager没有，NonfungiblePositionManager是IncreaseLiquidity和DecreaseLiquidity
     *
     *  ------------------------------------------------------------------------------------------------
     * 注意：
     *
     * Pool合约添加流动性，一定有mint事件，但有可能有NonfungiblePositionManager合约的IncreaseLiquidity事件，
     * 而NonfungiblePositionManager合约添加流动性，就一定会有IncreaseLiquidity事件和Pool合约的mint事件
     * ------------------------------------------------------------------------------------------------     *
     * @param evLog
     * @throws IOException
     */
    public static void descPoolMintEvent(Log evLog) throws IOException {
        //event Mint(address sender, address indexed owner, int24 indexed tickLower, int24 indexed tickUpper, uint128 amount, uint256 amount0, uint256 amount1)
        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.MINT_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
        List<String> topics = evLog.getTopics();

        MintAndIncreaseLiquidity mintAndIncreaseLiquidity = new MintAndIncreaseLiquidity();
        String transactionHash = evLog.getTransactionHash();
        Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());

        Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
        transaction.ifPresent(ethTransaction -> mintAndIncreaseLiquidity.setSender(ethTransaction.getFrom()));

        mintAndIncreaseLiquidity.setTxHash(transactionHash /*+ "-" + evLog.getLogIndex(*/);
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
            mintAndIncreaseLiquidity.setTxHash(transactionHash/* + "-" + evLog.getLogIndex()*/);
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
        //IncreaseLiquidity(uint256 indexed tokenId, uint128 liquidity, uint256 amount0, uint256 amount1)

        Event descEvent = new ContractsEventBuilder().build(ContractsEventEnum.INCREASE_LIQUIDITY_DESC);

        List<Type> args = FunctionReturnDecoder.decode(evLog.getData(), descEvent.getParameters());
        List<String> topics = evLog.getTopics();


        if (!CollectionUtils.isEmpty(args)) {
            MintAndIncreaseLiquidity mintAndIncreaseLiquidity = new MintAndIncreaseLiquidity();
            String transactionHash = evLog.getTransactionHash();
            Timestamp eventHappenedTimeStamp = web3jUtils.getEventHappenedTimeStampByBlockHash(evLog.getBlockHash());

            String tokenId = EthLogsParser.hexToBigInteger(topics.get(1)).toString();

            /**
             *   function positions(uint256 tokenId)
             *         external
             *         view
             *         override
             *         returns (
             *             uint96 nonce,
             *             address operator,
             *             address token0,
             *             address token1,
             *             uint24 fee,
             *             int24 tickLower,
             *             int24 tickUpper,
             *             uint128 liquidity,
             *             uint256 feeGrowthInside0LastX128,
             *             uint256 feeGrowthInside1LastX128,
             *             uint128 tokensOwed0,
             *             uint128 tokensOwed1
             *         )
             * @param tokenId
             * @return
             */
            List<Type> poolDetails = web3jUtils.getUniswapV3PoolDetails(tokenId);

            //String operator =poolDetails.get(1).getValue().toString();
            String token0 =poolDetails.get(2).getValue().toString();
            String token1 =poolDetails.get(3).getValue().toString();
            Integer fee = Integer.valueOf(poolDetails.get(4).getValue().toString()); // 100 就是 100/10000 = 0.01就是1%

            BigInteger tickLower = (BigInteger)poolDetails.get(5).getValue();
            BigInteger tickUpper = (BigInteger)poolDetails.get(6).getValue();

            String poolAddress = web3jUtils.getPoolAddress(token0, token1, fee);
            //List<Type> poolInfo2 = web3jUtils.getUniswapV3PoolSlot0(poolAddress);
            ContractsConfig.ContractInfo nonFungiblePositionManagerCI = contractsConfig.getContractInfo("NonfungiblePositionManager");


            String owner = web3jUtils.getNftOwnerOf(nonFungiblePositionManagerCI.getAddress(), tokenId);
            mintAndIncreaseLiquidity.setOwner(owner);
            Optional<Transaction> transaction = web3jUtils.getWeb3j().ethGetTransactionByHash(transactionHash).send().getTransaction();
            transaction.ifPresentOrElse(
                    ethTransaction -> {mintAndIncreaseLiquidity.setSender(ethTransaction.getFrom()); mintAndIncreaseLiquidity.setOwner(ethTransaction.getFrom());},
                    () -> {mintAndIncreaseLiquidity.setSender(owner); mintAndIncreaseLiquidity.setOwner(owner); });

            if(StringUtils.equalsIgnoreCase(mintAndIncreaseLiquidity.getSender(), nonFungiblePositionManagerCI.getAddress()))
            {
                mintAndIncreaseLiquidity.setSender(owner);
                mintAndIncreaseLiquidity.setOwner(owner);
            }

            mintAndIncreaseLiquidity.setTxHash(transactionHash /*+ "-" + evLog.getLogIndex()*/);
            mintAndIncreaseLiquidity.setTokenId(tokenId);
            mintAndIncreaseLiquidity.setPool(poolAddress);

            /**
             * 情况1： 一种是常规调用方法，用户通过界面调用NonfungiblePositionManager合约添加或移除流动性，需要监听交易中的两个事件:
             *  监听NonfungiblePositionManager的IncreaseLiquidity事件，获取tokenId，再监听LP的Mint事件，
             *  如USDT-USDC-0.01%，Mint事件的owner为NonfungiblePositionManager，tickLower和tickUpper为用户添加流动性的价格区间，amount0和amount1为用户添加的流动性币种数量。
             *
             *  情况1的Mint事件的owner如果是NonfungiblePositionManager(0xd8442B36A021Ec592638C8B3529E492c5f6799B9)，就去看下面事件
             *  IncreaseLiquidit的tokenId, 根据erc721获取owner(假设用户没有转移nft,一般也没有人去转移) ownerOf.  情况1的Mint事件的owner如果不是NonfungiblePositionManager，就是用户地址
             */
//IncreaseLiquidity(uint256 indexed tokenId, uint128 liquidity, uint256 amount0, uint256 amount1)
//            mintAndIncreaseLiquidity.setOwner();
            if (tickLower != null) {
                mintAndIncreaseLiquidity.setTickLower(tickLower.toString());
            }
            if (tickUpper != null) {
                mintAndIncreaseLiquidity.setTickUpper(tickUpper.toString());
            }
            mintAndIncreaseLiquidity.setAmount(args.get(0).getValue().toString());
            mintAndIncreaseLiquidity.setAmount0(args.get(1).getValue().toString());
            mintAndIncreaseLiquidity.setAmount1(args.get(2).getValue().toString());
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
            mintAndIncreaseLiquidity.setTxHash(transactionHash /*+ "-" + evLog.getLogIndex()*/);
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
