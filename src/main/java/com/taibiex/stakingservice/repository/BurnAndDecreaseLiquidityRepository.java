package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.BurnAndDecreaseLiquidity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BurnAndDecreaseLiquidityRepository extends JpaRepository<BurnAndDecreaseLiquidity, Long> {

    BurnAndDecreaseLiquidity findByTxHash(String txHash);

    long countAllBySender(String sender);

}
