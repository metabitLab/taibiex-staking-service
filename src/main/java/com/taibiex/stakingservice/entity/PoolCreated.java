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

    @Column(name = "tx_hash", nullable = false, length = 100)
    private String txHash;

    @Column(name = "token0", nullable = false, length = 100)
    private String token0;

    @Column(name = "token1", nullable = false, length = 100)
    private String token1;

    @Column(name = "fee", nullable = false, length = 64)
    private String fee;

    @Column(name = "tick_spacing", nullable = false, length = 64)
    private String tickSpacing;

    @Column(name = "pool", nullable = false, length = 100)
    private String pool;

}
