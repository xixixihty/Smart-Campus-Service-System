package com.hxq.smart_campus.constant;

/**
 * redis常量类
 */
public class RedisConstant {
    /**
     * 选课时间段缓存键前缀
     */
    public static final String COURSE_SELECTION_KEY_PERIOD = "courseSelectionPeriod:";
    /**
     * 课程详情缓存键前缀
     */
    public static final String COURSE_DETAIL_KEY_PREFIX = "courseDetail:";
    /**
     * 我的选课缓存键前缀
     */
    public static final String MY_COURSE_SELECTION_KEY_PREFIX = "myCourseSelection:";
    /**
     * 可选课程缓存键前缀1
     */
    public static final String AVAILABLE_COURSE_KEY_PREFIX = "availableCourse:";

    /**
     * 通知缓存键前缀
     */
    public static final String NOTICE_KEY_PREFIX = "notice:";
    /**
     * 我的通知缓存键前缀
     */
    public static final String MY_NOTICE_KEY_PREFIX = "myNotice:";



    /**
     * 分布式锁课程缓存键前缀
     */
    public static final String LOCK_COURSE_KEY_PREFIX = "lock:course:";


}
