package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.BurnAndDecreaseLiquidity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface BurnAndDecreaseLiquidityRepository extends JpaRepository<BurnAndDecreaseLiquidity, Long> {

    BurnAndDecreaseLiquidity findByTxHash(String txHash);

    long countAllBySender(String sender);

    List<BurnAndDecreaseLiquidity> findAllBySenderOrderByCreateTime(String sender);

    List<BurnAndDecreaseLiquidity> findAllByPoolAndCreateTimeBetween(String pool, Timestamp createTime, Timestamp createTime2);

    List<BurnAndDecreaseLiquidity> findAllByPoolAndTickLowerAndTickUpperOrderByCreateTime(String pool, String tickLower, String tickUpper);

}
