package com.taibiex.stakingservice.entity;

import com.taibiex.stakingservice.common.hibernate.Comment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Data;

/**
 * 监听到用户流动性事件后对应的数据
 */
@Entity
@Table(name = "user_staking_info", indexes = {
        @Index(name = "user_idx", columnList = "user_address"),
        @Index(name = "range_id_idx", columnList = "range_id"),
        @Index(name = "pool_idx", columnList = "pool"),
        @Index(name = "epoch_idx", columnList = "epoch"),
        @Index(name = "tx_hash_idx", columnList = "tx_hash", unique = true)
})
@Data
public class UserStakingInfo extends BaseEntity{

    @Column(name = "epoch", nullable = false)
    private long epoch;

    @Column(name = "tx_hash", nullable = false, length = 100)
    private String txHash;

    @Column(name = "user_address", nullable = false, length = 100)
    private String userAddress;

    @Column(name = "range_id")
    private long rangeId;

    @Column(name = "total_amount", nullable = false, length = 100)
    private String totalAmount;

    @Column(name = "total_amount0", nullable = false, length = 100)
    private String totalAmount0;

    @Column(name = "total_amount1", nullable = false, length = 100)
    private String totalAmount1;

    @Column(name = "staking_amount", nullable = false)
    private String stakingAmount;

    @Comment("流动性池子的地址")
    @Column(name = "pool", nullable = false, length = 100)
    private String pool;

}
