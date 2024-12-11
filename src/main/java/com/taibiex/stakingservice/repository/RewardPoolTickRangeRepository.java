package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.RewardPoolTickRange;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RewardPoolTickRangeRepository extends JpaRepository<RewardPoolTickRange, Long> {
}
