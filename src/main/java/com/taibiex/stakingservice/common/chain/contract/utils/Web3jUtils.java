package com.taibiex.stakingservice.common.chain.contract.utils;

import com.taibiex.stakingservice.common.chain.contract.listener.filter.Monitor;
import com.taibiex.stakingservice.common.chain.contract.types.eip721.generated.ERC721;
import com.taibiex.stakingservice.common.utils.RedisService;
import com.taibiex.stakingservice.config.ContractsConfig;
import com.taibiex.stakingservice.config.ProfileConfig;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileCopyUtils;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.FunctionReturnDecoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.*;
import org.web3j.abi.datatypes.generated.*;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.ReadonlyTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.DefaultGasProvider;
import org.web3j.tx.response.PollingTransactionReceiptProcessor;
import org.web3j.tx.response.TransactionReceiptProcessor;
import org.web3j.utils.Numeric;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
public class Web3jUtils {

    private static String DEV_PROFILE = "dev";
    private static String TEST_PROFILE = "test";
    private static String PROD_PROFILE = "prod";

    //https://github.com/Uniswap/v3-periphery/blob/0682387198a24c7cd63566a2c58398533860a5d1/contracts/libraries/PoolAddress.sol#L10
    private static String POOL_INIT_CODE_HASH = "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54";

    public static Logger logger = LoggerFactory.getLogger(Web3jUtils.class);

    @Value("${web3j.client-address}")
    private String rpcAddress;

    /**
     * inject by web3j-spring-boot-starter
     */
    @Resource
    private Web3j web3j;

    @Value("${tabi.password}")
    private String password;

    @Value("${tabi.chainId}")
    private int chainId;

    private static Credentials credentials;

    @Autowired
    private ProfileConfig profileConfig;

    @Autowired
    ContractsConfig contractsConfig;

    @Autowired
    private RedisService redisService;

    public Web3j getWeb3j() {
        return web3j;
    }

    @PostConstruct
    public void init() throws IOException {
        String keystoreContent = getKeystoreContent();
        credentials = Credentials.create(getPrivateKey(keystoreContent, password));
    }

    private String getKeystoreContent() throws IOException {

        String keystorePath = "";

        String activeProfile = profileConfig.getActiveProfile();

        log.info("The current runtime environment：" + activeProfile);

        if (activeProfile.equals(TEST_PROFILE)){
            keystorePath = "keystore/keystore_test";
        } else if (activeProfile.equals(DEV_PROFILE)) {
            keystorePath = "keystore/keystore_dev";
        } else if(activeProfile.equals(PROD_PROFILE)) {
            keystorePath = "keystore/keystore";
        }

        ClassPathResource classPathResource = new ClassPathResource(keystorePath);
        String data = "";

        byte[] bdata = FileCopyUtils.copyToByteArray(classPathResource.getInputStream());
        data = new String(bdata, StandardCharsets.UTF_8);

        return data;
    }

    public BigInteger getBlockNumber(Integer delayBlocks) {

        BigInteger blockNumber = new BigInteger("0");
        try {
            blockNumber = web3j.ethBlockNumber().send().getBlockNumber();

            if (blockNumber.compareTo(BigInteger.valueOf(delayBlocks)) > 0) {
                blockNumber = blockNumber.subtract(BigInteger.valueOf(delayBlocks));
            }

            logger.info(" getBlockNumber the current block number is {}", blockNumber);

        } catch (IOException e) {
            logger.error("get block number failed, IOException: ", e);
        }
        return blockNumber;
    }

    public EthLog filterEthLog(BigInteger start, BigInteger end, Event event, String contractAddress) throws IOException {
        DefaultBlockParameter startBlock = DefaultBlockParameter.valueOf(start);
        DefaultBlockParameter endBlock = DefaultBlockParameter.valueOf(end);
        org.web3j.protocol.core.methods.request.EthFilter filter = new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress);
        String topic = EventEncoder.encode(event);
        logger.info(" ==========> filterEthLog topic {}", topic);
        filter.addSingleTopic(topic);
        return web3j.ethGetLogs(filter).send();
    }

    public EthLog filterEthLog(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock, Event event, String contractAddress) throws IOException {

        org.web3j.protocol.core.methods.request.EthFilter filter = new org.web3j.protocol.core.methods.request.EthFilter(startBlock, endBlock, contractAddress);
        String topic = EventEncoder.encode(event);
        logger.info(" ==========> filterEthLog topic {}", topic);
        filter.addSingleTopic(topic);
        return web3j.ethGetLogs(filter).send();
    }

    /**
     * Retrieve Ethereum event logs
     *
     * @param addresses: can be null
     * @return EthLog
     */
    public EthLog getEthLogs(BigInteger start, BigInteger end, List<Event> events, List<String> addresses/*can be null */) throws IOException {
        org.web3j.protocol.core.methods.request.EthFilter filter = Monitor.getFilter(start, end, events, addresses/* null */);
        EthLog ethlog = web3j.ethGetLogs(filter).send();
        return ethlog;
    }

    /**
     * Get balance
     *
     * @param address Wallet address
     * @return  balance
     */
    public BigInteger getBalance(String address) {
        BigInteger balance = null;
        try {
            EthGetBalance ethGetBalance = web3j.ethGetBalance(address, DefaultBlockParameterName.LATEST).send();
            balance = ethGetBalance.getBalance();
        } catch (IOException e) {
            e.printStackTrace();
        }
        logger.info("address " + address + " balance " + balance + "wei");
        return balance;
    }

    /**
     * get Gas Price
     */
    private BigInteger getGasPrice() {
        BigInteger gasPrice = null;
        while (true) {
            try {
                EthGasPrice send = web3j.ethGasPrice().send();
                gasPrice = send.getGasPrice();
                return gasPrice;
            } catch (IOException e) {
                logger.error("can't get gasPrice from private chain exception log: ", e);
            }
        }
    }


    /**
     * Generate a regular transaction object
     *
     * @param fromAddress Sender address
     * @param toAddress Recipient address
     * @param nonce Transaction nonce
     * @param gasPrice Gas price
     * @param gasLimit Gas limit
     * @param value Amount
     * @return Transaction object
     */
    private org.web3j.protocol.core.methods.request.Transaction makeTransaction(String fromAddress, String toAddress, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, BigInteger value) {
        org.web3j.protocol.core.methods.request.Transaction transaction;
        transaction = org.web3j.protocol.core.methods.request.Transaction.createEtherTransaction(fromAddress, nonce, gasPrice, gasLimit, toAddress, value);
        return transaction;
    }

    /**
     * Get gas limit for a regular transaction
     *
     * @param transaction Transaction object
     * @return gas limit
     */
    private BigInteger getTransactionGasLimit(org.web3j.protocol.core.methods.request.Transaction transaction) {
        BigInteger gasLimit = BigInteger.ZERO;
        try {
            EthEstimateGas ethEstimateGas = web3j.ethEstimateGas(transaction).send();
            gasLimit = ethEstimateGas.getAmountUsed();
        } catch (IOException e) {
            // e.printStackTrace();
            logger.error("getTransactionGasLimit IOException log: {}", e);
        }
        return gasLimit;
    }

    /**
     * Get account transaction nonce
     *
     * @param address wallet address
     * @return nonce
     */
    private BigInteger getTransactionNonce(String address) {
        BigInteger nonce = BigInteger.ZERO;
        try {
            EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(address, DefaultBlockParameterName.PENDING).send();
            nonce = ethGetTransactionCount.getTransactionCount();
        } catch (IOException e) {
            logger.error("getTransactionNonce IOException log: {}", e);
        }
        return nonce;
    }

    public BigInteger getBalance(){
        if (null == credentials) {
            throw new RuntimeException("sendTransaction can't find keystore credentials");
        }
        return getBalance(credentials.getAddress());
    }

    public String sendTransaction(Function function, String contractAddress) throws IOException, ExecutionException, InterruptedException{

        synchronized (Web3jUtils.class) {
            String encodedFunction = FunctionEncoder.encode(function);

            if (null == credentials) {
                throw new RuntimeException("sendTransaction can't find keystore credentials");
            }
            Web3j web3j1 = Web3j.build(new HttpService(rpcAddress));
            String fromAddress = credentials.getAddress();
            try {
                EthGetTransactionCount ethGetTransactionCount = web3j1.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING).sendAsync().get();
                BigInteger nonce = ethGetTransactionCount.getTransactionCount();
                log.info("sendTransaction nonce: {}", nonce);

                /*Transaction transaction = Transaction.createFunctionCallTransaction(
                        fromAddress,
                        nonce,
                        BigInteger.ZERO,
                        BigInteger.ZERO,
                        contractAddress,
                        encodedFunction
                );*/

                //BigInteger transactionGasLimit = getTransactionGasLimit(transaction);

                BigInteger ethGasPrice = getGasPrice().multiply(new BigInteger("11")).divide(new BigInteger("10"));

                RawTransaction rawTransaction = RawTransaction.createTransaction( nonce, ethGasPrice, DefaultGasProvider.GAS_LIMIT, contractAddress, encodedFunction);
                //RawTransaction rawTransaction = RawTransaction.createTransaction( nonce, ethGasPrice, transactionGasLimit.multiply(new BigInteger("2")), contractAddress, encodedFunction);

                byte[] signedMessage = TransactionEncoder.signMessage(rawTransaction, chainId, credentials);
                String hexValue = Numeric.toHexString(signedMessage);

                EthSendTransaction ethSendTransaction = web3j1.ethSendRawTransaction(hexValue).send();
                log.info("sendTransaction txHash: {}", ethSendTransaction.getTransactionHash());
                if (ethSendTransaction.hasError()) {
                    log.error("sendTransaction Error:" + ethSendTransaction.getError().getMessage());
                    throw new EOFException(ethSendTransaction.getError().getMessage());
                }
                return ethSendTransaction.getTransactionHash();

            } catch (Exception e) {
                log.error("sendTransaction Exception:" + e);
                throw e;
            }
        }

    }

    /*
     * Waiting for transaction receipt
     */
    public TransactionReceipt waitForTransactionReceipt(String txHash) throws TransactionException {
        // Wait for transaction to be mined
        TransactionReceiptProcessor receiptProcessor = new PollingTransactionReceiptProcessor(web3j, TransactionManager.DEFAULT_POLLING_FREQUENCY, TransactionManager.DEFAULT_POLLING_ATTEMPTS_PER_TX_HASH);
        TransactionReceipt txReceipt = null;

        int m = 0, retryTimes = 20;
        while (m < retryTimes) {
            try {
                txReceipt = receiptProcessor.waitForTransactionReceipt(txHash);
                break;
            } catch (/*SocketTimeoutException*/ IOException e) {
                logger.error("waitForTransactionReceipt SocketException:", e);
                try {
                    TimeUnit.MILLISECONDS.sleep(2000);
                } catch (InterruptedException e1) {
                }
                m++;
                if (m < retryTimes) {
                    logger.info("waitForTransactionReceipt SocketException retrying ....");
                }
            } catch (TransactionException e) {
                // throw new RuntimeException(e);
                logger.error("waitForTransactionReceipt TransactionException:" + e);
                throw e;
            }
        }
        return txReceipt;
    }

    public List<Type> callContractFunction(Function function, String contractAddress) throws ExecutionException, InterruptedException {

        if (null == credentials) {
            throw new RuntimeException("sendTransaction can't find keystore credentials");
        }

        String fromAddress = credentials.getAddress();

        String encodedFunction = FunctionEncoder.encode(function);
        int m = 0, retryTimes = 20;
        while (m < retryTimes) {
            // use credentials get error: Empty value (0x) returned from contract web3j

            try {
                /*org.web3j.protocol.core.methods.response.EthCall*/
                EthCall response = web3j.ethCall(Transaction.createEthCallTransaction(fromAddress, contractAddress, encodedFunction), DefaultBlockParameterName.LATEST).sendAsync().get();

                return FunctionReturnDecoder.decode(response.getValue(), function.getOutputParameters());
            } catch (Exception e) {
                m++;
                if (m < retryTimes) {
                    log.error("callContractFunction 错误：",  e);
                }
                else{
                    throw e;
                }
            }
        }
        return null;

    }


    /**
     * get PrivateKey
     *
     * @param keystoreContent account keystore
     * @param password
     * @return privateKey
     */
    private String getPrivateKey(String keystoreContent, String password) {
        try {
            Credentials credentials = WalletUtils.loadJsonCredentials(password, keystoreContent);
            BigInteger privateKey = credentials.getEcKeyPair().getPrivateKey();
            return privateKey.toString(16);
        } catch (IOException | CipherException e) {
            logger.error("getPrivateKey Exception:" + e);
        }
        return null;
    }

    /**
     * generate Keystore
     *
     * @param privateKey privateKey
     * @param password   password
     * @param directory  directory
     */
    private static void generateKeystore(byte[] privateKey, String password, String directory) {
        ECKeyPair ecKeyPair = ECKeyPair.create(privateKey);
        try {
            String keystoreName = WalletUtils.generateWalletFile(password, ecKeyPair, new File(directory), true);
            System.out.println("keystore name " + keystoreName);
        } catch (CipherException | IOException e) {
            logger.error("generateKeystore Exception:" + e);
        }
    }

    public static boolean isSignatureValid(final String address, final String signature, final String message) {

        final String personalMessagePrefix = "\u0019Ethereum Signed Message:\n";
        boolean match = false;

        final String prefix = personalMessagePrefix + message.length();
        final byte[] msgHash = Hash.sha3((prefix + message).getBytes());
        final byte[] signatureBytes = Numeric.hexStringToByteArray(signature);
        byte v = signatureBytes[64];
        if (v < 27) {
            v += 27;
        }

        final Sign.SignatureData sd = new Sign.SignatureData(v,
                Arrays.copyOfRange(signatureBytes, 0, 32),
                Arrays.copyOfRange(signatureBytes, 32, 64));

        String addressRecovered = null;

        // Iterate for each possible key to recover
        for (int i = 0; i < 4; i++) {
            final BigInteger publicKey = Sign.recoverFromSignature((byte) i, new ECDSASignature(
                    new BigInteger(1, sd.getR()),
                    new BigInteger(1, sd.getS())), msgHash);

            if (publicKey != null) {
                addressRecovered = "0x" + Keys.getAddress(publicKey);
                logger.info("recovery public address {} {} ", i + 1, addressRecovered);
                if (addressRecovered.equalsIgnoreCase(address)) {
                    match = true;
                    break;
                }
            }
        }

        return match;
    }

    public Timestamp getEventHappenedTimeStamp(String transactionHash) {

        while (true) {
            try {
                return new Timestamp(web3j.ethGetBlockByHash(web3j.ethGetTransactionReceipt(transactionHash).send().getResult().getBlockHash(), true).send().getResult().getTimestamp().longValueExact() * 1000);
            } catch (IOException e) {
                logger.error("getEventHappenedTimeStamp IOException: {} retrying ...", e, e);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public Timestamp getEventHappenedTimeStampByBlockHash(String blockHash) {

        String blockHashKey = "blockTimeStamp:blockHash:" + blockHash;
        try {
            Object redisValue = redisService.get(blockHashKey);
            if (null != redisValue) {
                return  new Timestamp (Long.parseLong(redisValue.toString()));
            }
        }catch (Exception e){
            log.error("getEventHappenedTimeStampByBlockHash redis read error：{}", e.getMessage());
        }

        while (true) {
            try {
                long timestamp = web3j.ethGetBlockByHash(blockHash, true).send().getResult().getTimestamp().longValueExact() * 1000;
                redisService.set(blockHashKey, String.valueOf(timestamp), 180, TimeUnit.SECONDS);
                return new Timestamp(timestamp);
            } catch (IOException e) {
                logger.error("getEventHappenedTimeStampByBlockHash IOException: {} retrying ...", e, e);
            }
            try {
                TimeUnit.MILLISECONDS.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

    }

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
    public List<Type> getUniswapV3PoolDetails(String tokenId){

        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new Uint256(Long.parseLong(tokenId)));

        List<TypeReference<?>> outputParameters = new ArrayList<>();
        outputParameters.add(new TypeReference<Uint96>() {}); //nonce
        outputParameters.add(new TypeReference<Address>() {}); //operator
        outputParameters.add(new TypeReference<Address>() {}); //token0
        outputParameters.add(new TypeReference<Address>() {}); //token1
        outputParameters.add(new TypeReference<Uint24>() {});//fee
        outputParameters.add(new TypeReference<Int24>() {}); //tickLower
        outputParameters.add(new TypeReference<Int24>() {}); //tickUpper
        outputParameters.add(new TypeReference<Uint128>() {}); //liquidity
        outputParameters.add(new TypeReference<Uint256>() {}); //feeGrowthInside0LastX128 返回池中代币的费用增长情况，用于计算流动性提供者的收益。
        outputParameters.add(new TypeReference<Uint256>() {}); //feeGrowthInside1LastX128 返回池中代币的费用增长情况，用于计算流动性提供者的收益。
        outputParameters.add(new TypeReference<Uint128>() {});
        outputParameters.add(new TypeReference<Uint128>() {});

        Function function = new Function("positions", inputParameters, outputParameters);

        ContractsConfig.ContractInfo nonFungiblePositionManagerCI = contractsConfig.getContractInfo("NonfungiblePositionManager");

        try {
            List<Type> returnList = callContractFunction(function, nonFungiblePositionManagerCI.getAddress());
            return returnList;
            //return returnList.get(0).getValue().toString();
        } catch (ExecutionException e) {
            // throw new RuntimeException(e);
            log.error("call getUniswapV3PoolDetails ExecutionException: ", e);
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
            log.error("call getUniswapV3PoolDetails InterruptedException: ", e);
        }
        return null;
    }

    public String getPoolAddress(String token0, String token1, int fee) {


        ContractsConfig.ContractInfo factoryAddressCI = contractsConfig.getContractInfo("v3CoreFactoryAddress");
        String factoryAddress = factoryAddressCI.getAddress();


        String poolAddress = CalculatePoolAddress.getPoolAddress(POOL_INIT_CODE_HASH,factoryAddress,token0,token1,BigInteger.valueOf(fee) );

        return Keys.toChecksumAddress(poolAddress);
    }


    public String getNftOwnerOf(String contractAddress, String tokenId) throws Exception {

        // https://stackoverflow.com/questions/52028956/web3j-java-function-call-returns-empty-list-on-solidity-contract
        TransactionManager manager = new ReadonlyTransactionManager(web3j, "0xA81C479AB649D8de1d07bAD978301aFaD2890608");
        ERC721 contract = ERC721.load(Keys.toChecksumAddress(contractAddress), this.web3j, manager/*Web3jUtils.credentials*/, new DefaultGasProvider());

        int m = 0, retryTimes = 20;
        while (m < retryTimes) {
            // use credentials get error: Empty value (0x) returned from contract web3j

            try {
                String owner = contract.ownerOf(new BigInteger(tokenId)).send();
                return owner;
            } catch (Exception e) {
                m++;
                if (m < retryTimes) {
                    log.error("获取Pool价格区间对应对应的NFT token: {} 对应的 owner 错误, 合约地址：{}, 错误：{}", tokenId, contractAddress, e);
                }
                else{
                   throw e;
                }
            }
        }

        return null;
    }

    /**
     * function slot0() external view returns (
     *     uint160 sqrtPriceX96,
     *     int24 tick,
     *     uint16 observationIndex,
     *     uint16 observationCardinality,
     *     uint16 observationCardinalityNext,
     *     uint8 feeProtocol,
     *     bool unlocked
     * );
     * @return
     */
    public List<Type> getUniswapV3PoolSlot0(String poolAddress){

        List<Type> inputParameters = new ArrayList<>();
//        inputParameters.add(new Uint256(Long.parseLong(tokenId)));

        List<TypeReference<?>> outputParameters = new ArrayList<>();
        outputParameters.add(new TypeReference<Uint160>() {});
        outputParameters.add(new TypeReference<Int24>() {});
        outputParameters.add(new TypeReference<Uint16>() {});
        outputParameters.add(new TypeReference<Uint16>() {});
        outputParameters.add(new TypeReference<Uint16>() {});
        outputParameters.add(new TypeReference<Uint8>() {});
        outputParameters.add(new TypeReference<Bool>() {});


        Function function = new Function("slot0", inputParameters, outputParameters);


        try {
            List<Type> returnList = callContractFunction(function, poolAddress);
            return returnList;
            //return returnList.get(0).getValue().toString();
        } catch (ExecutionException e) {
            // throw new RuntimeException(e);
            log.error("call getUniswapV3PoolSlot0 ExecutionException: ", e);
        } catch (InterruptedException e) {
            //throw new RuntimeException(e);
            log.error("call getUniswapV3PoolSlot0 InterruptedException: ", e);
        }
        return null;
    }

    public String sendBatchTransferRewardTransfer(List<Address> users, String rewardToken, List<Uint256> amounts) throws IOException, ExecutionException, InterruptedException {
        List<Type> inputParameters = new ArrayList<>();
        inputParameters.add(new DynamicArray(users));
        inputParameters.add(new Address(rewardToken));
        inputParameters.add(new DynamicArray(amounts));
        List<TypeReference<?>> outputParameters = new ArrayList<>();
        Function function = new Function("batchTransferReward", inputParameters, outputParameters);
        ContractsConfig.ContractInfo batchTransferReward = contractsConfig.getContractInfo("BatchTransferReward");
        return sendTransaction(function, batchTransferReward.getAddress());
    }
}
