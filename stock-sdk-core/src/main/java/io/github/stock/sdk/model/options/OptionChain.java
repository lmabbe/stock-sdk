package io.github.stock.sdk.model.options;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * ETF 期权 T 型报价链
 */
public class OptionChain implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 标的资产代码，例如 510050 (华夏上证50ETF) */
    private String underlyingSymbol;
    /** 标的资产最新价 */
    private BigDecimal underlyingPrice;
    /** 到期月份/日期，例如 "2026-03" */
    private String expireMonth;
    /** T 型报价链条目（按行权价升序） */
    private List<OptionChainRow> rows = new ArrayList<>();

    public static class OptionChainRow implements Serializable {
        private static final long serialVersionUID = 1L;

        private BigDecimal strikePrice;
        private OptionQuote call;
        private OptionQuote put;

        public OptionChainRow() {
        }

        public OptionChainRow(BigDecimal strikePrice, OptionQuote call, OptionQuote put) {
            this.strikePrice = strikePrice;
            this.call = call;
            this.put = put;
        }

        public BigDecimal getStrikePrice() {
            return strikePrice;
        }

        public void setStrikePrice(BigDecimal strikePrice) {
            this.strikePrice = strikePrice;
        }

        public OptionQuote getCall() {
            return call;
        }

        public void setCall(OptionQuote call) {
            this.call = call;
        }

        public OptionQuote getPut() {
            return put;
        }

        public void setPut(OptionQuote put) {
            this.put = put;
        }
    }

    public OptionChain() {
    }

    public String getUnderlyingSymbol() {
        return underlyingSymbol;
    }

    public void setUnderlyingSymbol(String underlyingSymbol) {
        this.underlyingSymbol = underlyingSymbol;
    }

    public BigDecimal getUnderlyingPrice() {
        return underlyingPrice;
    }

    public void setUnderlyingPrice(BigDecimal underlyingPrice) {
        this.underlyingPrice = underlyingPrice;
    }

    public String getExpireMonth() {
        return expireMonth;
    }

    public void setExpireMonth(String expireMonth) {
        this.expireMonth = expireMonth;
    }

    public List<OptionChainRow> getRows() {
        return rows;
    }

    public void setRows(List<OptionChainRow> rows) {
        this.rows = rows;
    }
}
