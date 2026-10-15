package io.github.stock.sdk;

import io.github.stock.sdk.config.SdkConfig;
import io.github.stock.sdk.model.KlineBar;
import io.github.stock.sdk.provider.DataProvider;
import io.github.stock.sdk.provider.EastMoneyProvider;
import io.github.stock.sdk.provider.SinaProvider;
import io.github.stock.sdk.provider.TencentProvider;
import io.github.stock.sdk.screener.Backtester;
import io.github.stock.sdk.screener.Screener;
import io.github.stock.sdk.service.*;
import io.github.stock.sdk.signals.SignalEvent;
import io.github.stock.sdk.signals.Signals;
import io.github.stock.sdk.util.HttpClientHelper;

import java.util.Collection;
import java.util.List;

/**
 * Stock SDK 主入口门面类（Facade）
 * 100% 对齐 Node 版 stock-sdk 命名空间架构设计
 *
 * <pre>
 * // 快速开箱即用：
 * StockSDK sdk = StockSDK.getDefault();
 * Quote quote = sdk.quotes.cn("sh600519");
 * List&lt;KlineBar&gt; klines = sdk.kline.cn("600519");
 * ChipData chips = sdk.chips.cn("600519");
 * </pre>
 */
public class StockSDK {

    private static volatile StockSDK defaultInstance;

    private final SdkConfig config;
    private final HttpClientHelper httpHelper;
    private final io.github.stock.sdk.cache.SdkCache cache;

    // 数据源提供者
    private final DataProvider tencentProvider;
    private final DataProvider eastMoneyProvider;
    private final DataProvider sinaProvider;

    // 命名空间 API (public 字段直接访问，对齐 Node 规范)
    public final QuotesService quotes;
    public final KlineService kline;
    public final FundService fund;
    public final PlateService plate;
    public final PlateService board; // 别名
    public final SearchService search;
    public final ChipsService chips;
    public final BatchService batch;
    public final MoneyFlowService moneyflow;
    public final OptionsService options;
    public final FuturesService futures;

    public StockSDK() {
        this(SdkConfig.defaultConfig());
    }

    public StockSDK(SdkConfig config) {
        this.config = config != null ? config : SdkConfig.defaultConfig();
        this.httpHelper = new HttpClientHelper(this.config);
        this.cache = new io.github.stock.sdk.cache.SdkCache(this.config.getCacheConfig());

        this.tencentProvider = new TencentProvider(this.httpHelper);
        this.eastMoneyProvider = new EastMoneyProvider(this.httpHelper);
        this.sinaProvider = new SinaProvider(this.httpHelper);

        this.quotes = new QuotesService(this.tencentProvider, this.sinaProvider, this.config, this.cache);
        this.kline = new KlineService(this.eastMoneyProvider, this.config, this.cache);
        this.fund = new FundService(this.eastMoneyProvider);
        this.plate = new PlateService(this.eastMoneyProvider);
        this.board = this.plate;
        this.search = new SearchService(this.eastMoneyProvider);
        this.chips = new ChipsService(this.eastMoneyProvider, this.tencentProvider);
        this.batch = new BatchService(this.eastMoneyProvider, (TencentProvider) this.tencentProvider);
        this.moneyflow = new MoneyFlowService(this.eastMoneyProvider);
        this.options = new OptionsService(this.eastMoneyProvider);
        this.futures = new FuturesService(this.eastMoneyProvider);
    }

    /**
     * 清空当前 SDK 的全部本地内存缓存
     */
    public void clearCache() {
        if (this.cache != null) {
            this.cache.clear();
        }
    }

    /**
     * 获取当前 SDK 内存缓存管理器
     */
    public io.github.stock.sdk.cache.SdkCache getCache() {
        return this.cache;
    }

    /**
     * 获取全局默认单例实例
     */
    public static StockSDK getDefault() {
        if (defaultInstance == null) {
            synchronized (StockSDK.class) {
                if (defaultInstance == null) {
                    defaultInstance = new StockSDK();
                }
            }
        }
        return defaultInstance;
    }

    /**
     * 建造者模式初始化 SDK
     */
    public static Builder builder() {
        return new Builder();
    }

    public SdkConfig getConfig() {
        return config;
    }

    // -------------------------------------------------------------
    // 便捷静态算法与工具方法
    // -------------------------------------------------------------

    /**
     * 创建链式选股器
     */
    public static <T> Screener<T> screen(Collection<T> items) {
        return Screener.screen(items);
    }

    /**
     * 运行单标的回测
     */
    public static Backtester.BacktestReport backtest(Backtester.BacktestOptions options) {
        return Backtester.run(options);
    }

    /**
     * 识别 K 线序列中的技术指标信号（金叉/死叉/超买超卖）
     */
    public static List<SignalEvent> scanSignals(List<KlineBar> bars) {
        return Signals.scanAllSignals(bars);
    }

    public static class Builder {
        private SdkConfig config = SdkConfig.defaultConfig();

        public Builder config(SdkConfig config) {
            this.config = config;
            return this;
        }

        public StockSDK build() {
            return new StockSDK(config);
        }
    }
}
