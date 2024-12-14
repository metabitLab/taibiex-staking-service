package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.LiquidityEvent;
import com.taibiex.stakingservice.entity.RewardPoolTickRange;
import com.taibiex.stakingservice.entity.SPStaking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 单币质押/解质押事件记录
 */
@Repository
public interface SpStakingRepository extends JpaRepository<SPStaking, Long>, JpaSpecificationExecutor {

    SPStaking findByTxHash(String txHash);
    SPStaking findByTxHashAndType(String txHash, Short type);

}
