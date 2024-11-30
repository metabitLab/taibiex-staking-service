package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.ContractOffset;
import com.taibiex.stakingservice.entity.RewardPool;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface RewardPoolRepository extends JpaRepository<RewardPool, Long> {


    /**
     * 这个接口不需要分页
     * @return
     */

    List<RewardPool> findAll();

    RewardPool findByPool(String poolAddress);

}
