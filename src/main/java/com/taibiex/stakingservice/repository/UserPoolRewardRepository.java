package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.UserPoolReward;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserPoolRewardRepository extends JpaRepository<UserPoolReward, Long> {
}
