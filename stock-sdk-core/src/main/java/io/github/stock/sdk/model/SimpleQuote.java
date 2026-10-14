package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 极简行情对象（用于快速列表、低开销展示）
 */
public class SimpleQuote implements Serializable {
    private static final long serialVersionUID = 1L;

    private String symbol;
    private String name;
    private BigDecimal price;
    private BigDecimal changePercent;
    private BigDecimal change;
    private Long volume;
    private BigDecimal amount;

    public SimpleQuote() {
    }

    public SimpleQuote(String symbol, String name, BigDecimal price, BigDecimal changePercent, BigDecimal change, Long volume, BigDecimal amount) {
        this.symbol = symbol;
        this.name = name;
        this.price = price;
        this.changePercent = changePercent;
        this.change = change;
        this.volume = volume;
        this.amount = amount;
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

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
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
        return "SimpleQuote{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", changePercent=" + changePercent + "%" +
                '}';
    }
}
