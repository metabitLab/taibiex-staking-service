package com.taibiex.stakingservice.entity;

import com.taibiex.stakingservice.common.hibernate.Comment;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

/**
 * 后台配置的对应池子的奖励区间范围，即哪些tick范围内，给多少比例的奖励
 */

//在 JPA 中，@Index 注解的 columnList 属性填写的是数据库表中的列名称，而不是 Java 类中的属性名称
@Entity
@Table(name = "reward_pools_tick_range_config", indexes = {
        @Index(name = "pool_idx", columnList = "pool"),
        @Index(name = "pool_tick_idx", columnList = "pool, tick_lower, tick_upper, reward_ratio", unique = true)
})
@Data
@AllArgsConstructor
@NoArgsConstructor
@Comment("后台配置 奖励的池子对应的价格区间（及奖励）的列表")
public class RewardPoolTickRange extends BaseEntity {

    @Comment("流动性池地址")
    @Column(name = "pool", nullable = false, length = 100)
    private String pool;

    /**
     * Uniswap V3 的 tick 值通常在 -887272 到 887272 之间。超出该范围可能会导致计算错误。
     * <p>
     * function tickToPrice(tick) {
     * return Math.pow(1.0001, tick);
     * }
     */
    @Column(name = "tick_lower", nullable = false, length = 64)
    private String tickLower;

    @Column(name = "tick_upper", nullable = false, length = 64)
    private String tickUpper;

    /**
     * 1000 就表示 10%  ( 1000/10000), 100 就表示 1%  ( 100/10000)
     */
    @Comment("1000 就表示 10%  ( 1000/10000), 100 就表示 1%  ( 100/10000)")
    @Column(name = "reward_ratio", nullable = false, length = 64)
    private BigInteger rewardRatio;

    /**
     * 在
     *
     * @JoinColumn(name = "reward_pool_id") 指定外键列的名称，表示在 RewardPoolTickRange 表中存储 RewardPool 的 ID。
     * name 属性的值应该与数据库中对应的外键列的名称完全匹配。如果数据库中外键列的名称是 reward_pool_id，
     * 那么在 @JoinColumn 中 name 属性也应该设置为 "reward_pool_id"。
     */
    @ManyToOne(fetch = FetchType.EAGER) //https://blog.csdn.net/qq_43618881/article/details/105214416
    //JoinColumn 指定外键列：nullable = true 表示在 RewardPoolTickRange 表中有一个 reward_pool_id 列，但不在数据库中强制执行外键约束
    @JoinColumn(name = "reward_pool_id", referencedColumnName = "id", nullable = true, foreignKey = @ForeignKey(value = ConstraintMode.NO_CONSTRAINT))
    // 允许为 null
    private RewardPool rewardPool;


    /**
     * 获取指定tick对应的价格，Uniswap V3 的 tick 值通常在 -887272 到 887272 之间。超出该范围可能会导致计算错误。
     *
     * @param tick
     * @return
     */
    public Double getPriceFromTick(Integer tick) {
        return Math.pow(1.0001, tick);
    }

}
