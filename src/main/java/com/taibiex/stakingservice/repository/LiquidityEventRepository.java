package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.LiquidityEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface LiquidityEventRepository extends JpaRepository<LiquidityEvent, Long> {

    LiquidityEvent findByTxHash(String txHash);
    LiquidityEvent findByTxHashAndType(String txHash, Short type);
    List<LiquidityEvent> findAllBySenderOrderByCreateTime(String sender);

    List<LiquidityEvent> findAllByPoolAndTickLowerAndTickUpperAndCreateTimeBeforeOrderByCreateTime(String pool, String tickLower, String tickUpper, Timestamp createTime);

}
