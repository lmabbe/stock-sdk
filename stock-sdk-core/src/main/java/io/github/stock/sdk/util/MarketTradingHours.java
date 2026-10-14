package io.github.stock.sdk.util;

import java.time.*;

/**
 * 中国 A 股交易日与交易时段判断工具类
 *
 * 正常交易时间（北京时间 UTC+8）：
 * 1. 周一至周五（非周末）
 * 2. 上午：09:15 ~ 11:30（含 09:15-09:25 开盘集合竞价）
 * 3. 下午：13:00 ~ 15:00
 */
public class MarketTradingHours {

    public static final ZoneId CHINA_ZONE = ZoneId.of("Asia/Shanghai");

    private static final LocalTime MORNING_START = LocalTime.of(9, 15);
    private static final LocalTime MORNING_END = LocalTime.of(11, 30);
    private static final LocalTime AFTERNOON_START = LocalTime.of(13, 0);
    private static final LocalTime AFTERNOON_END = LocalTime.of(15, 0);

    /**
     * 判断当前系统时刻是否处于 A 股交易时间内
     */
    public static boolean isTradingTime() {
        return isTradingTime(ZonedDateTime.now(CHINA_ZONE));
    }

    /**
     * 判断指定时间是否处于 A 股交易时间内
     */
    public static boolean isTradingTime(ZonedDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }

        // 1. 周六与周日休市
        DayOfWeek dayOfWeek = dateTime.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }

        // 2. 检查当日交易时间窗口
        LocalTime time = dateTime.toLocalTime();

        // 上午交易时段 09:15 ~ 11:30
        boolean isMorning = !time.isBefore(MORNING_START) && !time.isAfter(MORNING_END);
        // 下午交易时段 13:00 ~ 15:00
        boolean isAfternoon = !time.isBefore(AFTERNOON_START) && !time.isAfter(AFTERNOON_END);

        return isMorning || isAfternoon;
    }
}
