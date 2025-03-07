package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.UserPoolReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPoolRewardRepository extends JpaRepository<UserPoolReward, Long> {

    List<UserPoolReward> findByEpoch(long epoch);

    List<UserPoolReward> findByUserAddressOrderByEpochDesc(String userAddress);

    List<UserPoolReward> findByUserAddressAndEpochBetween(String userAddress, long startEpoch, long endEpoch);

    List<UserPoolReward> findByUserAddressAndClaimed(String userAddress, boolean claimed);

    @Query(value = "select distinct epoch from user_pool_reward where user_address = :userAddress order by epoch desc", nativeQuery = true)
    List<Long> findEpochByUserAddressOrderByEpochDesc(@Param("userAddress") String userAddress);

    List<UserPoolReward> findAllByUserAddressAndEpochInOrderByEpochDesc(String userAddress, List<Long> epoch);

}
