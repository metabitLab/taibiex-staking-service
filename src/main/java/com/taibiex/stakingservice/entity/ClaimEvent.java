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
 * claim是领取解锁的token，不是领取奖励. claim那个得等7天才能解锁才能claim
 * claim是一次性领取所有解锁了的本金，claimIndex是选定哪一期解锁. 和奖励没关系
 *  (已解锁的)解质押事件(一笔提取所有的解质押事件) 或 提取某一笔质押事件
 */
@Entity
@Table(name = "claim_staking", indexes = {
        @Index(name = "user_idx", columnList = "user"),
        @Index(name = "type_idx", columnList = "type"),
        @Index(name = "tx_hash_idx", columnList = "tx_hash"),
        @Index(name = "unlock_block_idx", columnList = "unlock_block"),
        @Index(name = "user_tx_hash_index_idx", columnList = "tx_hash, user, index", unique = true),
})

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimEvent extends BaseEntity{

    @Column(name = "tx_hash", nullable = false, length = 100)
    private String txHash;

    @Column(name = "user", nullable = false, length = 100)
    private String userAddress;

    @Column(name = "amount", nullable = false, length = 100)
    private String amount;

    @Comment("claim单笔解质押时的索引，为 空字符串时 表示一笔claim所有")
    @Column(name = "`index`", nullable = false,columnDefinition = "varchar(64) default ''")
    private  String claimIndex;
}

