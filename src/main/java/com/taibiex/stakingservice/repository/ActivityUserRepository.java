package com.taibiex.stakingservice.repository;

import com.taibiex.stakingservice.entity.ActivityUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ActivityUserRepository extends JpaRepository<ActivityUser, Long> {

    ActivityUser findByUserAddress(String userAddress);

}
