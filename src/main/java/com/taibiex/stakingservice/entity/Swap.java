package com.taibiex.stakingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Entity
@Table(name = "swap", indexes = {
        @Index(name = "sender_idx", columnList = "sender"),
        @Index(name = "recipient_idx", columnList = "recipient"),
        @Index(name = "tx_hash_idx", columnList = "tx_hash", unique = true)
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Swap extends BaseEntity{

    @Column(name = "tx_hash", nullable = false, length = 100)
    private String txHash;

    @Column(name = "sender", nullable = false, length = 100)
    private String sender;

    @Column(name = "recipient", nullable = false, length = 100)
    private String recipient;

    @Column(name = "amount0", nullable = false, length = 100)
    private String amount0;

    @Column(name = "amount1", nullable = false, length = 100)
    private String amount1;

    @Column(name = "sqrt_price_x96", nullable = false)
    private String sqrtPriceX96;

    @Column(name = "liquidity", nullable = false, length = 100)
    private String liquidity;

    @Column(name = "tick", nullable = false)
    private String tick;
}
