package com.hxq.smart_campus.util;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TimeConflictUtils {

    /**
     * 检查两个节次范围是否有时间重叠
     * @param startSection1 第一个范围的开始节次
     * @param endSection1 第一个范围的结束节次
     * @param startSection2 第二个范围的开始节次
     * @param endSection2 第二个范围的结束节次
     * @return 是否有重叠
     */
    public static boolean hasTimeConflict(int startSection1, int endSection1, 
                                        int startSection2, int endSection2) {
        return !(endSection1 < startSection2 || startSection1 > endSection2);
    }

    /**
     * 检查节次范围是否有效
     * @param startSection 开始节次
     * @param endSection 结束节次
     * @return 是否有效
     */
    public static boolean isValidSectionRange(int startSection, int endSection) {
        return startSection > 0 && endSection > 0 && startSection <= endSection;
    }

    /**
     * 检查星期是否有效
     * @param weekDay 星期几
     * @return 是否有效
     */
    public static boolean isValidWeekDay(int weekDay) {
        return weekDay >= 1 && weekDay <= 7;
    }

    /**
     * 获取星期几的中文名称
     * @param weekDay 星期几（1-7）
     * @return 星期几的中文名称
     */
    public static String getWeekDayName(int weekDay) {
        switch (weekDay) {
            case 1:
                return "周一";
            case 2:
                return "周二";
            case 3:
                return "周三";
            case 4:
                return "周四";
            case 5:
                return "周五";
            case 6:
                return "周六";
            case 7:
                return "周日";
            default:
                return "未知";
        }
    }

    /**
     * 格式化节次范围
     * @param startSection 开始节次
     * @param endSection 结束节次
     * @return 格式化后的节次范围字符串
     */
    public static String formatSectionRange(int startSection, int endSection) {
        if (startSection == endSection) {
            return "第" + startSection + "节";
        } else {
            return "第" + startSection + "-" + endSection + "节";
        }
    }
}