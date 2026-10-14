package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 筹码分布（CYQ）核心指标与分布直方图
 */
public class ChipData implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 平均成本价 */
    private BigDecimal averageCost;
    /** 获利盘比例（0.0 ~ 1.0） */
    private BigDecimal profitRatio;

    /** 70% 筹码集中度（数值越小越集中） */
    private BigDecimal concentration70;
    /** 70% 筹码成本区间下限 */
    private BigDecimal costRange70Low;
    /** 70% 筹码成本区间上限 */
    private BigDecimal costRange70High;

    /** 90% 筹码集中度 */
    private BigDecimal concentration90;
    /** 90% 筹码成本区间下限 */
    private BigDecimal costRange90Low;
    /** 90% 筹码成本区间上限 */
    private BigDecimal costRange90High;

    /** 筹码柱状分布分箱（通常为 100 个等距价格区间） */
    private List<ChipBin> bins = new ArrayList<>();

    public ChipData() {
    }

    public BigDecimal getAverageCost() {
        return averageCost;
    }

    public void setAverageCost(BigDecimal averageCost) {
        this.averageCost = averageCost;
    }

    public BigDecimal getProfitRatio() {
        return profitRatio;
    }

    public void setProfitRatio(BigDecimal profitRatio) {
        this.profitRatio = profitRatio;
    }

    public BigDecimal getConcentration70() {
        return concentration70;
    }

    public void setConcentration70(BigDecimal concentration70) {
        this.concentration70 = concentration70;
    }

    public BigDecimal getCostRange70Low() {
        return costRange70Low;
    }

    public void setCostRange70Low(BigDecimal costRange70Low) {
        this.costRange70Low = costRange70Low;
    }

    public BigDecimal getCostRange70High() {
        return costRange70High;
    }

    public void setCostRange70High(BigDecimal costRange70High) {
        this.costRange70High = costRange70High;
    }

    public BigDecimal getConcentration90() {
        return concentration90;
    }

    public void setConcentration90(BigDecimal concentration90) {
        this.concentration90 = concentration90;
    }

    public BigDecimal getCostRange90Low() {
        return costRange90Low;
    }

    public void setCostRange90Low(BigDecimal costRange90Low) {
        this.costRange90Low = costRange90Low;
    }

    public BigDecimal getCostRange90High() {
        return costRange90High;
    }

    public void setCostRange90High(BigDecimal costRange90High) {
        this.costRange90High = costRange90High;
    }

    public List<ChipBin> getBins() {
        return bins;
    }

    public void setBins(List<ChipBin> bins) {
        this.bins = bins;
    }

    @Override
    public String toString() {
        return "ChipData{" +
                "avgCost=" + averageCost +
                ", profitRatio=" + (profitRatio != null ? profitRatio.multiply(BigDecimal.valueOf(100)) : 0) + "%" +
                ", concentration70=" + concentration70 +
                ", concentration90=" + concentration90 +
                ", binsCount=" + (bins != null ? bins.size() : 0) +
                '}';
    }
}
