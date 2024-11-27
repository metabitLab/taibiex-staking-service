package com.taibiex.stakingservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pool_created", indexes = {
        @Index(name = "token0_idx", columnList = "token0"),
        @Index(name = "token1_idx", columnList = "token1"),
        @Index(name = "pool_idx", columnList = "pool"),
        @Index(name = "tx_hash_idx", columnList = "tx_hash", unique = true)
})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PoolCreated extends BaseEntity{

    @Column(name = "tx_hash")
    private String txHash;

    @Column(name = "token0")
    private String token0;

    @Column(name = "token1")
    private String token1;

    @Column(name = "fee")
    private String fee;

    @Column(name = "tick_spacing")
    private String tickSpacing;

    @Column(name = "pool")
    private String pool;

}
