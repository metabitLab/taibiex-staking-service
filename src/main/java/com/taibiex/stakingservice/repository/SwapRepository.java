package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.Swap;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SwapRepository extends JpaRepository<Swap, Long> {

    Swap findByTxHash(String txHash);

    long countAllBySender(String sender);

    long countAllByRecipient(String recipient);

}
