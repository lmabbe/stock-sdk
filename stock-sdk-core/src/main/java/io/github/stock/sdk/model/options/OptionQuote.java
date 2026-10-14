package io.github.stock.sdk.model.options;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 期权单合约实时行情
 */
public class OptionQuote implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 合约代码，例如 10005001 */
    private String code;
    /** 合约名称，例如 50ETF购3月2800 */
    private String name;
    /** 期权类型：CALL (认购/看涨) 或 PUT (认沽/看跌) */
    private String type;
    /** 行权价格 */
    private BigDecimal strikePrice;
    /** 最新价 */
    private BigDecimal price;
    /** 昨结价 */
    private BigDecimal preSettlePrice;
    /** 涨跌额 */
    private BigDecimal change;
    /** 涨跌幅百分比 */
    private BigDecimal changePercent;
    /** 成交量（张） */
    private Long volume;
    /** 未平仓合约数（持仓量） */
    private Long openInterest;
    /** 隐含波动率 (IV, 例如 0.22 表示 22%) */
    private BigDecimal impliedVolatility;
    /** 到期日期，例如 2026-03-25 */
    private String expireDate;

    public OptionQuote() {
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getStrikePrice() {
        return strikePrice;
    }

    public void setStrikePrice(BigDecimal strikePrice) {
        this.strikePrice = strikePrice;
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

    public Long getOpenInterest() {
        return openInterest;
    }

    public void setOpenInterest(Long openInterest) {
        this.openInterest = openInterest;
    }

    public BigDecimal getImpliedVolatility() {
        return impliedVolatility;
    }

    public void setImpliedVolatility(BigDecimal impliedVolatility) {
        this.impliedVolatility = impliedVolatility;
    }

    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    @Override
    public String toString() {
        return "Option{" + name + ", Strike=" + strikePrice + ", Price=" + price + "}";
    }
}
