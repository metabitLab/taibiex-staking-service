package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.PoolCreated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PoolCreatedRepository extends JpaRepository<PoolCreated, Long> {

    PoolCreated findByTxHash(String txHash);

}
