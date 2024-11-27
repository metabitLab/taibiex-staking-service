package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.Mint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MintRepository extends JpaRepository<Mint, Long> {

    Mint findByTxHash(String txHash);

    long countAllBySender(String sender);

}
