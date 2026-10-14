package io.github.stock.sdk.spring.event;

import io.github.stock.sdk.model.Quote;
import org.springframework.context.ApplicationEvent;

import java.math.BigDecimal;

/**
 * 股票价格变动通知事件 (Spring ApplicationEvent)
 *
 * 当动态订阅列表中的股票在监控轮询中检测到现价发生变动时触发。
 * 业务服务可直接通过标准的 Spring {@code @EventListener} 注解进行监听处理。
 *
 * <pre>
 * &#64;Component
 * public class PriceAlertListener {
 *
 *     &#64;EventListener
 *     public void handlePriceChange(StockPriceChangeEvent event) {
 *         System.out.printf("[股价预警] %s(%s) 价格从 %s 变为 %s, 涨幅: %s%%\n",
 *                 event.getName(), event.getSymbol(), event.getOldPrice(), event.getNewPrice(), event.getTodayChangePercent());
 *     }
 * }
 * </pre>
 */
public class StockPriceChangeEvent extends ApplicationEvent {
    private static final long serialVersionUID = 1L;

    /** 统一股票代码，如 sh600519 */
    private final String symbol;
    /** 股票名称 */
    private final String name;
    /** 上次记录的价格 */
    private final BigDecimal oldPrice;
    /** 本次变动后的最新现价 */
    private final BigDecimal newPrice;
    /** 本次跳动价格差额 (newPrice - oldPrice) */
    private final BigDecimal tickDiff;
    /** 今日累计涨跌额 (newPrice - preClose) */
    private final BigDecimal todayChange;
    /** 今日累计涨跌幅百分比 */
    private final BigDecimal todayChangePercent;
    /** 最新完整行情对象（包含五档盘口、成交量、成交额等全部细节） */
    private final Quote quote;

    public StockPriceChangeEvent(Object source, String symbol, String name,
                                 BigDecimal oldPrice, BigDecimal newPrice,
                                 BigDecimal tickDiff, BigDecimal todayChange,
                                 BigDecimal todayChangePercent, Quote quote) {
        super(source);
        this.symbol = symbol;
        this.name = name;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
        this.tickDiff = tickDiff;
        this.todayChange = todayChange;
        this.todayChangePercent = todayChangePercent;
        this.quote = quote;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getOldPrice() {
        return oldPrice;
    }

    public BigDecimal getNewPrice() {
        return newPrice;
    }

    public BigDecimal getTickDiff() {
        return tickDiff;
    }

    public BigDecimal getTodayChange() {
        return todayChange;
    }

    public BigDecimal getTodayChangePercent() {
        return todayChangePercent;
    }

    public Quote getQuote() {
        return quote;
    }

    @Override
    public String toString() {
        return "StockPriceChangeEvent{" +
                "symbol='" + symbol + '\'' +
                ", name='" + name + '\'' +
                ", " + oldPrice + " -> " + newPrice +
                " (" + (todayChangePercent != null ? todayChangePercent + "%" : "") + ")" +
                '}';
    }
}
