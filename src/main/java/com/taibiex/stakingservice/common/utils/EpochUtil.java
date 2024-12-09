package com.taibiex.stakingservice.common.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EpochUtil {

    @Value("${epoch.startTimestamp}")
    private String startTimestamp;

    @Value("${epoch.unit}")
    public String epochUnit;

    /**
     * 获取当前时间戳对应的epoch
     * @return
     */
    public long getCurrentEpoch() {
        return getEpoch(System.currentTimeMillis());
    }

    /**
     * 获取时间戳对应的epoch
     */
    public long getEpoch(long timestamp) {
        return (timestamp - Long.parseLong(startTimestamp)) / Long.parseLong(epochUnit);
    }

    /**
     * 获取epoch的开始时间
     */
    public long getEpochStartTime(long epoch) {
        return Long.parseLong(startTimestamp) + epoch * Long.parseLong(epochUnit);
    }

    /**
     * 获取epoch的结束时间
     */
    public long getEpochEndTime(long epoch) {
        return getEpochStartTime(epoch) + Long.parseLong(epochUnit);
    }

    /**
     * 获取当前epoch的开始时间
     */
    public long getCurrentEpochStartTime() {
        return getEpochStartTime(getCurrentEpoch());
    }

    /**
     * 获取两个时间戳之间的epoch数
     */
    public long getEpochBetween(long start, long end) {
        return (end - start) / Long.parseLong(epochUnit);
    }

    /**
     * 获取两个时间戳之间的epoch列表
     */
    public long[] getEpochList(long start, long end) {
        long epoch = getEpochBetween(start, end);
        long[] epochList = new long[(int) epoch + 1];
        long startEpoch = getEpoch(start);
        for (int i = 0; i <= epoch; i++) {
            epochList[i] = startEpoch + i;
        }
        return epochList;
    }

    /**
     * 获取两个时间戳之间的epoch结束时间列表
     */
    public long[] getEpochEndTimeList(long start, long end) {
        long epoch = getEpochBetween(start, end);
        long[] epochEndTimeList = new long[(int) epoch + 1];
        long startEpoch = getEpoch(start);
        for (int i = 0; i <= epoch; i++) {
            epochEndTimeList[i] = getEpochEndTime(startEpoch + i);
        }
        return epochEndTimeList;
    }

}
