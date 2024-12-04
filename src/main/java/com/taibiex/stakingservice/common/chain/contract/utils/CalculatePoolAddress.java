package com.taibiex.stakingservice.common.chain.contract.utils;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.generated.Bytes1;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.abi.datatypes.generated.Uint24;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

/**
 * @ClassName: CalculatePoolAddress
 * @Author: huanglefei
 * @CreateDate: 2023/8/18 22:04
 * @Description: 计算uniswap v3的池子地址
 * @Version: 1.0
 * /// @notice Deterministically computes the pool address given the factory and PoolKey
 * /// @param factory The Uniswap V3 factory contract address
 * /// @param key The PoolKey
 * /// @return pool The contract address of the V3 pool
 * function computeAddress(address factory, PoolKey memory key) internal pure returns (address pool) {
 * require(key.token0 < key.token1);
 * pool = address(
 * uint256(
 * keccak256(
 * abi.encodePacked(
 * hex'ff',
 * factory,
 * keccak256(abi.encode(key.token0, key.token1, key.fee)),
 * POOL_INIT_CODE_HASH
 * )
 * )
 * )
 * );
 * }
 **/
public class CalculatePoolAddress {
    public static void main(String[] args) {
        // UniswapV3
        // https://etherscan.io/address/0x840deeef2f115cf50da625f7368c24af6fe74410#readContract
        String poolInitCodeHash = "0xe34f199b19b2b4f47f68442619d555527d244f78a3297ea89325f843f87b8b54";
        String factory = "0x1F98431c8aD98523631AE4a59f267346ea31F984";
        String tokenA = "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2";
        String tokenB = "0xBe9895146f7AF43049ca1c1AE358B0541Ea49704";
        BigInteger fee = BigInteger.valueOf(500);
        String pool = getPoolAddress(poolInitCodeHash, factory, tokenA, tokenB, fee);
        System.out.println("交易池地址：" + pool);

    }

    public static String getPoolAddress(String poolInitCodeHash, String factory, String tokenA, String tokenB, BigInteger fee) {
        List<String> strings = sortTokens(tokenA, tokenB);
        tokenA = strings.get(0);
        tokenB = strings.get(1);

        Bytes1 parff = new Bytes1(Numeric.hexStringToByteArray("ff"));
        Bytes32 parInitCodeHash = new Bytes32(Numeric.hexStringToByteArray(poolInitCodeHash));

        String encodeFee = TypeEncoder.encode(new Uint24(fee));
        String encodeTokenA = TypeEncoder.encode(new Address(tokenA));
        StringBuilder tokenAddress = new StringBuilder();
        tokenAddress.append(encodeTokenA);
        tokenAddress.append(TypeEncoder.encode(new Address(tokenB)));
        tokenAddress.append(encodeFee);
        String tokenHash = Hash.sha3("0x" + tokenAddress.toString());

        StringBuilder msg = new StringBuilder();
        msg.append(Hex.toHexString(parff.getValue()));
        msg.append(factory.substring(2, factory.length()));
        msg.append(Numeric.cleanHexPrefix(tokenHash));
        msg.append(Hex.toHexString(parInitCodeHash.getValue()));
        String hash1 = Hash.sha3("0x" + msg.toString());
        //对应这段代码 uint(keccak256(abi.encode(tokenA,tokenB,fee)))
        BigInteger uintAddress = Numeric.toBigInt(hash1);

        DynamicArray<Uint256> array = new DynamicArray<>(Uint256.class, new Uint256(uintAddress));
        BigInteger uint256 = array.getValue().get(0).getValue();

        String s3 = Numeric.toHexString(uint256.toByteArray());
        String pairAddress = s3.substring(s3.length() - 40);
        return "0x" + pairAddress;
    }

    /**
     * 确保 token0 是较小的地址
     * @param tokenA
     * @param tokenB
     * @return
     */
    public static List<String> sortTokens(String tokenA, String tokenB) {
        Address address1 = new Address(tokenA);
        Address address2 = new Address(tokenB);
        BigInteger value1 = address1.toUint().getValue();
        BigInteger value2 = address2.toUint().getValue();
        List<String> result = new ArrayList<>();
        if (value1.compareTo(value2) < 0) {
            result.add(tokenA);
            result.add(tokenB);
        } else {
            result.add(tokenB);
            result.add(tokenA);
        }
        return result;
    }
}

