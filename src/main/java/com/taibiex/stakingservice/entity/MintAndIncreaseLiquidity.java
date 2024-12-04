package com.taibiex.stakingservice.entity;

import com.taibiex.stakingservice.common.hibernate.Comment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "increase_liquidity", indexes = {
        @Index(name = "sender_idx", columnList = "sender"),
        @Index(name = "owner_idx", columnList = "owner"),
        @Index(name = "token_id_idx", columnList = "token_id"),
        @Index(name = "pool_idx", columnList = "pool"),
        @Index(name = "tx_hash_idx", columnList = "tx_hash", unique = true),

})

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MintAndIncreaseLiquidity extends BaseEntity{

    //Mint
    @Column(name = "tx_hash", nullable = false, length = 100)
    private String txHash;

    @Column(name = "sender", nullable = false, length = 100)
    private String sender;

    @Column(name = "owner", nullable = false, length = 100)
    private String owner;
    /**
     * 在 Uniswap V3 中，对于同一个 Pool，相同的 tickLower 和 tickUpper 只能对应一个流动性位置。
     * 如果想在相同的价格区间内增加流动性，您需要使用现有流动性位置的 tokenId 进行操作，而不能创建新的流动性位置。
     */
    @Column(name = "tick_lower", nullable = false, length = 64)
    private String tickLower;

    @Column(name = "tick_upper", nullable = false, length = 64)
    private String tickUpper;

    //IncreaseLiquidity事件中的liquidity,amount0，amount1与mint事件中的liquidity,amount0，amount1一致
    @Column(name = "amount", nullable = false, length = 100)
    private String amount;

    @Column(name = "amount0", nullable = false, length = 100)
    private String amount0;

    @Column(name = "amount1", nullable = false, length = 100)
    private String amount1;

    /**
     * 当您通过 NonfungiblePositionManager 的 mint 方法添加流动性时，它会返回一个 tokenId，这个 ID 代表了您创建的流动性 NFT。
     *  注意：只有第一次添加会生成NFT,但添加流动性的事件都会抛出
     *
     * 	查询流动性位置：
     * 	使用 NonfungiblePositionManager 合约的 positions(tokenId) 方法，可以获取与该 tokenId 相关的流动性位置的详细信息，包括与之关联的 Pool 地址。
     *
     *  uniswapV3中，添加流动性一定要通过NonfungiblePositionManager 合约吗？可以通过pool合约的mint方法吗？
     * 	    在 Uniswap V3 中，添加流动性确实需要通过 NonfungiblePositionManager 合约，而不能直接通过 Pool 合约的 mint 方法。
     *
     * 	    理由
     * 	    流动性作为 NFT：
     * 	    在 Uniswap V3 中，流动性提供者的流动性是通过非同质化代币（NFT）来管理的。每个 NFT 代表一个特定的流动性位置，包括价格区间、代币对和流动性数量等信息。
     * 	    NonfungiblePositionManager 合约负责创建和管理这些 NFT，提供流动性时会生成一个新的 NFT。
     * 	    Pool 合约的功能：
     * 	    Pool 合约本身不提供直接的流动性添加功能。它主要负责管理流动性池的状态、处理交易和维护价格信息。
     * 	    Pool 合约没有 mint 方法；流动性提供者必须通过 NonfungiblePositionManager 来添加流动性。
     */

    //IncreaseLiquidity
    //理论上存在这个的原因: 在 Uniswap V3 中，添加流动性确实需要通过 NonfungiblePositionManager 合约，而不能直接通过 Pool 合约的 mint 方法
    @Comment("添加流动性生成的NFT, 有可能为 空字符串： 通过Pool 添加的流动性时(理论上存在这个)")
    @Column(name = "token_id", nullable = false, length = 100)
    private String tokenId;

    @Comment("流动性池子的地址")
    @Column(name = "pool", nullable = false, length = 100)
    private String pool;

}

