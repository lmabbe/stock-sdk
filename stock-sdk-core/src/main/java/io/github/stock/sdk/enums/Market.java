package io.github.stock.sdk.enums;

/**
 * 证券交易市场
 */
public enum Market {
    /**
     * A 股市场（沪深京）
     */
    CN("cn"),
    /**
     * 港股市场
     */
    HK("hk"),
    /**
     * 美股市场
     */
    US("us");

    private final String code;

    Market(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
