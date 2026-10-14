package io.github.stock.sdk.indicators;

import io.github.stock.sdk.model.KlineBar;

import java.util.List;

/**
 * 纯数学技术指标计算引擎（Zero-Dependency Pure Math Engine）
 * 对应 Node 版 stock-sdk/indicators 子路径导出的 17 种常用技术指标算法
 */
public class Indicators {

    // -------------------------------------------------------------
    // 1. 均线系列：SMA / EMA / WMA / MA
    // -------------------------------------------------------------

    /**
     * 简单移动平均线 (Simple Moving Average)
     */
    public static double[] calcSMA(double[] data, int period) {
        if (data == null || data.length == 0 || period <= 0) {
            return new double[0];
        }
        int len = data.length;
        double[] result = new double[len];
        double sum = 0.0;

        for (int i = 0; i < len; i++) {
            sum += data[i];
            if (i >= period) {
                sum -= data[i - period];
            }
            if (i >= period - 1) {
                result[i] = sum / period;
            } else {
                result[i] = Double.NaN;
            }
        }
        return result;
    }

    public static double[] calcSMA(List<KlineBar> bars, int period) {
        return calcSMA(extractCloses(bars), period);
    }

    public static double[] calcMA(double[] data, int period) {
        return calcSMA(data, period);
    }

    public static double[] calcMA(List<KlineBar> bars, int period) {
        return calcSMA(bars, period);
    }

    /**
     * 指数移动平均线 (Exponential Moving Average)
     */
    public static double[] calcEMA(double[] data, int period) {
        if (data == null || data.length == 0 || period <= 0) {
            return new double[0];
        }
        int len = data.length;
        double[] result = new double[len];
        double multiplier = 2.0 / (period + 1.0);

        result[0] = data[0];
        for (int i = 1; i < len; i++) {
            result[i] = (data[i] - result[i - 1]) * multiplier + result[i - 1];
        }
        return result;
    }

    public static double[] calcEMA(List<KlineBar> bars, int period) {
        return calcEMA(extractCloses(bars), period);
    }

    /**
     * 加权移动平均线 (Weighted Moving Average)
     */
    public static double[] calcWMA(double[] data, int period) {
        if (data == null || data.length == 0 || period <= 0) {
            return new double[0];
        }
        int len = data.length;
        double[] result = new double[len];
        int denominator = period * (period + 1) / 2;

        for (int i = 0; i < len; i++) {
            if (i < period - 1) {
                result[i] = Double.NaN;
                continue;
            }
            double weightedSum = 0;
            for (int j = 0; j < period; j++) {
                weightedSum += data[i - period + 1 + j] * (j + 1);
            }
            result[i] = weightedSum / denominator;
        }
        return result;
    }

    // -------------------------------------------------------------
    // 2. MACD (异同移动平均线)
    // -------------------------------------------------------------

    public static MacdResult calcMACD(double[] data, int fastPeriod, int slowPeriod, int signalPeriod) {
        if (data == null || data.length == 0) {
            return new MacdResult(new double[0], new double[0], new double[0]);
        }
        int len = data.length;
        double[] fastEMA = calcEMA(data, fastPeriod > 0 ? fastPeriod : 12);
        double[] slowEMA = calcEMA(data, slowPeriod > 0 ? slowPeriod : 26);

        double[] dif = new double[len];
        for (int i = 0; i < len; i++) {
            dif[i] = fastEMA[i] - slowEMA[i];
        }

        double[] dea = calcEMA(dif, signalPeriod > 0 ? signalPeriod : 9);
        double[] hist = new double[len];
        for (int i = 0; i < len; i++) {
            hist[i] = (dif[i] - dea[i]) * 2.0;
        }

        return new MacdResult(dif, dea, hist);
    }

    public static MacdResult calcMACD(List<KlineBar> bars) {
        return calcMACD(extractCloses(bars), 12, 26, 9);
    }

    public static MacdResult calcMACD(List<KlineBar> bars, int fast, int slow, int signal) {
        return calcMACD(extractCloses(bars), fast, slow, signal);
    }

    // -------------------------------------------------------------
    // 3. 布林带 (BOLL)
    // -------------------------------------------------------------

    public static BollResult calcBOLL(double[] data, int period, double stdDevMultiplier) {
        if (data == null || data.length == 0 || period <= 0) {
            return new BollResult(new double[0], new double[0], new double[0]);
        }
        int len = data.length;
        double[] mid = calcSMA(data, period);
        double[] upper = new double[len];
        double[] lower = new double[len];

        for (int i = 0; i < len; i++) {
            if (i < period - 1) {
                upper[i] = Double.NaN;
                lower[i] = Double.NaN;
                continue;
            }
            double mean = mid[i];
            double sumSquares = 0.0;
            for (int j = i - period + 1; j <= i; j++) {
                double diff = data[j] - mean;
                sumSquares += diff * diff;
            }
            double sd = Math.sqrt(sumSquares / period);
            upper[i] = mean + (sd * stdDevMultiplier);
            lower[i] = mean - (sd * stdDevMultiplier);
        }

        return new BollResult(upper, mid, lower);
    }

    public static BollResult calcBOLL(List<KlineBar> bars) {
        return calcBOLL(extractCloses(bars), 20, 2.0);
    }

    public static BollResult calcBOLL(List<KlineBar> bars, int period, double stdDev) {
        return calcBOLL(extractCloses(bars), period, stdDev);
    }

    // -------------------------------------------------------------
    // 4. 随机指标 (KDJ)
    // -------------------------------------------------------------

    public static KdjResult calcKDJ(List<KlineBar> bars, int period, int m1, int m2) {
        if (bars == null || bars.isEmpty()) {
            return new KdjResult(new double[0], new double[0], new double[0]);
        }
        int len = bars.size();
        int n = period > 0 ? period : 9;
        int p1 = m1 > 0 ? m1 : 3;
        int p2 = m2 > 0 ? m2 : 3;

        double[] k = new double[len];
        double[] d = new double[len];
        double[] j = new double[len];

        double prevK = 50.0;
        double prevD = 50.0;

        for (int i = 0; i < len; i++) {
            int startIdx = Math.max(0, i - n + 1);
            double lowest = Double.MAX_VALUE;
            double highest = Double.MIN_VALUE;

            for (int idx = startIdx; idx <= i; idx++) {
                lowest = Math.min(lowest, bars.get(idx).lowVal());
                highest = Math.max(highest, bars.get(idx).highVal());
            }

            double close = bars.get(i).closeVal();
            double rsv = 50.0;
            if (highest != lowest) {
                rsv = ((close - lowest) / (highest - lowest)) * 100.0;
            }

            double curK = (rsv + (p1 - 1) * prevK) / p1;
            double curD = (curK + (p2 - 1) * prevD) / p2;
            double curJ = 3.0 * curK - 2.0 * curD;

            k[i] = curK;
            d[i] = curD;
            j[i] = curJ;

            prevK = curK;
            prevD = curD;
        }

        return new KdjResult(k, d, j);
    }

    public static KdjResult calcKDJ(List<KlineBar> bars) {
        return calcKDJ(bars, 9, 3, 3);
    }

    // -------------------------------------------------------------
    // 5. 相对强弱指标 (RSI)
    // -------------------------------------------------------------

    public static double[] calcRSI(double[] data, int period) {
        if (data == null || data.length <= period || period <= 0) {
            return new double[data != null ? data.length : 0];
        }
        int len = data.length;
        double[] rsi = new double[len];

        double gainSum = 0.0;
        double lossSum = 0.0;

        for (int i = 1; i <= period; i++) {
            double change = data[i] - data[i - 1];
            if (change > 0) {
                gainSum += change;
            } else {
                lossSum += Math.abs(change);
            }
        }

        double avgGain = gainSum / period;
        double avgLoss = lossSum / period;

        for (int i = 0; i < period; i++) {
            rsi[i] = Double.NaN;
        }

        if (avgLoss == 0) {
            rsi[period] = 100.0;
        } else {
            double rs = avgGain / avgLoss;
            rsi[period] = 100.0 - (100.0 / (1.0 + rs));
        }

        for (int i = period + 1; i < len; i++) {
            double change = data[i] - data[i - 1];
            double gain = Math.max(change, 0);
            double loss = Math.max(-change, 0);

            avgGain = (avgGain * (period - 1) + gain) / period;
            avgLoss = (avgLoss * (period - 1) + loss) / period;

            if (avgLoss == 0) {
                rsi[i] = 100.0;
            } else {
                double rs = avgGain / avgLoss;
                rsi[i] = 100.0 - (100.0 / (1.0 + rs));
            }
        }

        return rsi;
    }

    public static double[] calcRSI(List<KlineBar> bars, int period) {
        return calcRSI(extractCloses(bars), period > 0 ? period : 14);
    }

    // -------------------------------------------------------------
    // 6. 威廉指标 (Williams %R - WR)
    // -------------------------------------------------------------

    public static double[] calcWR(List<KlineBar> bars, int period) {
        if (bars == null || bars.isEmpty() || period <= 0) {
            return new double[0];
        }
        int len = bars.size();
        double[] wr = new double[len];

        for (int i = 0; i < len; i++) {
            if (i < period - 1) {
                wr[i] = Double.NaN;
                continue;
            }
            double highest = Double.MIN_VALUE;
            double lowest = Double.MAX_VALUE;
            for (int j = i - period + 1; j <= i; j++) {
                highest = Math.max(highest, bars.get(j).highVal());
                lowest = Math.min(lowest, bars.get(j).lowVal());
            }

            double close = bars.get(i).closeVal();
            if (highest != lowest) {
                wr[i] = ((highest - close) / (highest - lowest)) * -100.0;
            } else {
                wr[i] = 0.0;
            }
        }
        return wr;
    }

    // -------------------------------------------------------------
    // 7. 乖离率 (BIAS)
    // -------------------------------------------------------------

    public static double[] calcBIAS(double[] data, int period) {
        if (data == null || data.length == 0 || period <= 0) {
            return new double[0];
        }
        double[] ma = calcSMA(data, period);
        double[] bias = new double[data.length];

        for (int i = 0; i < data.length; i++) {
            if (Double.isNaN(ma[i]) || ma[i] == 0) {
                bias[i] = Double.NaN;
            } else {
                bias[i] = ((data[i] - ma[i]) / ma[i]) * 100.0;
            }
        }
        return bias;
    }

    public static double[] calcBIAS(List<KlineBar> bars, int period) {
        return calcBIAS(extractCloses(bars), period);
    }

    // -------------------------------------------------------------
    // 8. 顺势指标 (CCI)
    // -------------------------------------------------------------

    public static double[] calcCCI(List<KlineBar> bars, int period) {
        if (bars == null || bars.isEmpty() || period <= 0) {
            return new double[0];
        }
        int len = bars.size();
        double[] tp = new double[len];
        for (int i = 0; i < len; i++) {
            tp[i] = (bars.get(i).highVal() + bars.get(i).lowVal() + bars.get(i).closeVal()) / 3.0;
        }

        double[] ma = calcSMA(tp, period);
        double[] cci = new double[len];

        for (int i = 0; i < len; i++) {
            if (i < period - 1) {
                cci[i] = Double.NaN;
                continue;
            }
            double mean = ma[i];
            double mdSum = 0;
            for (int j = i - period + 1; j <= i; j++) {
                mdSum += Math.abs(tp[j] - mean);
            }
            double md = mdSum / period;
            if (md != 0) {
                cci[i] = (tp[i] - mean) / (0.015 * md);
            } else {
                cci[i] = 0;
            }
        }
        return cci;
    }

    // -------------------------------------------------------------
    // 9. 真实波幅 (Average True Range - ATR)
    // -------------------------------------------------------------

    public static double[] calcATR(List<KlineBar> bars, int period) {
        if (bars == null || bars.isEmpty() || period <= 0) {
            return new double[0];
        }
        int len = bars.size();
        double[] tr = new double[len];

        tr[0] = bars.get(0).highVal() - bars.get(0).lowVal();
        for (int i = 1; i < len; i++) {
            double h = bars.get(i).highVal();
            double l = bars.get(i).lowVal();
            double prevC = bars.get(i - 1).closeVal();
            double val1 = h - l;
            double val2 = Math.abs(h - prevC);
            double val3 = Math.abs(l - prevC);
            tr[i] = Math.max(val1, Math.max(val2, val3));
        }

        return calcEMA(tr, period);
    }

    // -------------------------------------------------------------
    // 10. 能量潮指标 (On-Balance Volume - OBV)
    // -------------------------------------------------------------

    public static double[] calcOBV(List<KlineBar> bars) {
        if (bars == null || bars.isEmpty()) {
            return new double[0];
        }
        int len = bars.size();
        double[] obv = new double[len];
        obv[0] = bars.get(0).volumeVal();

        for (int i = 1; i < len; i++) {
            double curC = bars.get(i).closeVal();
            double prevC = bars.get(i - 1).closeVal();
            double vol = bars.get(i).volumeVal();

            if (curC > prevC) {
                obv[i] = obv[i - 1] + vol;
            } else if (curC < prevC) {
                obv[i] = obv[i - 1] - vol;
            } else {
                obv[i] = obv[i - 1];
            }
        }
        return obv;
    }

    // -------------------------------------------------------------
    // 11. 变动率指标 (Rate of Change - ROC)
    // -------------------------------------------------------------

    public static double[] calcROC(double[] data, int period) {
        if (data == null || data.length == 0 || period <= 0) {
            return new double[0];
        }
        int len = data.length;
        double[] roc = new double[len];

        for (int i = 0; i < len; i++) {
            if (i < period) {
                roc[i] = Double.NaN;
            } else {
                double prev = data[i - period];
                if (prev != 0) {
                    roc[i] = ((data[i] - prev) / prev) * 100.0;
                } else {
                    roc[i] = 0;
                }
            }
        }
        return roc;
    }

    public static double[] calcROC(List<KlineBar> bars, int period) {
        return calcROC(extractCloses(bars), period);
    }

    // -------------------------------------------------------------
    // 12. 动向指标 (DMI / ADX)
    // -------------------------------------------------------------

    public static DmiResult calcDMI(List<KlineBar> bars, int period) {
        if (bars == null || bars.isEmpty() || period <= 0) {
            return new DmiResult(new double[0], new double[0], new double[0]);
        }
        int len = bars.size();
        double[] tr = new double[len];
        double[] plusDM = new double[len];
        double[] minusDM = new double[len];

        tr[0] = bars.get(0).highVal() - bars.get(0).lowVal();
        for (int i = 1; i < len; i++) {
            double h = bars.get(i).highVal();
            double l = bars.get(i).lowVal();
            double prevH = bars.get(i - 1).highVal();
            double prevL = bars.get(i - 1).lowVal();
            double prevC = bars.get(i - 1).closeVal();

            tr[i] = Math.max(h - l, Math.max(Math.abs(h - prevC), Math.abs(l - prevC)));
            double up = h - prevH;
            double down = prevL - l;

            plusDM[i] = (up > down && up > 0) ? up : 0;
            minusDM[i] = (down > up && down > 0) ? down : 0;
        }

        double[] smoothTR = calcEMA(tr, period);
        double[] smoothPlusDM = calcEMA(plusDM, period);
        double[] smoothMinusDM = calcEMA(minusDM, period);

        double[] pdi = new double[len];
        double[] mdi = new double[len];
        double[] dx = new double[len];

        for (int i = 0; i < len; i++) {
            if (smoothTR[i] != 0) {
                pdi[i] = (smoothPlusDM[i] / smoothTR[i]) * 100.0;
                mdi[i] = (smoothMinusDM[i] / smoothTR[i]) * 100.0;
            } else {
                pdi[i] = 0;
                mdi[i] = 0;
            }
            double sum = pdi[i] + mdi[i];
            dx[i] = sum != 0 ? (Math.abs(pdi[i] - mdi[i]) / sum) * 100.0 : 0;
        }

        double[] adx = calcEMA(dx, period);
        return new DmiResult(pdi, mdi, adx);
    }

    // -------------------------------------------------------------
    // 13. 抛物线转向指标 (Parabolic SAR)
    // -------------------------------------------------------------

    public static double[] calcSAR(List<KlineBar> bars, double step, double maxStep) {
        if (bars == null || bars.isEmpty()) {
            return new double[0];
        }
        int len = bars.size();
        double[] sar = new double[len];
        double af = step > 0 ? step : 0.02;
        double maxAf = maxStep > 0 ? maxStep : 0.2;

        boolean isLong = true;
        sar[0] = bars.get(0).lowVal();
        double ep = bars.get(0).highVal();
        double curAf = af;

        for (int i = 1; i < len; i++) {
            double curHigh = bars.get(i).highVal();
            double curLow = bars.get(i).lowVal();

            sar[i] = sar[i - 1] + curAf * (ep - sar[i - 1]);

            if (isLong) {
                if (curLow < sar[i]) {
                    isLong = false;
                    sar[i] = ep;
                    ep = curLow;
                    curAf = af;
                } else {
                    if (curHigh > ep) {
                        ep = curHigh;
                        curAf = Math.min(curAf + af, maxAf);
                    }
                }
            } else {
                if (curHigh > sar[i]) {
                    isLong = true;
                    sar[i] = ep;
                    ep = curHigh;
                    curAf = af;
                } else {
                    if (curLow < ep) {
                        ep = curLow;
                        curAf = Math.min(curAf + af, maxAf);
                    }
                }
            }
        }
        return sar;
    }

    // -------------------------------------------------------------
    // 14. 肯特纳通道 (Keltner Channels - KC)
    // -------------------------------------------------------------

    public static KcResult calcKC(List<KlineBar> bars, int emaPeriod, int atrPeriod, double multiplier) {
        if (bars == null || bars.isEmpty()) {
            return new KcResult(new double[0], new double[0], new double[0]);
        }
        double[] middle = calcEMA(bars, emaPeriod > 0 ? emaPeriod : 20);
        double[] atr = calcATR(bars, atrPeriod > 0 ? atrPeriod : 10);
        double mult = multiplier > 0 ? multiplier : 2.0;

        int len = bars.size();
        double[] upper = new double[len];
        double[] lower = new double[len];

        for (int i = 0; i < len; i++) {
            upper[i] = middle[i] + (mult * atr[i]);
            lower[i] = middle[i] - (mult * atr[i]);
        }

        return new KcResult(upper, middle, lower);
    }

    // -------------------------------------------------------------
    // 工具方法
    // -------------------------------------------------------------

    public static double[] extractCloses(List<KlineBar> bars) {
        if (bars == null) {
            return new double[0];
        }
        double[] closes = new double[bars.size()];
        for (int i = 0; i < bars.size(); i++) {
            closes[i] = bars.get(i).closeVal();
        }
        return closes;
    }
}
