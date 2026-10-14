package io.github.stock.sdk.enums;

/**
 * K线周期类型
 */
public enum Period {
    DAILY("daily", "101"),
    WEEKLY("weekly", "102"),
    MONTHLY("monthly", "103"),
    MIN_1("1", "1"),
    MIN_5("5", "5"),
    MIN_15("15", "15"),
    MIN_30("30", "30"),
    MIN_60("60", "60");

    private final String value;
    private final String eastMoneyKlt;

    Period(String value, String eastMoneyKlt) {
        this.value = value;
        this.eastMoneyKlt = eastMoneyKlt;
    }

    public String getValue() {
        return value;
    }

    public String getEastMoneyKlt() {
        return eastMoneyKlt;
    }

    public static Period fromValue(String value) {
        if (value == null) {
            return DAILY;
        }
        for (Period p : values()) {
            if (p.value.equalsIgnoreCase(value)) {
                return p;
            }
        }
        return DAILY;
    }
}
