package com.taibiex.stakingservice.config;

import org.springframework.boot.autoconfigure.batch.BatchProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

@EnableScheduling
@Configuration
public class ScheduledTaskConfiguration implements SchedulingConfigurer {
    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        Method[] methods = BatchProperties.Job.class.getMethods();
        final AtomicInteger corePoolSize = new AtomicInteger();
        if (Objects.nonNull(methods) && methods.length > 0) {
            Arrays.stream(methods).forEach(method -> {
                final Scheduled annotation = method.getAnnotation(Scheduled.class);
                if (Objects.nonNull(annotation)) {
                    corePoolSize.incrementAndGet();
                }
            });
        }
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(corePoolSize.get());
        taskRegistrar.setScheduler(executor);
    }
}
