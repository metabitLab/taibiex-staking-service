package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.ClaimEvent;
import com.taibiex.stakingservice.entity.SPStaking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * (已解锁的)解质押事件(一笔提取所有的解质押事件) 或 提取某一笔质押事件
 */
@Repository
public interface ClaimRepository extends JpaRepository<ClaimEvent, Long> {

    ClaimEvent findByTxHash(String txHash);
    ClaimEvent findByTxHashAndClaimIndex(String txHash, String claimIndex);

    ClaimEvent findByUserAddress(String userAddress);

    ClaimEvent findByTxHashAndUserAddressAndClaimIndex(String txHash, String userAddress, String claimIndex);
}
