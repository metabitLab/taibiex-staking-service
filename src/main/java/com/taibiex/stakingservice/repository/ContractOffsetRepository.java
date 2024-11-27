package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.ContractOffset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface ContractOffsetRepository extends JpaRepository<ContractOffset, Long> {

    ContractOffset findByContractAddress(String contractAddress);

    List<ContractOffset> findAll();

    @Query("select min(c.blockOffset) from ContractOffset c")
    BigInteger findMinBlockOffset();

}
