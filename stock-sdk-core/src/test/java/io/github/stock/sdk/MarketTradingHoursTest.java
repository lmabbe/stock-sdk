package io.github.stock.sdk;

import io.github.stock.sdk.util.MarketTradingHours;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.*;

public class MarketTradingHoursTest {

    @Test
    public void testTradingHours() {
        ZoneId zone = MarketTradingHours.CHINA_ZONE;

        // 1. 周三上午 10:00 -> 交易中
        ZonedDateTime wedMorning = ZonedDateTime.of(LocalDate.of(2026, 10, 14), LocalTime.of(10, 0), zone);
        Assertions.assertTrue(MarketTradingHours.isTradingTime(wedMorning));

        // 2. 周三上午 09:20 (集合竞价) -> 交易中
        ZonedDateTime wedCallAuction = ZonedDateTime.of(LocalDate.of(2026, 10, 14), LocalTime.of(9, 20), zone);
        Assertions.assertTrue(MarketTradingHours.isTradingTime(wedCallAuction));

        // 3. 周三中午 12:00 -> 中午休市 (不交易)
        ZonedDateTime wedNoon = ZonedDateTime.of(LocalDate.of(2026, 10, 14), LocalTime.of(12, 0), zone);
        Assertions.assertFalse(MarketTradingHours.isTradingTime(wedNoon));

        // 4. 周三下午 14:30 -> 交易中
        ZonedDateTime wedAfternoon = ZonedDateTime.of(LocalDate.of(2026, 10, 14), LocalTime.of(14, 30), zone);
        Assertions.assertTrue(MarketTradingHours.isTradingTime(wedAfternoon));

        // 5. 周三收盘后 15:30 -> 休市 (不交易)
        ZonedDateTime wedEvening = ZonedDateTime.of(LocalDate.of(2026, 10, 14), LocalTime.of(15, 30), zone);
        Assertions.assertFalse(MarketTradingHours.isTradingTime(wedEvening));

        // 6. 周六上午 10:00 -> 周末休市 (不交易)
        ZonedDateTime satMorning = ZonedDateTime.of(LocalDate.of(2026, 10, 17), LocalTime.of(10, 0), zone);
        Assertions.assertFalse(MarketTradingHours.isTradingTime(satMorning));

        // 7. 周日上午 10:00 -> 周末休市 (不交易)
        ZonedDateTime sunMorning = ZonedDateTime.of(LocalDate.of(2026, 10, 18), LocalTime.of(10, 0), zone);
        Assertions.assertFalse(MarketTradingHours.isTradingTime(sunMorning));
    }
}
