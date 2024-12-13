package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.UserPoolReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPoolRewardRepository extends JpaRepository<UserPoolReward, Long> {

    List<UserPoolReward> findByEpoch(long epoch);

    List<UserPoolReward> findByUserAddressOrderByEpochDesc(String userAddress);
}
