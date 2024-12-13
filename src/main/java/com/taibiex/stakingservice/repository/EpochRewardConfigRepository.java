package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.EpochRewardConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EpochRewardConfigRepository extends JpaRepository<EpochRewardConfig, Long> {

    EpochRewardConfig findByEpoch(long epoch);

    @Query(value = "SELECT * FROM epoch_reward_config ORDER BY epoch DESC LIMIT 1" , nativeQuery = true)
    EpochRewardConfig findFirstOrderByEpochDesc();

}
