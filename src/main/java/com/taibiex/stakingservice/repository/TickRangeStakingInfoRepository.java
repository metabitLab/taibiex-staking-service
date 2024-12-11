package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.TickRangeStakingInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TickRangeStakingInfoRepository extends JpaRepository<TickRangeStakingInfo, Long> {
}
