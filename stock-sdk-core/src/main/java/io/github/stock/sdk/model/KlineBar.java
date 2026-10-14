package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 单根 K 线柱（CandleStick 数据）
 */
public class KlineBar implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 日期或时间，如 "2026-10-15" 或 "2026-10-15 14:30" */
    private String date;

    /** 开盘价 */
    private BigDecimal open;
    /** 最高价 */
    private BigDecimal high;
    /** 最低价 */
    private BigDecimal low;
    /** 收盘价 */
    private BigDecimal close;

    /** 成交量（手或股） */
    private Long volume;
    /** 成交额（元） */
    private BigDecimal turnover;

    /** 振幅百分比 */
    private BigDecimal amplitude;
    /** 涨跌幅百分比 */
    private BigDecimal changePercent;
    /** 涨跌额 */
    private BigDecimal change;
    /** 换手率百分比 */
    private BigDecimal turnoverRate;

    public KlineBar() {
    }

    public KlineBar(String date, BigDecimal open, BigDecimal high, BigDecimal low, BigDecimal close, Long volume, BigDecimal turnover) {
        this.date = date;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
        this.volume = volume;
        this.turnover = turnover;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getClose() {
        return close;
    }

    public void setClose(BigDecimal close) {
        this.close = close;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    public BigDecimal getTurnover() {
        return turnover;
    }

    public void setTurnover(BigDecimal turnover) {
        this.turnover = turnover;
    }

    public BigDecimal getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(BigDecimal amplitude) {
        this.amplitude = amplitude;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
    }

    public BigDecimal getTurnoverRate() {
        return turnoverRate;
    }

    public void setTurnoverRate(BigDecimal turnoverRate) {
        this.turnoverRate = turnoverRate;
    }

    // 辅助 Double 数值获取，方便指标算法快速计算
    public double openVal() {
        return open != null ? open.doubleValue() : 0.0;
    }

    public double highVal() {
        return high != null ? high.doubleValue() : 0.0;
    }

    public double lowVal() {
        return low != null ? low.doubleValue() : 0.0;
    }

    public double closeVal() {
        return close != null ? close.doubleValue() : 0.0;
    }

    public double volumeVal() {
        return volume != null ? volume.doubleValue() : 0.0;
    }

    @Override
    public String toString() {
        return "{" + date + ": O=" + open + ", H=" + high + ", L=" + low + ", C=" + close + ", V=" + volume + "}";
    }
}
