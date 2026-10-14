package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 基金实时估算净值
 */
public class FundQuote implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 基金代码，如 000001 */
    private String fundCode;
    /** 基金名称 */
    private String name;
    /** 单位净值（上一交易日） */
    private BigDecimal netWorth;
    /** 估算净值 */
    private BigDecimal estimateNetWorth;
    /** 估算涨跌幅百分比（例如 1.25 表示 +1.25%） */
    private BigDecimal estimateChangePercent;
    /** 估算时间，如 2026-10-15 14:45 */
    private String estimateTime;

    public FundQuote() {
    }

    public String getFundCode() {
        return fundCode;
    }

    public void setFundCode(String fundCode) {
        this.fundCode = fundCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getNetWorth() {
        return netWorth;
    }

    public void setNetWorth(BigDecimal netWorth) {
        this.netWorth = netWorth;
    }

    public BigDecimal getEstimateNetWorth() {
        return estimateNetWorth;
    }

    public void setEstimateNetWorth(BigDecimal estimateNetWorth) {
        this.estimateNetWorth = estimateNetWorth;
    }

    public BigDecimal getEstimateChangePercent() {
        return estimateChangePercent;
    }

    public void setEstimateChangePercent(BigDecimal estimateChangePercent) {
        this.estimateChangePercent = estimateChangePercent;
    }

    public String getEstimateTime() {
        return estimateTime;
    }

    public void setEstimateTime(String estimateTime) {
        this.estimateTime = estimateTime;
    }

    @Override
    public String toString() {
        return "FundQuote{" +
                "code='" + fundCode + '\'' +
                ", name='" + name + '\'' +
                ", est=" + estimateNetWorth +
                ", estChange=" + estimateChangePercent + "%" +
                '}';
    }
}
