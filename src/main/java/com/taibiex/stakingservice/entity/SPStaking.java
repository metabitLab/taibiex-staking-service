package com.taibiex.stakingservice.entity;

import com.taibiex.stakingservice.common.hibernate.Comment;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 单币质押/解质押事件记录
 */
@Entity
@Table(name = "sp_staking", indexes = {
        @Index(name = "user_idx", columnList = "user"),
        @Index(name = "type_idx", columnList = "type"),
        @Index(name = "tx_hash_idx", columnList = "tx_hash"),
        @Index(name = "unlock_block_idx", columnList = "unlock_block"),
        @Index(name = "claim_index_idx", columnList = "claim_index"),
        @Index(name = "claimed_idx", columnList = "claimed"),
        @Index(name = "tx_hash_type_idx", columnList = "tx_hash, type", unique = true),
        @Index(name = "create_time_idx", columnList = "create_time"),
        @Index(name = "last_update_time_idx", columnList = "last_update_time")
})

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SPStaking extends BaseEntity{

    @Column(name = "tx_hash", nullable = false, length = 100)
    private String txHash;

    @Column(name = "user", nullable = false, length = 100)
    private String userAddress;

    @Column(name = "amount", nullable = false, length = 100)
    private String amount;

    @Comment("质押类型: 1.质押  0.解除质押")
    @Column(name = "`type`", nullable = false)
    private  Short type;

    @Comment("解除质押后，需要等到这个块才能解锁(claim), 质押时，该字段为空字符串''")
    @Column(name = "`unlock_block`", nullable = false)
    private  String unlockBlock;

    @Comment("type为0时有效，表示奖励是否已经claim了")
    @Column(name = "`claimed`", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private  Boolean claimed;

    @Comment("type为0时有效，claimIndex对应claim事件的claimIndex,表示如果claim过奖励，对应的是这笔unstake记录")
    @Column(name = "`claim_index`", nullable = false, columnDefinition = "varchar(100) DEFAULT ''")
    private  String claimIndex;

}

