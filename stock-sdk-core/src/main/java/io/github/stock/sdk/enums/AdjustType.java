package io.github.stock.sdk.enums;

/**
 * 复权类型
 */
public enum AdjustType {
    /**
     * 前复权（默认）
     */
    QFQ("qfq", "1"),
    /**
     * 后复权
     */
    HFQ("hfq", "2"),
    /**
     * 不复权
     */
    NONE("none", "0");

    private final String value;
    private final String eastMoneyFqt;

    AdjustType(String value, String eastMoneyFqt) {
        this.value = value;
        this.eastMoneyFqt = eastMoneyFqt;
    }

    public String getValue() {
        return value;
    }

    public String getEastMoneyFqt() {
        return eastMoneyFqt;
    }

    public static AdjustType fromValue(String value) {
        if (value == null) {
            return QFQ;
        }
        for (AdjustType a : values()) {
            if (a.value.equalsIgnoreCase(value)) {
                return a;
            }
        }
        return QFQ;
    }
}
