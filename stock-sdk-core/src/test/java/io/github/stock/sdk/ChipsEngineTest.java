package io.github.stock.sdk;

import io.github.stock.sdk.model.ChipData;
import io.github.stock.sdk.model.KlineBar;
import io.github.stock.sdk.util.ChipsEngine;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ChipsEngineTest {

    @Test
    public void testChipsDistribution() {
        List<KlineBar> bars = new ArrayList<>();
        // 构造震荡上涨行情
        for (int i = 0; i < 60; i++) {
            KlineBar bar = new KlineBar();
            bar.setDate("2026-08-" + String.format("%02d", (i % 30 + 1)));
            double base = 100.0 + i * 0.5;
            bar.setOpen(BigDecimal.valueOf(base - 1));
            bar.setClose(BigDecimal.valueOf(base + 0.2));
            bar.setHigh(BigDecimal.valueOf(base + 2.0));
            bar.setLow(BigDecimal.valueOf(base - 2.0));
            bar.setVolume(50000L);
            bar.setTurnoverRate(BigDecimal.valueOf(2.5)); // 换手率 2.5%
            bars.add(bar);
        }

        ChipData chipData = ChipsEngine.calcCYQ(bars, BigDecimal.valueOf(130.0));

        Assertions.assertNotNull(chipData);
        Assertions.assertNotNull(chipData.getAverageCost());
        Assertions.assertNotNull(chipData.getProfitRatio());
        Assertions.assertEquals(100, chipData.getBins().size());
        // 现价 130 处于整体价格区间偏上方，获利盘比例应该较高 (> 0.5)
        Assertions.assertTrue(chipData.getProfitRatio().doubleValue() > 0.5);
        Assertions.assertNotNull(chipData.getConcentration70());
        Assertions.assertNotNull(chipData.getConcentration90());
    }
}
