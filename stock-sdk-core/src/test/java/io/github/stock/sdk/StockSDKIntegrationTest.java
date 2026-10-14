package io.github.stock.sdk;

import io.github.stock.sdk.model.ChipData;
import io.github.stock.sdk.model.EnrichedKline;
import io.github.stock.sdk.model.KlineBar;
import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.screener.Backtester;
import io.github.stock.sdk.screener.Screener;
import io.github.stock.sdk.signals.SignalEvent;
import io.github.stock.sdk.signals.Signals;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StockSDKIntegrationTest {

    @Test
    public void testSdkInitializationAndUsage() {
        StockSDK sdk = StockSDK.getDefault();
        Assertions.assertNotNull(sdk);
        Assertions.assertNotNull(sdk.quotes);
        Assertions.assertNotNull(sdk.kline);
        Assertions.assertNotNull(sdk.fund);
        Assertions.assertNotNull(sdk.plate);
        Assertions.assertNotNull(sdk.board);
        Assertions.assertNotNull(sdk.search);
        Assertions.assertNotNull(sdk.chips);
        Assertions.assertNotNull(sdk.batch);
        Assertions.assertNotNull(sdk.moneyflow);
        Assertions.assertNotNull(sdk.options);
        Assertions.assertNotNull(sdk.futures);
    }

    @Test
    public void testMockWorkflow() {
        // 构造虚拟行情数据测试 SDK 工作流
        List<Quote> sampleQuotes = new ArrayList<>();
        Quote q1 = new Quote();
        q1.setSymbol("sh600519");
        q1.setName("贵州茅台");
        q1.setPrice(BigDecimal.valueOf(1680.0));
        q1.setPe(BigDecimal.valueOf(25.0));
        q1.setChangePercent(BigDecimal.valueOf(3.2));
        q1.setAmount(BigDecimal.valueOf(2_000_000_000.0));
        sampleQuotes.add(q1);

        Quote q2 = new Quote();
        q2.setSymbol("sz000001");
        q2.setName("平安银行");
        q2.setPrice(BigDecimal.valueOf(11.2));
        q2.setPe(BigDecimal.valueOf(4.5));
        q2.setChangePercent(BigDecimal.valueOf(-0.5));
        q2.setAmount(BigDecimal.valueOf(500_000_000.0));
        sampleQuotes.add(q2);

        // 1. 测试选股链
        List<Quote> picks = StockSDK.screen(sampleQuotes)
                .where(q -> q.getChangePercent().doubleValue() > 0)
                .sortBy(Quote::getAmount, "desc")
                .top(5)
                .toList();

        Assertions.assertEquals(1, picks.size());
        Assertions.assertEquals("贵州茅台", picks.get(0).getName());

        // 2. 构造 30 根 K 线
        List<KlineBar> klines = new ArrayList<>();
        for (int i = 0; i < 30; i++) {
            KlineBar bar = new KlineBar();
            bar.setDate("2026-09-" + String.format("%02d", i + 1));
            double p = 100.0 + (i > 15 ? (i - 15) * 2.0 : -i * 0.5);
            bar.setOpen(BigDecimal.valueOf(p));
            bar.setClose(BigDecimal.valueOf(p + 1.0));
            bar.setHigh(BigDecimal.valueOf(p + 2.0));
            bar.setLow(BigDecimal.valueOf(p - 1.0));
            bar.setVolume(20000L);
            bar.setTurnoverRate(BigDecimal.valueOf(1.5));
            klines.add(bar);
        }
        System.out.println(klines);

        // 3. 测试信号扫描
        List<SignalEvent> signals = StockSDK.scanSignals(klines);
        Assertions.assertNotNull(signals);

        // 4. 测试回测
        Backtester.BacktestReport report = StockSDK.backtest(new Backtester.BacktestOptions(
                klines,
                (bar, idx, series) -> {
                    if (idx == 5) return Backtester.Action.BUY;
                    if (idx == 25) return Backtester.Action.SELL;
                    return Backtester.Action.HOLD;
                }
        ));
        Assertions.assertNotNull(report);
        Assertions.assertEquals(1, report.getTotalTrades());
    }
}
