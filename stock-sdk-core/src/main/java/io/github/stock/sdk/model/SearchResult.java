package io.github.stock.sdk.model;

import java.io.Serializable;

/**
 * 股票/基金/指数搜索联想结果
 */
public class SearchResult implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 标准化代码，如 sh600519 */
    private String symbol;
    /** 纯代码，如 600519 */
    private String code;
    /** 证券名称，如 贵州茅台 */
    private String name;
    /** 市场：cn / hk / us */
    private String market;
    /** 类型：stock (股票), etf, fund (基金), index (指数) */
    private String type;

    public SearchResult() {
    }

    public SearchResult(String symbol, String code, String name, String market, String type) {
        this.symbol = symbol;
        this.code = code;
        this.name = name;
        this.market = market;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "SearchResult{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", market='" + market + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
