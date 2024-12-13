package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.TickRangeStakingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TickRangeStakingInfoRepository extends JpaRepository<TickRangeStakingInfo, Long> {

    List<TickRangeStakingInfo> findByTxHash(String txHash);

    TickRangeStakingInfo findFirstByPoolAndRangeIdOrderByIdDesc(String pool, long rangeId);

    @Query(value = "select\n" +
                "t.id,\n" +
                "t.epoch ,\n" +
                "t.pool ,\n" +
                "t.range_id ,\n" +
                "t.total_amount ,\n" +
                "t.total_amount0 ,\n" +
                "t.total_amount1 ,\n" +
                "t.staking_amount ,\n" +
                "t.tx_hash ,\n" +
                "t.create_time ,\n" +
                "t.last_update_time\n" +
            "from tick_range_staking_info t,\n" +
                "(select  \n" +
                    "trsi.range_id, max(id) id \n" +
                "from tick_range_staking_info trsi \n" +
                "where trsi.epoch <= :epoch and trsi.pool = :pool\n" +
                "group by\n" +
                "trsi.range_id ) a\n" +
            "where\n" +
            "t.id = a.id and t.epoch <= :epoch and t.pool = :pool \n", nativeQuery = true)
    List<TickRangeStakingInfo> findNewestByEpoch(@Param("epoch") long epoch, @Param("pool") String pool);
}
