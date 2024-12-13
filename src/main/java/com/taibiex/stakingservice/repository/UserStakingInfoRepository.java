package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.UserStakingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserStakingInfoRepository extends JpaRepository<UserStakingInfo, Long> {

    List<UserStakingInfo> findByTxHash(String txHash);

    UserStakingInfo findFirstByUserAddressAndRangeIdOrderByIdDesc(String userAddress, long rangeId);

    @Query(value = "select\n" +
            "usi.id,\n" +
            "usi.epoch ,\n" +
            "usi.pool ,\n" +
            "usi.range_id ,\n" +
            "usi.total_amount ,\n" +
            "usi.total_amount0 ,\n" +
            "usi.total_amount1 ,\n" +
            "usi.staking_amount ,\n" +
            "usi.tx_hash ,\n" +
            "usi.user_address ,\n" +
            "usi.create_time ,\n" +
            "usi.last_update_time\n" +
            "from\n" +
            " user_staking_info usi ,\n" +
            "(\n" +
            " select u.range_id, max(id) id from user_staking_info u where u.epoch < :epoch and u.user_address = :userAddress and u.pool = :pool group by u.range_id ) a\n" +
            "where\n" +
            "usi.id = a.id\n" +
            "and usi.epoch <= :epoch and usi.user_address = :userAddress and usi.pool = :pool", nativeQuery = true)
    List<UserStakingInfo> findNewestByRangeId(@Param("epoch") long epoch, @Param("userAddress") String userAddress, @Param("pool") String pool);

}
