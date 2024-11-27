package com.taibiex.stakingservice.config;

import jakarta.annotation.Resource;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;


@Configuration
public class ProfileConfig {

    @Resource
    private ApplicationContext applicationContext;

    public String getActiveProfile(){
        return applicationContext.getEnvironment().getActiveProfiles()[0];
    }
}
