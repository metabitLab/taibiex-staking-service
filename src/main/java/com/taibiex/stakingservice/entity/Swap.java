package com.taibiex.stakingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "sender")
    private String sender;

    @Column(name = "recipient")
    private String recipient;

    @Column(name = "amount0")
    private String amount0;

    @Column(name = "amount1")
    private String amount1;

    @Column(name = "sqrt_price_x96")
    private String sqrtPriceX96;

    @Column(name = "liquidity")
    private String liquidity;

    @Column(name = "tick")
    private String tick;
}
