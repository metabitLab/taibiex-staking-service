package com.taibiex.stakingservice.service;

import com.taibiex.stakingservice.entity.ActivityUser;
import com.taibiex.stakingservice.repository.ActivityUserRepository;
import org.springframework.stereotype.Service;

@Service
public class ActivityUserService {

    private final ActivityUserRepository activityUserRepository;

    public ActivityUserService(ActivityUserRepository activityUserRepository) {
        this.activityUserRepository = activityUserRepository;
    }

    public void save(ActivityUser activityUser){
        ActivityUser user = activityUserRepository.findByUserAddress(activityUser.getUserAddress());
        if (user == null){
            activityUserRepository.save(activityUser);
        }
    }
}
