package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.ClaimEvent;
import com.taibiex.stakingservice.entity.SPStaking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * claim是领取解锁的token，不是领取奖励. claim那个得等7天才能解锁才能claim
 * claim是一次性领取所有解锁了的本金，claimIndex是选定哪一期解锁. 和奖励没关系
 * (已解锁的)解质押事件(一笔提取所有的解质押事件) 或 提取某一笔质押事件
 */
@Repository
public interface ClaimRepository extends JpaRepository<ClaimEvent, Long>, JpaSpecificationExecutor {

    ClaimEvent findByTxHash(String txHash);
    ClaimEvent findByTxHashAndClaimIndex(String txHash, String claimIndex);

    ClaimEvent findByUserAddress(String userAddress);

    ClaimEvent findByTxHashAndUserAddressAndClaimIndex(String txHash, String userAddress, String claimIndex);
}
