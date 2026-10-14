package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 股票主力资金流向数据
 */
public class MoneyFlow implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String name;

    /** 主力资金净流入（元） (超大单 + 大单) */
    private BigDecimal mainNetInflow;
    /** 主力净流入占比百分比 */
    private BigDecimal mainNetInflowPercent;

    /** 超大单净流入（元） */
    private BigDecimal superLargeNetInflow;
    /** 大单净流入（元） */
    private BigDecimal largeNetInflow;
    /** 中单净流入（元） */
    private BigDecimal mediumNetInflow;
    /** 小单（散户）净流入（元） */
    private BigDecimal smallNetInflow;

    /** 更新时间 */
    private String updateTime;

    public MoneyFlow() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getMainNetInflow() {
        return mainNetInflow;
    }

    public void setMainNetInflow(BigDecimal mainNetInflow) {
        this.mainNetInflow = mainNetInflow;
    }

    public BigDecimal getMainNetInflowPercent() {
        return mainNetInflowPercent;
    }

    public void setMainNetInflowPercent(BigDecimal mainNetInflowPercent) {
        this.mainNetInflowPercent = mainNetInflowPercent;
    }

    public BigDecimal getSuperLargeNetInflow() {
        return superLargeNetInflow;
    }

    public void setSuperLargeNetInflow(BigDecimal superLargeNetInflow) {
        this.superLargeNetInflow = superLargeNetInflow;
    }

    public BigDecimal getLargeNetInflow() {
        return largeNetInflow;
    }

    public void setLargeNetInflow(BigDecimal largeNetInflow) {
        this.largeNetInflow = largeNetInflow;
    }

    public BigDecimal getMediumNetInflow() {
        return mediumNetInflow;
    }

    public void setMediumNetInflow(BigDecimal mediumNetInflow) {
        this.mediumNetInflow = mediumNetInflow;
    }

    public BigDecimal getSmallNetInflow() {
        return smallNetInflow;
    }

    public void setSmallNetInflow(BigDecimal smallNetInflow) {
        this.smallNetInflow = smallNetInflow;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public String toString() {
        return "MoneyFlow{" +
                "symbol='" + symbol + '\'' +
                ", mainNetInflow=" + mainNetInflow +
                ", mainPercent=" + mainNetInflowPercent + "%" +
                '}';
    }
}
