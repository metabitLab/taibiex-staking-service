package com.taibiex.stakingservice.repository;


import com.taibiex.stakingservice.entity.SPStaking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 单币质押/解质押事件记录
 */
@Repository
public interface SpStakingRepository extends JpaRepository<SPStaking, Long>, JpaSpecificationExecutor {

    SPStaking findByTxHash(String txHash);
    SPStaking findByTxHashAndType(String txHash, Short type);


    /*
        1）update或delete时必须使用@Modifying对方法进行注解，才能使得ORM知道现在要执行的是写操作
        2）有时候不加@Param注解参数，可能会报如下异常：
            org.springframework.dao.InvalidDataAccessApiUsageException: Name must not be null or empty!; nested exception is java.lang.IllegalArgumentException: Name must not be null or empty!
    */
    @Modifying
    @Query("update SPStaking sp set sp.claimed = true where sp.claimIndex in :claimIndexList")
    public void updateSPStakingByClaimIndexList(@Param(value = "claimIndexList") List<String> claimIndexList);

}
