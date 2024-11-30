package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.MintAndIncreaseLiquidity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MintAndIncreaseLiquidityRepository extends JpaRepository<MintAndIncreaseLiquidity, Long> {

    MintAndIncreaseLiquidity findByTxHash(String txHash);

    long countAllBySender(String sender);

}
