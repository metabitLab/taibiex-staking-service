package com.taibiex.stakingservice.config;


import com.taibiex.stakingservice.common.chain.contract.listener.impl.BlockEventListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ListenerConfig {

    @Bean
    public BlockEventListener blockEventListenerDelay0() {
        return new BlockEventListener();
    }

    @Bean
    public BlockEventListener blockEventDelayListener3() {
        return new BlockEventListener();
    }

    @Bean
    public BlockEventListener blockEventDelayListener100() {
        return new BlockEventListener();
    }
}