package io.github.stock.sdk.service;

import io.github.stock.sdk.enums.AdjustType;
import io.github.stock.sdk.enums.Period;
import io.github.stock.sdk.indicators.BollResult;
import io.github.stock.sdk.indicators.Indicators;
import io.github.stock.sdk.indicators.KdjResult;
import io.github.stock.sdk.indicators.MacdResult;
import io.github.stock.sdk.model.EnrichedKline;
import io.github.stock.sdk.model.KlineBar;
import io.github.stock.sdk.provider.DataProvider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * K 线数据命名空间服务 (sdk.kline.*)
 */
public class KlineService {

    private final DataProvider provider;
    private final io.github.stock.sdk.config.SdkConfig config;
    private final io.github.stock.sdk.cache.SdkCache cache;

    public KlineService(DataProvider provider, io.github.stock.sdk.config.SdkConfig config, io.github.stock.sdk.cache.SdkCache cache) {
        this.provider = provider;
        this.config = config;
        this.cache = cache;
    }

    /**
     * 获取 A 股历史 K 线（默认日 K、前复权、最多 500 根）
     */
    public List<KlineBar> cn(String symbol) {
        return cn(symbol, Period.DAILY, AdjustType.QFQ, 500);
    }

    public List<KlineBar> cn(String symbol, Period period, AdjustType adjust, int limit) {
        Period p = period != null ? period : Period.DAILY;
        AdjustType a = adjust != null ? adjust : AdjustType.QFQ;
        int lmt = limit > 0 ? limit : 500;

        if (cache != null && config.getCacheConfig().isEnabled()) {
            String cacheKey = "kline:cn:" + symbol.toLowerCase() + ":" + p.getValue() + ":" + a.getValue() + ":" + lmt;
            return cache.getOrCompute(cacheKey, config.getCacheConfig().getKlineTtl(), () -> provider.getKlines(symbol, p, a, lmt));
        }
        return provider.getKlines(symbol, p, a, lmt);
    }

    /**
     * 获取港股历史 K 线
     */
    public List<KlineBar> hk(String symbol) {
        return hk(symbol, Period.DAILY, AdjustType.QFQ, 500);
    }

    public List<KlineBar> hk(String symbol, Period period, AdjustType adjust, int limit) {
        return provider.getKlines(symbol, period, adjust, limit);
    }

    /**
     * 获取美股历史 K 线
     */
    public List<KlineBar> us(String symbol) {
        return us(symbol, Period.DAILY, AdjustType.QFQ, 500);
    }

    public List<KlineBar> us(String symbol, Period period, AdjustType adjust, int limit) {
        return provider.getKlines(symbol, period, adjust, limit);
    }

    /**
     * 获取 A 股分钟级别 K 线 (period: 1, 5, 15, 30, 60)
     */
    public List<KlineBar> cnMinute(String symbol, String minutePeriod, int limit) {
        Period p = Period.fromValue(minutePeriod);
        return provider.getKlines(symbol, p, AdjustType.NONE, limit);
    }

    public List<KlineBar> cnMinute(String symbol, String minutePeriod) {
        return cnMinute(symbol, minutePeriod, 240);
    }

    /**
     * 获取股票当日分时图分钟级别走势（包含当前价、均价、涨跌幅、成交量、成交额）
     * @param symbol 股票代码，如 "sh600519"
     */
    public io.github.stock.sdk.model.TimelineData timeline(String symbol) {
        if (cache != null && config.getCacheConfig().isEnabled()) {
            String cacheKey = "timeline:" + symbol.toLowerCase();
            return cache.getOrCompute(cacheKey, config.getCacheConfig().getTimelineTtl(), () -> provider.getTimeline(symbol));
        }
        return provider.getTimeline(symbol);
    }

    /**
     * 异步获取 K 线
     */
    public CompletableFuture<List<KlineBar>> cnAsync(String symbol, Period period, AdjustType adjust, int limit) {
        return CompletableFuture.supplyAsync(() -> cn(symbol, period, adjust, limit));
    }

    /**
     * 获取并自动装配核心常用技术指标 (MA5, MA10, MA20, MACD, BOLL, KDJ, RSI)
     * 对应 Node 版 sdk.kline.withIndicators()
     */
    public List<EnrichedKline> withIndicators(String symbol, Period period, AdjustType adjust, int limit) {
        List<KlineBar> bars = provider.getKlines(symbol, period, adjust, limit);
        if (bars == null || bars.isEmpty()) {
            return Collections.emptyList();
        }

        // 计算技术指标
        double[] ma5 = Indicators.calcSMA(bars, 5);
        double[] ma10 = Indicators.calcSMA(bars, 10);
        double[] ma20 = Indicators.calcSMA(bars, 20);
        MacdResult macd = Indicators.calcMACD(bars);
        BollResult boll = Indicators.calcBOLL(bars);
        KdjResult kdj = Indicators.calcKDJ(bars);
        double[] rsi14 = Indicators.calcRSI(bars, 14);

        List<EnrichedKline> result = new ArrayList<>(bars.size());
        for (int i = 0; i < bars.size(); i++) {
            EnrichedKline ek = new EnrichedKline(bars.get(i));

            if (!Double.isNaN(ma5[i])) ek.addIndicator("ma5", ma5[i]);
            if (!Double.isNaN(ma10[i])) ek.addIndicator("ma10", ma10[i]);
            if (!Double.isNaN(ma20[i])) ek.addIndicator("ma20", ma20[i]);

            ek.addIndicator("macd.dif", macd.getDif()[i]);
            ek.addIndicator("macd.dea", macd.getDea()[i]);
            ek.addIndicator("macd.hist", macd.getHistogram()[i]);

            if (!Double.isNaN(boll.getUpper()[i])) {
                ek.addIndicator("boll.upper", boll.getUpper()[i]);
                ek.addIndicator("boll.mid", boll.getMiddle()[i]);
                ek.addIndicator("boll.lower", boll.getLower()[i]);
            }

            ek.addIndicator("kdj.k", kdj.getK()[i]);
            ek.addIndicator("kdj.d", kdj.getD()[i]);
            ek.addIndicator("kdj.j", kdj.getJ()[i]);

            if (!Double.isNaN(rsi14[i])) ek.addIndicator("rsi14", rsi14[i]);

            result.add(ek);
        }

        return result;
    }

    public List<EnrichedKline> withIndicators(String symbol) {
        return withIndicators(symbol, Period.DAILY, AdjustType.QFQ, 250);
    }
}
