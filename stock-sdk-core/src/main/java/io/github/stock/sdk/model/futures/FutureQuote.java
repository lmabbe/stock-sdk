package io.github.stock.sdk.model.futures;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 期货主力/单合约实时行情
 */
public class FutureQuote implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 期货代码，如 RB2405 (螺纹钢), IF2403 (沪深300股指), AU2406 (黄金) */
    private String symbol;
    /** 合约名称 */
    private String name;
    /** 交易所名称（中金所/上期所/大商所/郑商所） */
    private String exchange;

    /** 最新价 */
    private BigDecimal price;
    /** 昨结算价 */
    private BigDecimal preSettlePrice;
    /** 今开盘价 */
    private BigDecimal open;
    /** 最高价 */
    private BigDecimal high;
    /** 最低价 */
    private BigDecimal low;
    /** 涨跌额 */
    private BigDecimal change;
    /** 涨跌幅百分比 */
    private BigDecimal changePercent;

    /** 成交量（手） */
    private Long volume;
    /** 成交额（元） */
    private BigDecimal amount;
    /** 持仓量（手） */
    private Long openInterest;
    /** 持仓量日增减（手） */
    private Long openInterestChange;

    /** 行情时间戳 */
    private String timestamp;

    public FutureQuote() {
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

    public String getExchange() {
        return exchange;
    }

    public void setExchange(String exchange) {
        this.exchange = exchange;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPreSettlePrice() {
        return preSettlePrice;
    }

    public void setPreSettlePrice(BigDecimal preSettlePrice) {
        this.preSettlePrice = preSettlePrice;
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

    public BigDecimal getChange() {
        return change;
    }

    public void setChange(BigDecimal change) {
        this.change = change;
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

    public Long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(Long openInterest) {
        this.openInterest = openInterest;
    }

    public Long getOpenInterestChange() {
        return openInterestChange;
    }

    public void setOpenInterestChange(Long openInterestChange) {
        this.openInterestChange = openInterestChange;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "FutureQuote{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", change=" + changePercent + "%" +
                ", oi=" + openInterest +
                '}';
    }
}
