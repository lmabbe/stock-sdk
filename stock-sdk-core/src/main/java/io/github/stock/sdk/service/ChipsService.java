package io.github.stock.sdk.service;

import io.github.stock.sdk.enums.AdjustType;
import io.github.stock.sdk.enums.Period;
import io.github.stock.sdk.model.ChipData;
import io.github.stock.sdk.model.KlineBar;
import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.provider.DataProvider;
import io.github.stock.sdk.util.ChipsEngine;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

/**
 * 筹码分布（CYQ）命名空间服务 (sdk.chips.*)
 */
public class ChipsService {

    private final DataProvider klineProvider;
    private final DataProvider quoteProvider;

    public ChipsService(DataProvider klineProvider, DataProvider quoteProvider) {
        this.klineProvider = klineProvider;
        this.quoteProvider = quoteProvider;
    }

    /**
     * 计算指定股票的当前筹码分布及各项指标（平均成本、获利盘、70%/90%集中度与区间、100分箱直方图）
     * @param symbol 股票代码，如 "600519"
     */
    public ChipData cn(String symbol) {
        return cn(symbol, 120);
    }

    /**
     * 计算指定股票在指定日 K 线统计窗口下的筹码分布
     * @param symbol 股票代码
     * @param historyDays 历史统计窗口天数（建议 60 ~ 250 天）
     */
    public ChipData cn(String symbol, int historyDays) {
        int limit = Math.max(30, historyDays);
        List<KlineBar> bars = klineProvider.getKlines(symbol, Period.DAILY, AdjustType.QFQ, limit);
        if (bars == null || bars.isEmpty()) {
            return new ChipData();
        }

        BigDecimal currentPrice = null;
        if (quoteProvider != null) {
            try {
                List<Quote> quotes = quoteProvider.getQuotes(Collections.singletonList(symbol));
                if (quotes != null && !quotes.isEmpty() && quotes.get(0).getPrice() != null) {
                    currentPrice = quotes.get(0).getPrice();
                }
            } catch (Exception ignored) {
            }
        }

        return ChipsEngine.calcCYQ(bars, currentPrice);
    }
}
