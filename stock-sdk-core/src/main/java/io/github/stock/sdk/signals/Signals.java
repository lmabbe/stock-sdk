package io.github.stock.sdk.signals;

import io.github.stock.sdk.indicators.BollResult;
import io.github.stock.sdk.indicators.Indicators;
import io.github.stock.sdk.indicators.KdjResult;
import io.github.stock.sdk.indicators.MacdResult;
import io.github.stock.sdk.model.KlineBar;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 交易信号识别分析器
 * 自动识别双均线金叉/死叉、MACD金叉/死叉、KDJ超买超卖/交叉、RSI极值、布林带突破等
 */
public class Signals {

    /**
     * 自动综合扫描 K 线序列中的全部技术信号
     */
    public static List<SignalEvent> scanAllSignals(List<KlineBar> bars) {
        if (bars == null || bars.size() < 10) {
            return Collections.emptyList();
        }

        List<SignalEvent> events = new ArrayList<>();
        events.addAll(detectMaCross(bars, 5, 20));
        events.addAll(detectMacdCross(bars));
        events.addAll(detectKdjCross(bars));
        events.addAll(detectRsiExtremes(bars, 14, 80.0, 20.0));
        events.addAll(detectBollBreakout(bars, 20, 2.0));

        events.sort((a, b) -> Integer.compare(a.getIndex(), b.getIndex()));
        return events;
    }

    /**
     * 双均线金叉与死叉识别 (例如 5日均线与 20日均线)
     */
    public static List<SignalEvent> detectMaCross(List<KlineBar> bars, int fastPeriod, int slowPeriod) {
        List<SignalEvent> events = new ArrayList<>();
        double[] fastMA = Indicators.calcSMA(bars, fastPeriod);
        double[] slowMA = Indicators.calcSMA(bars, slowPeriod);

        for (int i = 1; i < bars.size(); i++) {
            if (Double.isNaN(fastMA[i]) || Double.isNaN(slowMA[i]) || Double.isNaN(fastMA[i - 1]) || Double.isNaN(slowMA[i - 1])) {
                continue;
            }

            // 金叉：快线上穿慢线
            if (fastMA[i - 1] <= slowMA[i - 1] && fastMA[i] > slowMA[i]) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "MA_GOLDEN_CROSS",
                        SignalEvent.Type.BUY,
                        String.format("MA%d 上穿 MA%d 形成金叉买入信号", fastPeriod, slowPeriod),
                        bars.get(i).closeVal()
                ));
            }
            // 死叉：快线下穿慢线
            else if (fastMA[i - 1] >= slowMA[i - 1] && fastMA[i] < slowMA[i]) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "MA_DEATH_CROSS",
                        SignalEvent.Type.SELL,
                        String.format("MA%d 下穿 MA%d 形成死叉卖出信号", fastPeriod, slowPeriod),
                        bars.get(i).closeVal()
                ));
            }
        }
        return events;
    }

    /**
     * MACD 金叉与死叉识别 (DIF 与 DEA 交叉)
     */
    public static List<SignalEvent> detectMacdCross(List<KlineBar> bars) {
        List<SignalEvent> events = new ArrayList<>();
        MacdResult macd = Indicators.calcMACD(bars);
        double[] dif = macd.getDif();
        double[] dea = macd.getDea();

        for (int i = 1; i < bars.size(); i++) {
            // DIF 上穿 DEA
            if (dif[i - 1] <= dea[i - 1] && dif[i] > dea[i]) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "MACD_GOLDEN_CROSS",
                        SignalEvent.Type.BUY,
                        "MACD DIF 上穿 DEA 形成金叉",
                        bars.get(i).closeVal()
                ));
            }
            // DIF 下穿 DEA
            else if (dif[i - 1] >= dea[i - 1] && dif[i] < dea[i]) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "MACD_DEATH_CROSS",
                        SignalEvent.Type.SELL,
                        "MACD DIF 下穿 DEA 形成死叉",
                        bars.get(i).closeVal()
                ));
            }
        }
        return events;
    }

    /**
     * KDJ 金叉与死叉识别 (K线与 D线交叉)
     */
    public static List<SignalEvent> detectKdjCross(List<KlineBar> bars) {
        List<SignalEvent> events = new ArrayList<>();
        KdjResult kdj = Indicators.calcKDJ(bars);
        double[] k = kdj.getK();
        double[] d = kdj.getD();

        for (int i = 1; i < bars.size(); i++) {
            if (k[i - 1] <= d[i - 1] && k[i] > d[i]) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "KDJ_GOLDEN_CROSS",
                        SignalEvent.Type.BUY,
                        "KDJ K线 上穿 D线 形成金叉",
                        bars.get(i).closeVal()
                ));
            } else if (k[i - 1] >= d[i - 1] && k[i] < d[i]) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "KDJ_DEATH_CROSS",
                        SignalEvent.Type.SELL,
                        "KDJ K线 下穿 D线 形成死叉",
                        bars.get(i).closeVal()
                ));
            }
        }
        return events;
    }

    /**
     * RSI 超买与超卖识别
     */
    public static List<SignalEvent> detectRsiExtremes(List<KlineBar> bars, int period, double overbought, double oversold) {
        List<SignalEvent> events = new ArrayList<>();
        double[] rsi = Indicators.calcRSI(bars, period);

        for (int i = 1; i < bars.size(); i++) {
            if (Double.isNaN(rsi[i])) continue;

            if (rsi[i] >= overbought && rsi[i - 1] < overbought) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "RSI_OVERBOUGHT",
                        SignalEvent.Type.WARNING,
                        String.format("RSI(%d)=%.1f 突破超买线 %.1f", period, rsi[i], overbought),
                        bars.get(i).closeVal()
                ));
            } else if (rsi[i] <= oversold && rsi[i - 1] > oversold) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "RSI_OVERSOLD",
                        SignalEvent.Type.BUY,
                        String.format("RSI(%d)=%.1f 跌破超卖线 %.1f（可能见底反弹）", period, rsi[i], oversold),
                        bars.get(i).closeVal()
                ));
            }
        }
        return events;
    }

    /**
     * 布林带上轨突破与下轨击穿识别
     */
    public static List<SignalEvent> detectBollBreakout(List<KlineBar> bars, int period, double stdDev) {
        List<SignalEvent> events = new ArrayList<>();
        BollResult boll = Indicators.calcBOLL(bars, period, stdDev);
        double[] upper = boll.getUpper();
        double[] lower = boll.getLower();

        for (int i = 1; i < bars.size(); i++) {
            if (Double.isNaN(upper[i]) || Double.isNaN(lower[i])) continue;

            double curC = bars.get(i).closeVal();
            double prevC = bars.get(i - 1).closeVal();

            if (prevC <= upper[i - 1] && curC > upper[i]) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "BOLL_BREAK_UPPER",
                        SignalEvent.Type.BUY,
                        "收盘价强势突破布林带上轨",
                        curC
                ));
            } else if (prevC >= lower[i - 1] && curC < lower[i]) {
                events.add(new SignalEvent(
                        i,
                        bars.get(i).getDate(),
                        "BOLL_BREAK_LOWER",
                        SignalEvent.Type.SELL,
                        "收盘价跌破布林带下轨",
                        curC
                ));
            }
        }
        return events;
    }
}
