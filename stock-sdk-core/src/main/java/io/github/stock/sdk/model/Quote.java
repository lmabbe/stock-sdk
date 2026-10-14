package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 股票完整实时行情
 */
public class Quote implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 统一股票代码，如 sh600519, sz000001, hk00700, usAAPL */
    private String symbol;
    /** 纯数字/英文代码，如 600519 */
    private String code;
    /** 股票名称，如 贵州茅台 */
    private String name;
    /** 所属市场：cn / hk / us */
    private String market;

    /** 最新价 */
    private BigDecimal price;
    /** 昨收价 */
    private BigDecimal preClose;
    /** 今开盘价 */
    private BigDecimal open;
    /** 最高价 */
    private BigDecimal high;
    /** 最低价 */
    private BigDecimal low;

    /** 涨跌额 (price - preClose) */
    private BigDecimal change;
    /** 涨跌幅百分比 (例如 2.5 表示 +2.5%) */
    private BigDecimal changePercent;
    /** 振幅百分比 */
    private BigDecimal amplitude;

    /** 成交量（手或股） */
    private Long volume;
    /** 成交额（元） */
    private BigDecimal amount;
    /** 换手率百分比 */
    private BigDecimal turnoverRate;

    /** 市盈率 (动/静) */
    private BigDecimal pe;
    /** 市净率 */
    private BigDecimal pb;
    /** 总市值（元） */
    private BigDecimal totalMarketCap;
    /** 流通市值（元） */
    private BigDecimal floatMarketCap;

    /** 买五档盘口 */
    private List<OrderBookLevel> bids = new ArrayList<>();
    /** 卖五档盘口 */
    private List<OrderBookLevel> asks = new ArrayList<>();

    /** 行情时间戳/日期字符串，如 2026-10-15 14:30:00 */
    private String timestamp;

    public Quote() {
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
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

    public String getMarket() {
        return market;
    }

    public void setMarket(String market) {
        this.market = market;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getPreClose() {
        return preClose;
    }

    public void setPreClose(BigDecimal preClose) {
        this.preClose = preClose;
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

    public BigDecimal getAmplitude() {
        return amplitude;
    }

    public void setAmplitude(BigDecimal amplitude) {
        this.amplitude = amplitude;
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

    public BigDecimal getTurnoverRate() {
        return turnoverRate;
    }

    public void setTurnoverRate(BigDecimal turnoverRate) {
        this.turnoverRate = turnoverRate;
    }

    public BigDecimal getPe() {
        return pe;
    }

    public void setPe(BigDecimal pe) {
        this.pe = pe;
    }

    public BigDecimal getPb() {
        return pb;
    }

    public void setPb(BigDecimal pb) {
        this.pb = pb;
    }

    public BigDecimal getTotalMarketCap() {
        return totalMarketCap;
    }

    public void setTotalMarketCap(BigDecimal totalMarketCap) {
        this.totalMarketCap = totalMarketCap;
    }

    public BigDecimal getFloatMarketCap() {
        return floatMarketCap;
    }

    public void setFloatMarketCap(BigDecimal floatMarketCap) {
        this.floatMarketCap = floatMarketCap;
    }

    public List<OrderBookLevel> getBids() {
        return bids;
    }

    public void setBids(List<OrderBookLevel> bids) {
        this.bids = bids;
    }

    public List<OrderBookLevel> getAsks() {
        return asks;
    }

    public void setAsks(List<OrderBookLevel> asks) {
        this.asks = asks;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Quote{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", changePercent=" + changePercent + "%" +
                ", amount=" + amount +
                ", pe=" + pe +
                '}';
    }
}
