package com.taibiex.stakingservice.common.chain.contract.task.listener;

import com.taibiex.stakingservice.common.chain.contract.listener.impl.BlockEventListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TasksListener {

    /**
     * 分布式锁本可解决该问题，但是由于锁有最长时间限制，就会一定会存在锁超时(但是任务没有执行完)被释放的问题，造成扫块任务可能同时重入的问题，增加本地同步块
     * 保证每个任务执行完了，在进入下一次定时去执行该任务
     */
    private static final Object blockListenerTaskKey = new Object();
    private static boolean lockBlockListenerTaskFlag = false;

    private static final Object blockListenerDelay3TaskKey = new Object();
    private static boolean lockBlockListenerDelay3TaskFlag = false;

    private static final Object blockListenerDelayTaskKey = new Object();
    private static boolean lockBlockListenerDelayTaskFlag = false;

    //springboot scheduled 解决多定时任务不执行的问题，多线程配置的几种方式 https://blog.csdn.net/liaoyi9203/article/details/109842925

    @Autowired
    @Qualifier("blockEventListenerDelay0") //需要对应ListenerConfig中的Bean的名称
    BlockEventListener blockEventListener;

    @Autowired
    @Qualifier("blockEventDelayListener3")//需要对应ListenerConfig中的Bean的名称
    BlockEventListener blockEventDelayListener3;

    @Autowired
    @Qualifier("blockEventDelayListener100")//需要对应ListenerConfig中的Bean的名称
    BlockEventListener blockEventDelayListener15;

    @Async
    @Scheduled(cron = "0/6 * * * * ?")
    public void scanBlockEvent() {


        synchronized (blockListenerTaskKey) {
            if (TasksListener.lockBlockListenerTaskFlag) {
                log.warn("区块链事件扫描任务 已经在执行,等待执行完成...");
                return;
            }
            TasksListener.lockBlockListenerTaskFlag = true;
        }

        log.info("开始执行 区块链事件扫描任务");
        try {
            blockEventListener.start(0, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        TasksListener.lockBlockListenerTaskFlag = false;

        log.info("结束执行 区块链事件扫描任务");
    }

    @Async
    @Scheduled(cron = "0/8 * * * * ?")
    public void scanBlockEventDelay15() {

        synchronized (blockListenerDelay3TaskKey) {
            if (TasksListener.lockBlockListenerDelay3TaskFlag) {
                log.warn("Delay15区块链事件扫描任务 已经在执行,等待执行完成...");
                return;
            }
            TasksListener.lockBlockListenerDelay3TaskFlag = true;
        }

        log.info("开始执行 Delay15区块链事件扫描任务");
        try {
            blockEventDelayListener3.start(15, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }

        TasksListener.lockBlockListenerDelay3TaskFlag = false;

        log.info("结束执行 Delay15区块链事件扫描任务");
    }


    @Async
    @Scheduled(cron = "0/10 * * * * ?")
    public void scanBlockEventDelay100() {

        synchronized (blockListenerDelayTaskKey) {
            if (TasksListener.lockBlockListenerDelayTaskFlag) {
                log.warn("Delay100 区块链事件扫描任务 已经在执行,等待执行完成...");
                return;
            }
            TasksListener.lockBlockListenerDelayTaskFlag = true;
        }

        log.info("开始执行 Delay100 区块链事件扫描任务");
        try {

            blockEventDelayListener15.start(100, null, null);

        } catch (Exception e) {
            e.printStackTrace();
        }

        TasksListener.lockBlockListenerDelayTaskFlag = false;

        log.info("结束执行 Delay100 区块链事件扫描任务");
    }

}
