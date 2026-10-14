package io.github.stock.sdk;

import io.github.stock.sdk.model.KlineBar;
import io.github.stock.sdk.screener.Backtester;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class BacktesterTest {

    @Test
    public void testBacktestEngine() {
        List<KlineBar> bars = new ArrayList<>();
        // 模拟价格序列：第0天 10元，第1天 11元，第2天 12元，第3天 15元
        double[] prices = {10, 11, 12, 15, 14, 16};
        for (int i = 0; i < prices.length; i++) {
            KlineBar bar = new KlineBar();
            bar.setDate("2026-10-0" + (i + 1));
            bar.setOpen(BigDecimal.valueOf(prices[i]));
            bar.setClose(BigDecimal.valueOf(prices[i]));
            bar.setHigh(BigDecimal.valueOf(prices[i] + 1));
            bar.setLow(BigDecimal.valueOf(prices[i] - 1));
            bar.setVolume(10000L);
            bars.add(bar);
        }

        // 策略：第1天买入，第3天卖出
        Backtester.StrategyFunction strategy = (bar, idx, series) -> {
            if (idx == 1) {
                return Backtester.Action.BUY;
            }
            if (idx == 3) {
                return Backtester.Action.SELL;
            }
            return Backtester.Action.HOLD;
        };

        Backtester.BacktestOptions options = new Backtester.BacktestOptions(bars, strategy);
        options.setInitialCapital(100_000.0);
        options.setCommissionRate(0.0003);

        Backtester.BacktestReport report = Backtester.run(options);

        Assertions.assertNotNull(report);
        Assertions.assertEquals(1, report.getTotalTrades());
        Assertions.assertEquals(1, report.getWinningTrades());
        Assertions.assertEquals(1.0, report.getWinRate(), 0.001);
        Assertions.assertTrue(report.getFinalValue() > 100_000.0);
        Assertions.assertTrue(report.getTotalReturnRate() > 0.30); // 11元买，15元卖，收益超 30%
    }
}
