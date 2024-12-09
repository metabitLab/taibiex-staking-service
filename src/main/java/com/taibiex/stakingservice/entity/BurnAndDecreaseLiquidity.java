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
@Table(name = "decrease_liquidity", indexes = {
        @Index(name = "sender_idx", columnList = "sender"),
        @Index(name = "owner_idx", columnList = "owner"),
        @Index(name = "token_id_idx", columnList = "token_id"),
        @Index(name = "pool_idx", columnList = "pool"),
        @Index(name = "tx_hash_idx", columnList = "tx_hash", unique = true),
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BurnAndDecreaseLiquidity extends LiquidityEvent{

    @Column(name = "tx_hash", nullable = false, length = 100)
    private String txHash;

    @Column(name = "sender", nullable = false, length = 100)
    private String sender;

    @Column(name = "owner", nullable = false, length = 100)
    private String owner;

    @Column(name = "tick_lower", nullable = false, length = 64)
    private String tickLower;

    @Column(name = "tick_upper", nullable = false, length = 64)
    private String tickUpper;

    @Column(name = "amount", nullable = false, length = 100)
    private String amount;

    @Column(name = "amount0", nullable = false, length = 100)
    private String amount0;

    @Column(name = "amount1", nullable = false, length = 100)
    private String amount1;

    //DecreaseLiquidity
    //理论上存在这个的原因: 在 Uniswap V3 中，移除流动性确实需要通过 NonfungiblePositionManager 合约，而不能直接通过 Pool 合约的 mint 方法
    @Comment("添加流动性时生成的NFT, 有可能为 空字符串： 通过Pool 添加的流动性时(理论上存在这个)")
    @Column(name = "token_id", nullable = false, length = 100)
    private String tokenId;

    @Comment("流动性池子的地址")
    @Column(name = "pool", nullable = false, length = 100)
    private String pool;
}

