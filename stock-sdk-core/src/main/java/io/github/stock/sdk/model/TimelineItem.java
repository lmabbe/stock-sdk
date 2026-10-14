package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 分时图单分钟走势数据点
 */
public class TimelineItem implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 分钟时间，例如 "09:31" 或 "2026-10-15 09:31" */
    private String time;
    /** 分钟当前价 */
    private BigDecimal price;
    /** 分钟均价（黄线均线） */
    private BigDecimal avgPrice;
    /** 涨跌幅百分比 */
    private BigDecimal changePercent;
    /** 当分钟成交量（手） */
    private Long volume;
    /** 当分钟成交额（元） */
    private BigDecimal amount;

    public TimelineItem() {
    }

    public TimelineItem(String time, BigDecimal price, BigDecimal avgPrice, BigDecimal changePercent, Long volume, BigDecimal amount) {
        this.time = time;
        this.price = price;
        this.avgPrice = avgPrice;
        this.changePercent = changePercent;
        this.volume = volume;
        this.amount = amount;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    @Override
    public String toString() {
        return "{" + time + ": P=" + price + ", Avg=" + avgPrice + ", V=" + volume + "}";
    }
}
