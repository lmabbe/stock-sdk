package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 当日分时图完整数据（Timeline）
 */
public class TimelineData implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 统一股票代码 */
    private String symbol;
    /** 股票名称 */
    private String name;
    /** 昨日收盘价（分时图基准中轴线） */
    private BigDecimal preClose;
    /** 分时分钟数据点列表（通常为 241 个分钟点） */
    private List<TimelineItem> items = new ArrayList<>();

    public TimelineData() {
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

    public BigDecimal getPreClose() {
        return preClose;
    }

    public void setPreClose(BigDecimal preClose) {
        this.preClose = preClose;
    }

    public List<TimelineItem> getItems() {
        return items;
    }

    public void setItems(List<TimelineItem> items) {
        this.items = items;
    }
}
