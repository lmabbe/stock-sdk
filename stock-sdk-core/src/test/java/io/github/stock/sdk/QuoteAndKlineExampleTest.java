package io.github.stock.sdk;

import io.github.stock.sdk.enums.AdjustType;
import io.github.stock.sdk.enums.Period;
import io.github.stock.sdk.model.*;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * 实时行情 (Quotes) 与 K 线 (Kline) 功能测试与演示代码
 *
 * 可以通过 JUnit 运行单元测试，也可以直接运行 main() 方法在控制台查看打印输出。
 */
public class QuoteAndKlineExampleTest {

    public static void main(String[] args) {
        QuoteAndKlineExampleTest example = new QuoteAndKlineExampleTest();
        System.out.println("==================================================");
        System.out.println("           1. 测试获取股票实时行情 (Quotes)");
        System.out.println("==================================================");
        example.testFetchQuotes();

        System.out.println("\n==================================================");
        System.out.println("           2. 测试获取股票 K 线数据 (Kline)");
        System.out.println("==================================================");
        example.testFetchKlines();
    }

    /**
     * 测试实时行情获取：单只、批量、五档盘口、极简行情、跨市场(港股/美股)
     */
    @Test
    public void testFetchQuotes() {
        StockSDK sdk = StockSDK.getDefault();

        // 1. 获取单只 A 股实时行情（支持 600519, sh600519, 600519.SH 等格式）
        String symbol = "600519"; // 贵州茅台
        System.out.println(">>> 正在获取股票实时行情: " + symbol);
        Quote quote = sdk.quotes.cn(symbol);

        if (quote != null) {
            System.out.println("--------------------------------------------------");
            System.out.println("股票名称: " + quote.getName() + " (" + quote.getSymbol() + ")");
            System.out.println("最新现价: " + quote.getPrice() + " 元");
            System.out.println("今日今开: " + quote.getOpen() + " 元 | 昨日收盘: " + quote.getPreClose() + " 元");
            System.out.println("今日最高: " + quote.getHigh() + " 元 | 今日最低: " + quote.getLow() + " 元");
            System.out.println("涨跌额度: " + quote.getChange() + " 元");
            System.out.println("涨跌幅度: " + quote.getChangePercent() + "%");
            System.out.println("成交手数: " + quote.getVolume() + " 手");
            System.out.println("成交金额: " + (quote.getAmount() != null ? String.format("%.2f 亿元", quote.getAmount().doubleValue() / 100_000_000.0) : "-"));
            System.out.println("换手比率: " + quote.getTurnoverRate() + "%");
            System.out.println("市盈率PE: " + quote.getPe() + " | 市净率PB: " + quote.getPb());
            System.out.println("行情时间: " + quote.getTimestamp());

            // 打印五档盘口
            System.out.println("\n[五档买卖买卖盘口]");
            List<OrderBookLevel> asks = quote.getAsks(); // 卖五到卖一
            for (int i = asks.size() - 1; i >= 0; i--) {
                System.out.printf("  卖%d: %8s  | 委托量: %d 手\n", i + 1, asks.get(i).getPrice(), asks.get(i).getVolume());
            }
            System.out.println("  ----------------------------");
            List<OrderBookLevel> bids = quote.getBids(); // 买一到买五
            for (int i = 0; i < bids.size(); i++) {
                System.out.printf("  买%d: %8s  | 委托量: %d 手\n", i + 1, bids.get(i).getPrice(), bids.get(i).getVolume());
            }
        } else {
            System.out.println("未获取到行情数据，可能网络异常或非交易时段");
        }

        // 2. 批量查询多只股票（包含沪市、深市主板、创业板）
        System.out.println("\n>>> 批量获取多只股票行情:");
        List<Quote> batchList = sdk.quotes.cn(List.of("sh600519", "sz000001", "sz300750"));
        for (Quote q : batchList) {
            System.out.printf("  [%s] %-6s 现价: %-8s 涨跌幅: %6s%% 成交额: %s\n",
                    q.getSymbol(), q.getName(), q.getPrice(), q.getChangePercent(),
                    q.getAmount() != null ? String.format("%.2f 亿", q.getAmount().doubleValue() / 1e8) : "-");
        }

        // 3. 极简轻量行情（适合列表展示与移动端节省流量）
        System.out.println("\n>>> 获取极简行情结构 (SimpleQuote):");
        SimpleQuote simple = sdk.quotes.cnSimple("000001");
        if (simple != null) {
            System.out.println("  " + simple.getName() + " -> 最新价: " + simple.getPrice() + ", 涨跌: " + simple.getChangePercent() + "%");
        }

        // 4. 跨市场行情查询：港股 & 美股
        try {
            Quote hk = sdk.quotes.hk("00700"); // 腾讯控股
            if (hk != null) {
                System.out.printf("  [港股] %s (%s) 最新价: %s 港元, 涨幅: %s%%\n",
                        hk.getName(), hk.getSymbol(), hk.getPrice(), hk.getChangePercent());
            }

            Quote us = sdk.quotes.us("AAPL"); // 苹果
            if (us != null) {
                System.out.printf("  [美股] %s (%s) 最新价: %s 美元, 涨幅: %s%%\n",
                        us.getName(), us.getSymbol(), us.getPrice(), us.getChangePercent());
            }
        } catch (Exception e) {
            System.out.println("跨市场行情查询提示: " + e.getMessage());
        }
    }

    /**
     * 测试 K 线数据获取：历史日 K 线、周 K 线、15 分钟 K 线、当日分时图、指标自动装配
     */
    @Test
    public void testFetchKlines() {
        StockSDK sdk = StockSDK.getDefault();
        String symbol = "600519"; // 贵州茅台

        // 1. 获取日 K 线（默认前复权 QFQ，最新 10 根演示）
        System.out.println(">>> 1. 获取历史日 K 线 (前复权, 最近 10 根):");
        List<KlineBar> dailyBars = sdk.kline.cn(symbol, Period.DAILY, AdjustType.QFQ, 10);
        System.out.println("日期        | 开盘价   | 收盘价   | 最高价   | 最低价   | 涨跌幅   | 成交量(手)");
        System.out.println("--------------------------------------------------------------------------");
        for (KlineBar bar : dailyBars) {
            System.out.printf("%-10s | %-8s | %-8s | %-8s | %-8s | %6s%% | %d\n",
                    bar.getDate(), bar.getOpen(), bar.getClose(), bar.getHigh(), bar.getLow(),
                    bar.getChangePercent() != null ? bar.getChangePercent() : "-", bar.getVolume());
        }

        // 2. 获取周 K 线 (后复权 HFQ, 最近 5 根)
        System.out.println("\n>>> 2. 获取周 K 线 (后复权 HFQ, 最近 5 根):");
        List<KlineBar> weeklyBars = sdk.kline.cn(symbol, Period.WEEKLY, AdjustType.HFQ, 5);
        for (KlineBar bar : weeklyBars) {
            System.out.printf("周K日期: %s | 收盘价: %s | 涨跌幅: %s%%\n",
                    bar.getDate(), bar.getClose(), bar.getChangePercent());
        }

        // 3. 获取 15 分钟 K 线
        System.out.println("\n>>> 3. 获取 15 分钟级 K 线 (最近 5 根):");
        List<KlineBar> min15Bars = sdk.kline.cnMinute(symbol, "15", 5);
        for (KlineBar bar : min15Bars) {
            System.out.printf("时间点: %s | 收盘价: %s | 成交量: %d 手\n",
                    bar.getDate(), bar.getClose(), bar.getVolume());
        }

        // 4. 获取当日分时图分钟走势 (Timeline)
        System.out.println("\n>>> 4. 获取当日分时图走势 (前 5 个分钟点):");
        TimelineData timeline = sdk.kline.timeline(symbol);
        if (timeline != null && !timeline.getItems().isEmpty()) {
            System.out.println("标的: " + timeline.getName() + " | 昨日收盘基准: " + timeline.getPreClose());
            List<TimelineItem> items = timeline.getItems();
            int count = Math.min(5, items.size());
            for (int i = 0; i < count; i++) {
                TimelineItem item = items.get(i);
                System.out.printf("  时刻: %-16s 现价: %-8s 均价(黄线): %-8s 涨跌: %6s%% 成交: %d 手\n",
                        item.getTime(), item.getPrice(), item.getAvgPrice(), item.getChangePercent(), item.getVolume());
            }
        } else {
            System.out.println("当前未获取到分时数据（可能非交易日或休市）");
        }

        // 5. 获取 K 线并自动装配核心技术指标 (withIndicators: MA5/10/20, MACD, BOLL, KDJ, RSI)
        System.out.println("\n>>> 5. 获取 K 线并自动装配技术指标 (展示最新一根):");
        List<EnrichedKline> enrichedBars = sdk.kline.withIndicators(symbol, Period.DAILY, AdjustType.QFQ, 30);
        if (!enrichedBars.isEmpty()) {
            EnrichedKline latest = enrichedBars.get(enrichedBars.size() - 1);
            System.out.println("最新K线日期: " + latest.getDate() + ", 收盘价: " + latest.getClose());
            System.out.println("  MA 均线指标 : MA5=" + latest.getIndicator("ma5")
                    + ", MA10=" + latest.getIndicator("ma10")
                    + ", MA20=" + latest.getIndicator("ma20"));
            System.out.println("  MACD 指标  : DIF=" + latest.getIndicator("macd.dif")
                    + ", DEA=" + latest.getIndicator("macd.dea")
                    + ", 柱状图(Hist)=" + latest.getIndicator("macd.hist"));
            System.out.println("  BOLL 布林带 : 上轨=" + latest.getIndicator("boll.upper")
                    + ", 中轨=" + latest.getIndicator("boll.mid")
                    + ", 下轨=" + latest.getIndicator("boll.lower"));
            System.out.println("  KDJ 随机指标: K=" + latest.getIndicator("kdj.k")
                    + ", D=" + latest.getIndicator("kdj.d")
                    + ", J=" + latest.getIndicator("kdj.j"));
            System.out.println("  RSI 强弱指标: RSI(14)=" + latest.getIndicator("rsi14"));
        }
    }
}
