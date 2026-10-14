package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 基金官方公布净值记录
 */
public class FundNav implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 净值日期，如 2026-10-14 */
    private String date;
    /** 单位净值 */
    private BigDecimal unitNav;
    /** 累计净值 */
    private BigDecimal cumulativeNav;
    /** 日增长率百分比 */
    private BigDecimal dailyGrowthRate;

    public FundNav() {
    }

    public FundNav(String date, BigDecimal unitNav, BigDecimal cumulativeNav, BigDecimal dailyGrowthRate) {
        this.date = date;
        this.unitNav = unitNav;
        this.cumulativeNav = cumulativeNav;
        this.dailyGrowthRate = dailyGrowthRate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public BigDecimal getUnitNav() {
        return unitNav;
    }

    public void setUnitNav(BigDecimal unitNav) {
        this.unitNav = unitNav;
    }

    public BigDecimal getCumulativeNav() {
        return cumulativeNav;
    }

    public void setCumulativeNav(BigDecimal cumulativeNav) {
        this.cumulativeNav = cumulativeNav;
    }

    public BigDecimal getDailyGrowthRate() {
        return dailyGrowthRate;
    }

    public void setDailyGrowthRate(BigDecimal dailyGrowthRate) {
        this.dailyGrowthRate = dailyGrowthRate;
    }

    @Override
    public String toString() {
        return "{" + date + ": NAV=" + unitNav + ", Acc=" + cumulativeNav + ", Growth=" + dailyGrowthRate + "%}";
    }
}
