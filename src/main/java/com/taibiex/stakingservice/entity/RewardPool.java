package com.taibiex.stakingservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.taibiex.stakingservice.common.hibernate.Comment;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 后台配置的 组那些LP需要给奖励(哪些池子有奖励)
 * 如：USDT-USDC[USDT-USDC-0.01%需要配置这个池子的地址，所有监听事件入口都是当前池子](比如设置了三个价格区间的占比)，Tabi-usdt(比如设置了两个价格区间的占比)，
 *
 * 假设一个epoch 是 1天， 一个100奖励是100个。   注意：上面所有币对的所有价格区间的占比加起来 为1.
 *
 * 比如： USDT-USDC三个区间占比是： [10块-20块]10% [30块-50块]20% [60块-70块]20%,   Tabi-usdt设置了两个价格区间的占比 40% 10%.
 *
 * 比如说 USDT-USDC的[10块-20块]10%里面有2个人添加流动性， 第一个人添加了10个流动性，时间为2天
 *
 *
 * 每个epoch的奖励单独计算： epoch2下 某个User的奖励: 这个User的在epoch2下质押时间和质押数量 占 所有用户在epoch2下的质押时间和质押数量 的总和
 *
 * 假设用户是 epoch1质押的，那么我到epoch2(epoch1结束)才能计算用户的奖励。所以要看当前是在哪个epoch, 比如说当前是在第epoch100, 那么返回给前端可提前的奖励是 epoch0到epoch99.
 *
 * 假设用户是 epoch1质押的, 当前是epoch3, 那么我计算用户在epoch2的质押时长为：整个epoch2的时间（如果在epoch2的某个时间点t1用户手动移除了流动性，那么epoch2的流动性时长就是移除了流动性的时间点(t1) - epoch2的起始时间）。计算epoch1的质押时长为：epoch1 的结束时间减去用户的添加流动性时间（假设一个epoch 是 1天，Epoch time len = 86400秒）。由于当前是epoch3,所以等到epoch4才能计算出epoch3奖励。
 *
 *
 *
 * 需要给前端返回，这个流动性币对的这个区间的这个所有epoch的每个epoch的奖励，如果当前是epoch100,则返回epoch0 到 epoch0到epoch99的奖励，并且如果提取，epoch 98, 则把epoch98到epoch0的所有奖励都提取出来
 *
 *
 * 10189 start1 end1  amount  池子地址关联
 * 	  start2 end2
 * 	  	  ....
 * 	  startn endn
 *
 *
 * pool start1 end1  amount 池子地址关联
 * 	 start2 end2
 * 	  	  ....
 * 	 startn endn
 *
 */

//在 JPA 中，@Index 注解的 columnList 属性填写的是数据库表中的列名称，而不是 Java 类中的属性名称
@Entity
@Table(name = "reward_pools_config", indexes = {
        @Index(name = "token0_idx", columnList = "token0"),
        @Index(name = "token1_idx", columnList = "token1"),
        @Index(name = "pool_idx", columnList = "pool", unique = true),
        @Index(name = "pool_pair_idx", columnList = "token0, token1, fee", unique = true),
        //token0, token1, fee <==> pool (address)
        @Index(name = "create_time_idx", columnList = "create_time"),
        @Index(name = "last_update_time_idx", columnList = "last_update_time")
})

//@Data and add the @JsonIgnore for @OneToMany //https://blog.csdn.net/qq_44766883/article/details/107126456 and https://blog.csdn.net/qq_41621362/article/details/103997237 for fix: StackOverflowError
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Comment("后台配置 奖励的池子列表")
public class RewardPool extends BaseEntity{

    @Comment("流动性池地址")
    @Column(name = "pool", nullable = false, length = 100)
    private String pool;

    @Column(name = "token0", nullable = false, length = 100)
    private String token0;

    @Column(name = "token1", nullable = false, length = 100)
    private String token1;

    /**
     * 1000 就表示 10%  ( 1000/10000), 100 就表示 1%  ( 100/10000)
     */
    @Comment("1000 就表示 10%  ( 1000/10000), 100 就表示 1%  ( 100/10000)")
    @Column(name = "fee", nullable = false, length = 64)
    private String fee;

    //https://cloud.tencent.com/developer/article/2431379

    /**
     * mappedBy 属性指定了关系的反向端，即 RewardPoolTickRange 类中的 reward_pool 属性。
     * cascade = CascadeType.ALL 表示当对 RewardPool 进行持久化操作时，相关的 RewardPoolTickRange 实体也会自动进行持久化。
     */
    @JsonIgnore  //在一的一方加的 //@Data and add the @JsonIgnore for @OneToMany //https://blog.csdn.net/qq_44766883/article/details/107126456 and https://blog.csdn.net/qq_41621362/article/details/103997237 for fix: StackOverflowError
    @OneToMany(mappedBy = "rewardPool", fetch = FetchType.EAGER) //https://blog.csdn.net/qq_43618881/article/details/105214416
    private List<RewardPoolTickRange> rewardPoolTickRanges;


}
