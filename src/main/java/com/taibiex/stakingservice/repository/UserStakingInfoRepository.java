package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.UserStakingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserStakingInfoRepository extends JpaRepository<UserStakingInfo, Long> {
}
