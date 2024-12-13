package com.taibiex.stakingservice.common.utils;

import java.math.BigInteger;

/**
 * 判断X轴的两个区间是否相交,  如 [1,5] 和 [4,8]
 */
public class XIntervalOverlap {

    public static void main(String[] args) {
        BigInteger[] interval1 = {BigInteger.valueOf(4), BigInteger.valueOf(8)};
        BigInteger[] interval2 = {BigInteger.valueOf(1), BigInteger.valueOf(5)};

        boolean isOverlapping = areIntervalsOverlapping(interval1, interval2);
        System.out.println("Intervals overlap: " + isOverlapping); // true
    }

    public static BigInteger min(BigInteger a, BigInteger b) {
        return (a.compareTo(b) < 0) ? a : b; // 比较并返回较小的值
    }

    public static BigInteger max(BigInteger a, BigInteger b) {
        return (a.compareTo(b) > 0) ? a : b; // 比较并返回较小的值
    }

    // 自定义方法，判断是否小于或等于
    public static boolean isLessThanOrEqual(BigInteger a, BigInteger b) {
        return a.compareTo(b) <= 0; // 使用 compareTo 方法
    }

    // 你也可以添加其他比较方法
    public static boolean isGreaterThan(BigInteger a, BigInteger b) {
        return a.compareTo(b) > 0;
    }

    public static boolean isEqual(BigInteger a, BigInteger b) {
        return a.compareTo(b) == 0;
    }

    public static boolean areIntervalsOverlapping(BigInteger[] interval1, BigInteger[] interval2) {
        // 确保区间有效
        if (interval1.length != 2 || interval2.length != 2) {
            throw new IllegalArgumentException("Each interval must have exactly two elements.");
        }

        // 提取区间的起始和结束值
        BigInteger start1 = min(interval1[0], interval1[1]);
        BigInteger end1 = max(interval1[0], interval1[1]);
        BigInteger start2 = min(interval2[0], interval2[1]);
        BigInteger end2 = max(interval2[0], interval2[1]);

        // 判断是否相交
        //return start1 <= end2 && start2 <= end1;
        return isLessThanOrEqual(start1, end2) && isLessThanOrEqual(start2, end1);
    }
}
