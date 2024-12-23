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
 * (已解锁的)解质押事件(一笔提取所有的解质押事件) 或 提取某一笔质押事件
 */
@Entity
@Table(name = "claim_staking", indexes = {
        //注意：varchar 大于255就不让加索引了。注意：JPA不支持前缀索引
        //@Index(name = "user_tx_hash_index_idx", columnList = "tx_hash, user, claim_index(255)", unique = true),
        @Index(name = "user_tx_hash_index_idx", columnList = "tx_hash, user, claim_index_hash", unique = true),
        @Index(name = "user_idx", columnList = "user"),
        @Index(name = "tx_hash_idx", columnList = "tx_hash"),
        //注意：varchar 大于255就不让加索引了, 所以增加一个hash字段，主要是为了唯一索引使用
        //@Index(name = "claim_index_idx", columnList = "claim_index"),
        @Index(name = "claim_index_hash_idx", columnList = "claim_index_hash"),
        @Index(name = "create_time_idx", columnList = "create_time"),
        @Index(name = "last_update_time_idx", columnList = "last_update_time"),
}
)

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ClaimEvent extends BaseEntity {

    @Column(name = "tx_hash", nullable = false, length = 100)
    private String txHash;

    @Column(name = "user", nullable = false, length = 100)
    private String userAddress;

    @Column(name = "amount", nullable = false, length = 100)
    private String amount;

    @Comment("为 ,连接的字符串 表示一笔claim之前所有已解质押并且已解锁的奖励。如:1,2,3 单个值时，表示claim的单笔已解质押已解锁的奖励, 如: 5")
    @Column(name = "`claim_index`", nullable = false, columnDefinition = "varchar(8192) default ''")
    private String claimIndex;


    @Comment("varchar 大于255就不让加索引了, 所以增加一个hash字段，主要是为了唯一索引使用, 值为claim_index的 SHA-256 哈希值")
    @Column(name = "`claim_index_hash`", nullable = false, columnDefinition = "varchar(100) default ''")
    private String claimIndexHash;
}

