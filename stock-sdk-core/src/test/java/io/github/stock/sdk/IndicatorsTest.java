package io.github.stock.sdk;

import io.github.stock.sdk.indicators.*;
import io.github.stock.sdk.model.KlineBar;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class IndicatorsTest {

    private List<KlineBar> mockBars;

    @BeforeEach
    public void setup() {
        mockBars = new ArrayList<>();
        double[] prices = {10, 11, 12, 11, 13, 14, 15, 14, 16, 17, 18, 19, 20, 21, 22, 21, 20, 19, 18, 17};
        for (int i = 0; i < prices.length; i++) {
            double p = prices[i];
            KlineBar bar = new KlineBar();
            bar.setDate("2026-10-" + (i + 1));
            bar.setOpen(BigDecimal.valueOf(p - 0.5));
            bar.setClose(BigDecimal.valueOf(p));
            bar.setHigh(BigDecimal.valueOf(p + 1.0));
            bar.setLow(BigDecimal.valueOf(p - 1.0));
            bar.setVolume((long) (1000 + i * 100));
            bar.setTurnover(BigDecimal.valueOf((1000 + i * 100) * p));
            mockBars.add(bar);
        }
    }

    @Test
    public void testSMA() {
        double[] closes = Indicators.extractCloses(mockBars);
        double[] ma5 = Indicators.calcSMA(closes, 5);

        Assertions.assertTrue(Double.isNaN(ma5[0]));
        Assertions.assertTrue(Double.isNaN(ma5[3]));
        // 前5天：10, 11, 12, 11, 13 -> 均值 11.4
        Assertions.assertEquals(11.4, ma5[4], 0.001);
    }

    @Test
    public void testMACD() {
        MacdResult macd = Indicators.calcMACD(mockBars);
        Assertions.assertEquals(mockBars.size(), macd.getDif().length);
        Assertions.assertEquals(mockBars.size(), macd.getDea().length);
        Assertions.assertEquals(mockBars.size(), macd.getHistogram().length);
    }

    @Test
    public void testBOLL() {
        BollResult boll = Indicators.calcBOLL(mockBars, 10, 2.0);
        Assertions.assertEquals(mockBars.size(), boll.getUpper().length);
        Assertions.assertEquals(mockBars.size(), boll.getMiddle().length);
        Assertions.assertEquals(mockBars.size(), boll.getLower().length);

        // 检验在有效周期后，upper >= middle >= lower
        for (int i = 10; i < mockBars.size(); i++) {
            Assertions.assertTrue(boll.getUpper()[i] >= boll.getMiddle()[i]);
            Assertions.assertTrue(boll.getMiddle()[i] >= boll.getLower()[i]);
        }
    }

    @Test
    public void testKDJ() {
        KdjResult kdj = Indicators.calcKDJ(mockBars);
        Assertions.assertEquals(mockBars.size(), kdj.getK().length);
        Assertions.assertEquals(mockBars.size(), kdj.getD().length);
        Assertions.assertEquals(mockBars.size(), kdj.getJ().length);
    }

    @Test
    public void testRSI() {
        double[] rsi = Indicators.calcRSI(mockBars, 14);
        Assertions.assertEquals(mockBars.size(), rsi.length);
        for (int i = 14; i < rsi.length; i++) {
            Assertions.assertTrue(rsi[i] >= 0 && rsi[i] <= 100);
        }
    }

    @Test
    public void testATR() {
        double[] atr = Indicators.calcATR(mockBars, 10);
        Assertions.assertEquals(mockBars.size(), atr.length);
        Assertions.assertTrue(atr[atr.length - 1] > 0);
    }
}
