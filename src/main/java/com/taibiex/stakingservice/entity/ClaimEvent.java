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
        @Index(name = "tx_hash_idx", columnList = "tx_hash"),
        @Index(name = "claim_index_idx", columnList = "claim_index"),
        @Index(name = "user_tx_hash_index_idx", columnList = "tx_hash, user, claim_index", unique = true),
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

    @Comment("为 空字符串或null时，表示 表示一笔claim之前所有已解质押并且已解锁的奖励。 有值时，表示claim的单笔已解质押已解锁的奖励")
    @Column(name = "`claim_index`", nullable = false, columnDefinition = "varchar(64) default ''")
    private  String claimIndex;
}

