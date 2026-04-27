package com.hxq.smart_campus.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class WeekRangeUtils {

    private static final String WEEK_RANGE_PATTERN = "^\\d+(-\\d+)?$";

    /**
     * 验证周次范围格式
     * @param weekRange 周次范围字符串，如 "1-10,12-18"
     * @return 是否有效
     */
    public static boolean isValidWeekRange(String weekRange) {
        if (weekRange == null || weekRange.trim().isEmpty()) {
            return false;
        }

        String[] ranges = weekRange.split(",");
        for (String range : ranges) {
            range = range.trim();
            if (!range.matches(WEEK_RANGE_PATTERN)) {
                return false;
            }

            if (range.contains("-")) {
                String[] parts = range.split("-");
                try {
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    if (start > end) {
                        return false;
                    }
                } catch (NumberFormatException e) {
                    log.warn("周次范围解析失败: {}", range);
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * 解析周次范围，返回周次列表
     * @param weekRange 周次范围字符串，如 "1-10,12-18"
     * @return 周次列表
     */
    public static List<Integer> parseWeekRange(String weekRange) {
        List<Integer> weeks = new ArrayList<>();
        
        if (weekRange == null || weekRange.trim().isEmpty()) {
            return weeks;
        }

        try {
            String[] ranges = weekRange.split(",");
            for (String range : ranges) {
                range = range.trim();
                if (range.contains("-")) {
                    String[] parts = range.split("-");
                    int start = Integer.parseInt(parts[0].trim());
                    int end = Integer.parseInt(parts[1].trim());
                    for (int i = start; i <= end; i++) {
                        weeks.add(i);
                    }
                } else {
                    weeks.add(Integer.parseInt(range));
                }
            }
        } catch (NumberFormatException e) {
            log.error("解析周次范围失败: {}", weekRange, e);
            throw new IllegalArgumentException("周次范围格式不正确");
        }

        return weeks;
    }

    /**
     * 检查两个周次范围是否有重叠
     * @param range1 周次范围1
     * @param range2 周次范围2
     * @return 是否有重叠
     */
    public static boolean hasWeekRangeOverlap(String range1, String range2) {
        List<Integer> weeks1 = parseWeekRange(range1);
        List<Integer> weeks2 = parseWeekRange(range2);

        return weeks1.stream().anyMatch(weeks2::contains);
    }

    /**
     * 格式化周次范围
     * @param weeks 周次列表
     * @return 格式化后的周次范围字符串
     */
    public static String formatWeekRange(List<Integer> weeks) {
        if (weeks == null || weeks.isEmpty()) {
            return "";
        }

        weeks.sort(Integer::compareTo);

        StringBuilder result = new StringBuilder();
        int start = weeks.get(0);
        int end = start;

        for (int i = 1; i < weeks.size(); i++) {
            int current = weeks.get(i);
            if (current == end + 1) {
                end = current;
            } else {
                if (result.length() > 0) {
                    result.append(",");
                }
                if (start == end) {
                    result.append(start);
                } else {
                    result.append(start).append("-").append(end);
                }
                start = current;
                end = current;
            }
        }

        if (result.length() > 0) {
            result.append(",");
        }
        if (start == end) {
            result.append(start);
        } else {
            result.append(start).append("-").append(end);
        }

        return result.toString();
    }
}