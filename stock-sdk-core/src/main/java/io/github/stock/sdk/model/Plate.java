package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 行业板块 / 概念板块模型
 */
public class Plate implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 板块代码，例如 BK0420, BK1036 */
    private String code;
    /** 板块名称，例如 贵金属, 半导体, 人工智能 */
    private String name;
    /** 最新指数 */
    private BigDecimal price;
    /** 涨跌幅百分比 */
    private BigDecimal changePercent;
    /** 领涨股票名称 */
    private String leadingStockName;
    /** 领涨股票涨跌幅 */
    private BigDecimal leadingStockChangePercent;

    public Plate() {
    }

    public Plate(String code, String name) {
        this.code = code;
        this.name = name;
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

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(BigDecimal changePercent) {
        this.changePercent = changePercent;
    }

    public String getLeadingStockName() {
        return leadingStockName;
    }

    public void setLeadingStockName(String leadingStockName) {
        this.leadingStockName = leadingStockName;
    }

    public BigDecimal getLeadingStockChangePercent() {
        return leadingStockChangePercent;
    }

    public void setLeadingStockChangePercent(BigDecimal leadingStockChangePercent) {
        this.leadingStockChangePercent = leadingStockChangePercent;
    }

    @Override
    public String toString() {
        return "Plate{" +
                "code='" + code + '\'' +
                ", name='" + name + '\'' +
                ", changePercent=" + changePercent + "%" +
                ", leader='" + leadingStockName + '\'' +
                '}';
    }
}
