package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.ClaimRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimRecordRepository extends JpaRepository<ClaimRecord, Long> {

    List<ClaimRecord> findAllByClaimed(boolean claimed);

}
