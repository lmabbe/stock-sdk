package io.github.stock.sdk.util;

import io.github.stock.sdk.model.ChipBin;
import io.github.stock.sdk.model.ChipData;
import io.github.stock.sdk.model.KlineBar;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 筹码分布（CYQ）计算引擎
 * 基于东方财富/通达信标准的历史换手率衰减模型（100 价格分箱）
 */
public class ChipsEngine {

    private static final int DEFAULT_BIN_COUNT = 100;
    private static final double DEFAULT_DECAY_FACTOR = 1.0;

    /**
     * 根据历史日 K 线数据计算筹码分布指标
     * @param bars 日 K 线历史数据（建议至少 60~250 根）
     * @param currentPrice 当前收盘价（若为 null 则取最后一根 K 线收盘价）
     */
    public static ChipData calcCYQ(List<KlineBar> bars, BigDecimal currentPrice) {
        if (bars == null || bars.isEmpty()) {
            return new ChipData();
        }

        BigDecimal curPrice = currentPrice != null ? currentPrice : bars.get(bars.size() - 1).getClose();
        if (curPrice == null) {
            curPrice = BigDecimal.valueOf(bars.get(bars.size() - 1).closeVal());
        }

        // 1. 扫描整体价格极值
        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;
        for (KlineBar bar : bars) {
            minPrice = Math.min(minPrice, bar.lowVal());
            maxPrice = Math.max(maxPrice, bar.highVal());
        }

        if (maxPrice <= minPrice) {
            maxPrice = minPrice + 0.01;
        }

        // 2. 初始化 100 个价格分箱
        int binCount = DEFAULT_BIN_COUNT;
        double binStep = (maxPrice - minPrice) / binCount;
        double[] chipVolumes = new double[binCount];

        // 3. 逐日衰减与新成交量堆叠
        for (KlineBar bar : bars) {
            double turnoverRate = bar.getTurnoverRate() != null ? bar.getTurnoverRate().doubleValue() / 100.0 : 0.02;
            turnoverRate = Math.min(Math.max(turnoverRate, 0.001), 0.99); // 边界保护

            // 旧筹码衰减
            double decay = 1.0 - (turnoverRate * DEFAULT_DECAY_FACTOR);
            for (int i = 0; i < binCount; i++) {
                chipVolumes[i] *= decay;
            }

            // 新筹码在当天的 [low, high] 均匀分布
            double low = bar.lowVal();
            double high = bar.highVal();
            double vol = bar.volumeVal() > 0 ? bar.volumeVal() : 10000.0;

            int startBin = (int) Math.floor((low - minPrice) / binStep);
            int endBin = (int) Math.floor((high - minPrice) / binStep);
            startBin = Math.max(0, Math.min(binCount - 1, startBin));
            endBin = Math.max(0, Math.min(binCount - 1, endBin));

            int coveredBins = Math.max(1, endBin - startBin + 1);
            double volPerBin = vol / coveredBins;
            for (int b = startBin; b <= endBin; b++) {
                chipVolumes[b] += volPerBin;
            }
        }

        // 4. 统计指标计算
        double totalVolume = 0.0;
        double weightedCostSum = 0.0;
        for (int i = 0; i < binCount; i++) {
            double p = minPrice + (i + 0.5) * binStep;
            totalVolume += chipVolumes[i];
            weightedCostSum += p * chipVolumes[i];
        }

        ChipData result = new ChipData();
        if (totalVolume <= 0) {
            return result;
        }

        double avgCost = weightedCostSum / totalVolume;
        result.setAverageCost(BigDecimal.valueOf(avgCost).setScale(2, RoundingMode.HALF_UP));

        // 获利盘比例 (小于等于当前价格的筹码占总筹码比例)
        double currentP = curPrice.doubleValue();
        double profitVolume = 0.0;
        for (int i = 0; i < binCount; i++) {
            double p = minPrice + (i + 0.5) * binStep;
            if (p <= currentP) {
                profitVolume += chipVolumes[i];
            }
        }
        double profitRatio = profitVolume / totalVolume;
        result.setProfitRatio(BigDecimal.valueOf(profitRatio).setScale(4, RoundingMode.HALF_UP));

        // 5. 集中度与区间分位数计算（70% 居中区间去掉 15% 两头；90% 居中区间去掉 5% 两头）
        double price70Low = findPriceByQuantile(chipVolumes, minPrice, binStep, totalVolume, 0.15);
        double price70High = findPriceByQuantile(chipVolumes, minPrice, binStep, totalVolume, 0.85);
        result.setCostRange70Low(BigDecimal.valueOf(price70Low).setScale(2, RoundingMode.HALF_UP));
        result.setCostRange70High(BigDecimal.valueOf(price70High).setScale(2, RoundingMode.HALF_UP));
        double conc70 = (price70High + price70Low) != 0 ? (price70High - price70Low) / (price70High + price70Low) : 0.0;
        result.setConcentration70(BigDecimal.valueOf(conc70).setScale(4, RoundingMode.HALF_UP));

        double price90Low = findPriceByQuantile(chipVolumes, minPrice, binStep, totalVolume, 0.05);
        double price90High = findPriceByQuantile(chipVolumes, minPrice, binStep, totalVolume, 0.95);
        result.setCostRange90Low(BigDecimal.valueOf(price90Low).setScale(2, RoundingMode.HALF_UP));
        result.setCostRange90High(BigDecimal.valueOf(price90High).setScale(2, RoundingMode.HALF_UP));
        double conc90 = (price90High + price90Low) != 0 ? (price90High - price90Low) / (price90High + price90Low) : 0.0;
        result.setConcentration90(BigDecimal.valueOf(conc90).setScale(4, RoundingMode.HALF_UP));

        // 6. 构造 Bins 直方图结果
        List<ChipBin> binsList = new ArrayList<>(binCount);
        for (int i = 0; i < binCount; i++) {
            double p = minPrice + (i + 0.5) * binStep;
            double vol = chipVolumes[i];
            double pct = vol / totalVolume;
            binsList.add(new ChipBin(
                    BigDecimal.valueOf(p).setScale(2, RoundingMode.HALF_UP),
                    vol,
                    pct
            ));
        }
        result.setBins(binsList);

        return result;
    }

    private static double findPriceByQuantile(double[] chipVolumes, double minPrice, double binStep, double totalVolume, double quantile) {
        double targetVolume = totalVolume * quantile;
        double accumulated = 0.0;
        for (int i = 0; i < chipVolumes.length; i++) {
            accumulated += chipVolumes[i];
            if (accumulated >= targetVolume) {
                return minPrice + (i + 0.5) * binStep;
            }
        }
        return minPrice + (chipVolumes.length - 0.5) * binStep;
    }
}
