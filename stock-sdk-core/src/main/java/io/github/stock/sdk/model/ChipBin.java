package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 筹码分布单个价格分箱节点
 */
public class ChipBin implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 分箱对应的价格中心点 */
    private BigDecimal price;
    /** 该价格区间累积的筹码量（股数或相对权重） */
    private Double volume;
    /** 该价格区间筹码占全部筹码的比例（0.0 ~ 1.0） */
    private Double percent;

    public ChipBin() {
    }

    public ChipBin(BigDecimal price, Double volume, Double percent) {
        this.price = price;
        this.volume = volume;
        this.percent = percent;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Double getVolume() {
        return volume;
    }

    public void setVolume(Double volume) {
        this.volume = volume;
    }

    public Double getPercent() {
        return percent;
    }

    public void setPercent(Double percent) {
        this.percent = percent;
    }

    @Override
    public String toString() {
        return "{" + price + ": " + volume + " (" + (percent != null ? String.format("%.2f%%", percent * 100) : "0") + ")}";
    }
}
