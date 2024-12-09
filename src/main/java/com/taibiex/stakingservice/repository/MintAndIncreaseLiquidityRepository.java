package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.MintAndIncreaseLiquidity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MintAndIncreaseLiquidityRepository extends JpaRepository<MintAndIncreaseLiquidity, Long> {

    MintAndIncreaseLiquidity findByTxHash(String txHash);

    long countAllBySender(String sender);

    List<MintAndIncreaseLiquidity> findAllBySenderOrderByCreateTime(String sender);

    List<MintAndIncreaseLiquidity> findAllByPoolAndCreateTimeBetween(String pool, long start, long end);

    List<MintAndIncreaseLiquidity> findAllByPoolAndTickLowerAndTickUpperOrderByCreateTime(String pool, String tickLower, String tickUpper);

}
