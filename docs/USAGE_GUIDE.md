# Stock SDK (Java 11+) 使用开发指南

欢迎阅读 **Stock SDK (Java 11+)** 开发者使用手册！本项目是针对 Node.js 开源生态著名的股票 SDK **`stock-sdk`** 的 100% 完整 Java 复刻版，专注于为 Java 11+ 项目提供**零 Python 环境、零外部数据服务依赖、轻量且开箱即用的全能金融行情与量化分析能力**。

---

## 📑 目录

1. [项目引入与构建](#1-项目引入与构建)
2. [快速起步 (Quick Start)](#2-快速起步-quick-start)
3. [统一代码解析规则 (Symbol Model)](#3-统一代码解析规则-symbol-model)
4. [实时行情与盘口 (Quotes)](#4-实时行情与盘口-quotes)
5. [K 线行情与分时图 (Kline & Timeline)](#5-k-线行情与分时图-kline--timeline)
6. [17 种技术指标计算库 (Indicators)](#6-17-种技术指标计算库-indicators)
7. [筹码分布算法 (CYQ)](#7-筹码分布算法-cyq)
8. [链式选股器 (Screener)](#8-链式选股器-screener)
9. [技术信号识别 (Signals)](#9-技术信号识别-signals)
10. [单标的量化回测引擎 (Backtester)](#10-单标的量化回测引擎-backtester)
11. [资金流向 (Money Flow)](#11-资金流向-money-flow)
12. [公募基金 (Fund)](#12-公募基金-fund)
13. [行业与概念板块 (Plate)](#13-行业与概念板块-plate)
14. [智能代码搜索 (Search)](#14-智能代码搜索-search)
15. [ETF 期权与期货 (Options & Futures)](#15-etf-期权与期货-options--futures)
16. [高级配置、本地缓存与多源降级 (Config & Cache)](#16-高级配置本地缓存与多源降级-config--cache)
17. [最佳实践与常见问题 (FAQ)](#17-最佳实践与常见问题-faq)
18. [Spring Boot Starter、动态订阅与股价变动事件 (Spring Boot Integration)](#18-spring-boot-starter动态订阅与股价变动事件-spring-boot-integration)

---

## 1. 项目引入与构建

### 1.1 本地打包与安装
在 `stock-sdk-java` 根目录下执行：
```bash
mvn clean install
```
即可将 SDK 安装到本机的 Maven 本地仓库（`~/.m2/repository`）。

### 1.2 Maven 引入

#### 选项 A：Spring Boot 项目（推荐，自动装配 + 动态订阅 + 变动 Event）
```xml
<dependency>
    <groupId>io.github.lmabbe</groupId>
    <artifactId>stock-sdk-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

#### 选项 B：纯 Java 项目（无 Spring 强依赖）
```xml
<dependency>
    <groupId>io.github.lmabbe</groupId>
    <artifactId>stock-sdk-core</artifactId>
    <version>2.0.0</version>
</dependency>
```

### 1.3 Gradle 引入
```groovy
implementation 'io.github.lmabbe:stock-sdk-spring-boot-starter:2.0.0'
// 或纯 Java: implementation 'io.github.lmabbe:stock-sdk-core:2.0.0'
```

---

## 2. 快速起步 (Quick Start)

### 2.1 获取默认单例
SDK 内部提供了线程安全的单例入口，大多数场景直接使用单例即可：

```java
import io.github.stock.sdk.StockSDK;
import io.github.stock.sdk.model.Quote;

public class Main {
    public static void main(String[] args) {
        StockSDK sdk = StockSDK.getDefault();

        // 查一只股票的实时行情
        Quote quote = sdk.quotes.cn("sh600519");
        System.out.printf("股票名称: %s, 当前最新价: %s, 今日涨跌幅: %s%%\n",
                quote.getName(), quote.getPrice(), quote.getChangePercent());
    }
}
```

### 2.2 自定义建造者模式
当需要配置网络超时、重试次数或降级策略时，使用 Builder：

```java
import io.github.stock.sdk.StockSDK;
import io.github.stock.sdk.config.SdkConfig;
import java.time.Duration;

StockSDK customSdk = StockSDK.builder()
    .config(SdkConfig.builder()
        .timeout(Duration.ofSeconds(12)) // 请求超时
        .maxRetries(3)                   // 失败重试次数
        .fallbackEnabled(true)           // 开启多源自动降级
        .userAgent("MyCustomTradingApp/1.0")
        .build())
    .build();
```

---

## 3. 统一代码解析规则 (Symbol Model)

SDK 内置强大的自适应代码识别器 `SymbolNormalizer`，允许开发者输入任意格式的证券代码：

```java
import io.github.stock.sdk.util.SymbolNormalizer;

// 以下写法均能正确识别为贵州茅台 (sh600519)
SymbolNormalizer.NormalizedSymbol s1 = SymbolNormalizer.normalize("600519");
SymbolNormalizer.NormalizedSymbol s2 = SymbolNormalizer.normalize("sh600519");
SymbolNormalizer.NormalizedSymbol s3 = SymbolNormalizer.normalize("600519.SH");
SymbolNormalizer.NormalizedSymbol s4 = SymbolNormalizer.normalize("SH600519");

System.out.println(s1.getUnified());        // "sh600519"
System.out.println(s1.getCode());           // "600519"
System.out.println(s1.getEastmoneySecId()); // "1.600519" (供东方财富接口)
System.out.println(s1.getMarket());         // Market.CN

// 深市识别
SymbolNormalizer.normalize("000001");    // sz000001 (平安银行)
SymbolNormalizer.normalize("300750");    // sz300750 (宁德时代)

// 港股识别 (自动补齐5位)
SymbolNormalizer.normalize("00700");     // hk00700 (腾讯控股)
SymbolNormalizer.normalize("hk700");     // hk00700

// 美股识别
SymbolNormalizer.normalize("AAPL");      // usAAPL (苹果公司)
SymbolNormalizer.normalize("usTSLA");    // usTSLA (特斯拉)
```

---

## 4. 实时行情与盘口 (Quotes)

提供详尽的现价、开高低收、涨跌幅、成交额、换手率、PE/PB、市值及五档买卖深度盘口。

```java
import io.github.stock.sdk.model.OrderBookLevel;
import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.model.SimpleQuote;
import java.util.List;
import java.util.concurrent.CompletableFuture;

// 1. 获取单只 A 股
Quote q = sdk.quotes.cn("600519");
System.out.println("今开: " + q.getOpen() + ", 昨收: " + q.getPreClose());
System.out.println("最高: " + q.getHigh() + ", 最低: " + q.getLow());
System.out.println("总成交量: " + q.getVolume() + " 手, 总成交额: " + q.getAmount() + " 元");
System.out.println("换手率: " + q.getTurnoverRate() + "%, 市盈率(PE): " + q.getPe());

// 打印五档盘口
List<OrderBookLevel> bids = q.getBids(); // 买一到买五
List<OrderBookLevel> asks = q.getAsks(); // 卖一到卖五
System.out.println("买一价: " + bids.get(0).getPrice() + " 买一量: " + bids.get(0).getVolume());
System.out.println("卖一价: " + asks.get(0).getPrice() + " 卖一量: " + asks.get(0).getVolume());

// 2. 批量并发获取
List<Quote> batchList = sdk.quotes.cn(List.of("600519", "000001", "300750", "688981"));

// 3. 港股与美股
Quote hk = sdk.quotes.hk("00700");
Quote us = sdk.quotes.us("AAPL");

// 4. 极简轻量行情（用于移动端列表或滚动看板，节省内存）
SimpleQuote simple = sdk.quotes.cnSimple("600519");
System.out.println(simple.getName() + ": " + simple.getPrice() + " (" + simple.getChangePercent() + "%)");

// 5. 异步非阻塞调用
CompletableFuture<List<Quote>> future = sdk.quotes.cnAsync(List.of("600519", "000001"));
future.thenAccept(quotes -> {
    quotes.forEach(item -> System.out.println("异步回调: " + item.getName()));
});
```

---

## 5. K 线行情与分时图 (Kline & Timeline)

### 5.1 历史日/周/月 K 线
```java
import io.github.stock.sdk.enums.AdjustType;
import io.github.stock.sdk.enums.Period;
import io.github.stock.sdk.model.KlineBar;
import java.util.List;

// 默认获取：日 K 线、前复权 (QFQ)、最新 500 根
List<KlineBar> dailyBars = sdk.kline.cn("600519");

// 自定义周期与复权：获取周 K 线、后复权 (HFQ)、最新 120 根
List<KlineBar> weeklyBars = sdk.kline.cn("600519", Period.WEEKLY, AdjustType.HFQ, 120);

for (KlineBar bar : dailyBars) {
    System.out.printf("[%s] 开: %s, 高: %s, 低: %s, 收: %s, 量: %d\n",
            bar.getDate(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume());
}
```

### 5.2 分钟级别 K 线
支持 `1`、`5`、`15`、`30`、`60` 分钟线：
```java
// 获取 15 分钟 K 线，最新 240 根
List<KlineBar> min15Bars = sdk.kline.cnMinute("600519", "15", 240);
```

### 5.3 当日分时图分钟走势 (Timeline)
用于绘制日内分时走势图与黄线均价线：
```java
import io.github.stock.sdk.model.TimelineData;
import io.github.stock.sdk.model.TimelineItem;

TimelineData timeline = sdk.kline.timeline("600519");
System.out.println("分时中轴基准价(昨日收盘): " + timeline.getPreClose());

for (TimelineItem item : timeline.getItems()) {
    System.out.printf("时间: %s | 当前价: %s | 黄线均价: %s | 涨跌: %s%% | 成交量: %d\n",
            item.getTime(), item.getPrice(), item.getAvgPrice(), item.getChangePercent(), item.getVolume());
}
```

### 5.4 自动装配技术指标 (withIndicators)
自动将 `MA5`、`MA10`、`MA20`、`MACD`、`BOLL`、`KDJ`、`RSI` 计算并附加到每根 K 线：
```java
import io.github.stock.sdk.model.EnrichedKline;

List<EnrichedKline> enriched = sdk.kline.withIndicators("600519");
EnrichedKline latest = enriched.get(enriched.size() - 1);

System.out.println("最新日期: " + latest.getDate());
System.out.println("MA5: " + latest.getIndicator("ma5"));
System.out.println("MACD DIF: " + latest.getIndicator("macd.dif"));
System.out.println("MACD DEA: " + latest.getIndicator("macd.dea"));
System.out.println("BOLL 上轨: " + latest.getIndicator("boll.upper"));
System.out.println("KDJ K值: " + latest.getIndicator("kdj.k"));
System.out.println("RSI14: " + latest.getIndicator("rsi14"));
```

---

## 6. 17 种技术指标计算库 (Indicators)

`Indicators` 是纯数学算法库，**无需网络调用**，可对任意外部价格数组或 `KlineBar` 进行本地毫秒级计算：

```java
import io.github.stock.sdk.indicators.*;

double[] closes = { 10.0, 11.2, 11.8, 12.5, 12.0, 13.5, 14.2, 14.0, 15.1, 16.0 };

// 1. 均线系列
double[] sma5 = Indicators.calcSMA(closes, 5); // 简单移动平均
double[] ema12 = Indicators.calcEMA(closes, 12); // 指数移动平均
double[] wma5 = Indicators.calcWMA(closes, 5); // 加权移动平均

// 2. MACD 指标 (快线12, 慢线26, 信号线9)
MacdResult macd = Indicators.calcMACD(closes, 12, 26, 9);
double[] dif = macd.getDif();
double[] dea = macd.getDea();
double[] hist = macd.getHistogram();

// 3. 布林带 BOLL (周期20, 标准差倍数2.0)
BollResult boll = Indicators.calcBOLL(closes, 20, 2.0);
double[] upper = boll.getUpper();
double[] middle = boll.getMiddle();
double[] lower = boll.getLower();

// 4. KDJ 随机指标 (周期9, M1=3, M2=3)
KdjResult kdj = Indicators.calcKDJ(dailyBars, 9, 3, 3);
double[] k = kdj.getK();
double[] d = kdj.getD();
double[] j = kdj.getJ();

// 5. RSI 相对强弱指标 (周期14)
double[] rsi = Indicators.calcRSI(closes, 14);

// 6. 威廉指标 WR (Williams %R)
double[] wr = Indicators.calcWR(dailyBars, 14);

// 7. 乖离率 BIAS (周期6)
double[] bias = Indicators.calcBIAS(closes, 6);

// 8. 顺势指标 CCI (周期20)
double[] cci = Indicators.calcCCI(dailyBars, 20);

// 9. 真实波幅 ATR (周期14)
double[] atr = Indicators.calcATR(dailyBars, 14);

// 10. 能量潮 OBV
double[] obv = Indicators.calcOBV(dailyBars);

// 11. 变动率指标 ROC (周期12)
double[] roc = Indicators.calcROC(closes, 12);

// 12. 动向指标 DMI / ADX
DmiResult dmi = Indicators.calcDMI(dailyBars, 14);

// 13. 抛物线转向指标 SAR
double[] sar = Indicators.calcSAR(dailyBars, 0.02, 0.2);

// 14. 肯特纳通道 KC (Keltner Channels)
KcResult kc = Indicators.calcKC(dailyBars, 20, 10, 2.0);
```

---

## 7. 筹码分布算法 (CYQ)

复刻东方财富历史换手率衰减模型，计算投资者的持仓成本分布、主力获利盘与集中度：

```java
import io.github.stock.sdk.model.ChipData;
import io.github.stock.sdk.model.ChipBin;

// 计算近 120 个交易日的筹码分布
ChipData chipData = sdk.chips.cn("600519", 120);

System.out.println("加权平均持仓成本: " + chipData.getAverageCost() + " 元");
System.out.printf("当前价位获利盘比例: %.2f%%\n", chipData.getProfitRatio().doubleValue() * 100);

System.out.println("70% 筹码成本区间: [" + chipData.getCostRange70Low() + " ~ " + chipData.getCostRange70High() + "]");
System.out.println("70% 筹码集中度: " + chipData.getConcentration70() + " (数值越小代表筹码越集中)");

System.out.println("90% 筹码成本区间: [" + chipData.getCostRange90Low() + " ~ " + chipData.getCostRange90High() + "]");
System.out.println("90% 筹码集中度: " + chipData.getConcentration90());

// 遍历 100 个等距价格分箱的直方图（用于前端 ECharts 画筹码峰）
for (ChipBin bin : chipData.getBins()) {
    System.out.printf("价格: %s | 筹码量: %.0f | 占比: %.2f%%\n",
            bin.getPrice(), bin.getVolume(), bin.getPercent() * 100);
}
```

---

## 8. 链式选股器 (Screener)

提供流畅链式语法，支持不可变数据集筛选、安全防 NPE、多字段排序与 Top 截取：

```java
import io.github.stock.sdk.screener.Screener;
import io.github.stock.sdk.model.Quote;
import java.util.List;

// 1. 获取全市场 A 股快照行情
List<Quote> allStocks = sdk.batch.cn();

// 2. 链式选股
List<Quote> picks = Screener.screen(allStocks)
    // 条件 1: 总市值大于等于 100 亿
    .where(q -> q.getTotalMarketCap() != null && q.getTotalMarketCap().doubleValue() >= 10_000_000_000.0)
    // 条件 2: 动态市盈率 PE 在 0 ~ 25 之间（估值合理）
    .where(q -> q.getPe() != null && q.getPe().doubleValue() > 0 && q.getPe().doubleValue() < 25.0)
    // 条件 3: 今日涨幅超过 3%
    .where(q -> q.getChangePercent() != null && q.getChangePercent().doubleValue() >= 3.0)
    // 条件 4: 今日成交额大于 2 亿（流动性充足）
    .where(q -> q.getAmount() != null && q.getAmount().doubleValue() >= 200_000_000.0)
    // 排序: 按成交额从大到小降序排列
    .sortBy(Quote::getAmount, "desc")
    // 截取前 10 只
    .top(10)
    .toList();

picks.forEach(stock -> {
    System.out.printf("命中标的: %s (%s) | 现价: %s | 涨幅: %s%% | PE: %s\n",
            stock.getName(), stock.getSymbol(), stock.getPrice(), stock.getChangePercent(), stock.getPe());
});
```

---

## 9. 技术信号识别 (Signals)

自动扫描时间序列中的典型买入、卖出与超买超卖预警信号：

```java
import io.github.stock.sdk.signals.Signals;
import io.github.stock.sdk.signals.SignalEvent;
import java.util.List;

List<KlineBar> klines = sdk.kline.cn("600519");

// 1. 扫描全部综合技术信号
List<SignalEvent> allSignals = Signals.scanAllSignals(klines);
for (SignalEvent s : allSignals) {
    System.out.printf("[%s] 信号: %s (%s) @ 触发价: %.2f | 说明: %s\n",
            s.getDate(), s.getSignalName(), s.getType(), s.getPrice(), s.getDescription());
}

// 2. 单独检测双均线交叉 (如 5日线与 20日线)
List<SignalEvent> maCrosses = Signals.detectMaCross(klines, 5, 20);

// 3. 单独检测 MACD 金叉/死叉
List<SignalEvent> macdCrosses = Signals.detectMacdCross(klines);

// 4. 单独检测 RSI 极值预警 (超买80, 超卖20)
List<SignalEvent> rsiSignals = Signals.detectRsiExtremes(klines, 14, 80.0, 20.0);
```

---

## 10. 单标的量化回测引擎 (Backtester)

提供零依赖、高速度的本地单标的回测，支持灵活策略回调与详尽报告生成：

```java
import io.github.stock.sdk.screener.Backtester;
import io.github.stock.sdk.indicators.Indicators;

List<KlineBar> klines = sdk.kline.cn("600519", Period.DAILY, AdjustType.QFQ, 500);

// 预先计算均线
double[] ma5 = Indicators.calcSMA(klines, 5);
double[] ma20 = Indicators.calcSMA(klines, 20);

// 定义双均线金叉做多、死叉平仓策略
Backtester.StrategyFunction dualMaStrategy = (bar, idx, series) -> {
    if (idx < 20) return Backtester.Action.HOLD;
    
    // 金叉：MA5 上穿 MA20 -> 买入
    if (ma5[idx - 1] <= ma20[idx - 1] && ma5[idx] > ma20[idx]) {
        return Backtester.Action.BUY;
    }
    // 死叉：MA5 下穿 MA20 -> 卖出平仓
    else if (ma5[idx - 1] >= ma20[idx - 1] && ma5[idx] < ma20[idx]) {
        return Backtester.Action.SELL;
    }
    return Backtester.Action.HOLD;
};

Backtester.BacktestOptions options = new Backtester.BacktestOptions(klines, dualMaStrategy);
options.setInitialCapital(100_000.0); // 初始本金 10 万元
options.setCommissionRate(0.0003);    // 万分之三手续费

Backtester.BacktestReport report = Backtester.run(options);

System.out.printf("初始资金: %.2f 元 -> 最终资产: %.2f 元\n", report.getInitialCapital(), report.getFinalValue());
System.out.printf("累计收益率: %.2f%%\n", report.getTotalReturnRate() * 100);
System.out.printf("年化收益率: %.2f%%\n", report.getAnnualizedReturnRate() * 100);
System.out.printf("最大回撤: %.2f%%\n", report.getMaxDrawdown() * 100);
System.out.printf("交易总笔数: %d 笔 | 盈利笔数: %d 笔 | 胜率: %.2f%%\n",
        report.getTotalTrades(), report.getWinningTrades(), report.getWinRate() * 100);
System.out.printf("盈亏比: %.2f\n", report.getProfitFactor());

// 查看每一笔成交流水记录
report.getTrades().forEach(trade -> {
    System.out.printf("买入: %s (%.2f元) -> 卖出: %s (%.2f元) | 单笔收益: %.2f%% | 净利润: %.2f元\n",
            trade.getBuyDate(), trade.getBuyPrice(), trade.getSellDate(), trade.getSellPrice(),
            trade.getReturnRate() * 100, trade.getProfit());
});
```

---

## 11. 资金流向 (Money Flow)

监控个股的主力（超大单、大单）与散户（中单、小单）资金博弈动向：

```java
import io.github.stock.sdk.model.MoneyFlow;

MoneyFlow flow = sdk.moneyflow.cn("600519");

System.out.printf("股票: %s (%s)\n", flow.getName(), flow.getSymbol());
System.out.printf("主力资金净流入: %.2f 万元 (占比: %.2f%%)\n",
        flow.getMainNetInflow().doubleValue() / 10000.0, flow.getMainNetInflowPercent().doubleValue());
System.out.printf("超大单净额: %.2f 万 | 大单净额: %.2f 万\n",
        flow.getSuperLargeNetInflow().doubleValue() / 10000.0, flow.getLargeNetInflow().doubleValue() / 10000.0);
System.out.printf("中单净额: %.2f 万 | 散户(小单)净额: %.2f 万\n",
        flow.getMediumNetInflow().doubleValue() / 10000.0, flow.getSmallNetInflow().doubleValue() / 10000.0);
```

---

## 12. 公募基金 (Fund)

支持获取开放式基金、ETF 的盘中实时估值与官方公布的历史净值：

```java
import io.github.stock.sdk.model.FundQuote;
import io.github.stock.sdk.model.FundNav;
import java.util.List;

// 1. 盘中实时估算净值
FundQuote fQuote = sdk.fund.realtime("000001"); // 华夏成长混合
System.out.printf("基金名称: %s | 上日净值: %s | 盘中估算净值: %s | 估算涨跌幅: %s%%\n",
        fQuote.getName(), fQuote.getNetWorth(), fQuote.getEstimateNetWorth(), fQuote.getEstimateChangePercent());

// 2. 基金最新公布净值
FundNav latestNav = sdk.fund.nav("000001");
System.out.printf("净值公布日期: %s | 单位净值: %s | 累计净值: %s | 日涨幅: %s%%\n",
        latestNav.getDate(), latestNav.getUnitNav(), latestNav.getCumulativeNav(), latestNav.getDailyGrowthRate());

// 3. 基金历史净值序列
List<FundNav> navHistory = sdk.fund.history("000001", 1, 20); // 第1页，每页20条
```

---

## 13. 行业与概念板块 (Plate)

获取 A 股行业、概念板块排行，以及板块内部的成分股列表：

```java
import io.github.stock.sdk.model.Plate;
import io.github.stock.sdk.model.Quote;
import java.util.List;

// 1. 获取全部行业板块行情
List<Plate> industries = sdk.plate.industry();
industries.stream().limit(5).forEach(p -> {
    System.out.printf("行业: %s (%s) | 指数: %s | 涨跌幅: %s%% | 领涨股: %s (%s%%)\n",
            p.getName(), p.getCode(), p.getPrice(), p.getChangePercent(),
            p.getLeadingStockName(), p.getLeadingStockChangePercent());
});

// 2. 获取全部概念板块行情 (如: 人工智能、半导体、光伏等)
List<Plate> concepts = sdk.plate.concept();

// 3. 获取指定板块内的成分股行情 (以贵金属板块 BK0420 为例)
List<Quote> members = sdk.plate.members("BK0420");
members.forEach(stock -> {
    System.out.printf("成分股: %s (%s) | 现价: %s | 涨跌幅: %s%%\n",
            stock.getName(), stock.getSymbol(), stock.getPrice(), stock.getChangePercent());
});
```

---

## 14. 智能代码搜索 (Search)

构建智能自动补全与搜索框：

```java
import io.github.stock.sdk.model.SearchResult;
import java.util.List;

// 支持中文、拼音首字母、纯数字代码
List<SearchResult> r1 = sdk.search.search("茅台");
List<SearchResult> r2 = sdk.search.search("GZMT");
List<SearchResult> r3 = sdk.search.search("600519");

r1.forEach(item -> {
    System.out.printf("匹配到: %s | 代码: %s | 规范Symbol: %s | 市场: %s | 类型: %s\n",
            item.getName(), item.getCode(), item.getSymbol(), item.getMarket(), item.getType());
});
```

---

## 15. ETF 期权与期货 (Options & Futures)

### 15.1 ETF 期权 T 型报价链
```java
import io.github.stock.sdk.model.options.OptionChain;

// 获取华夏上证 50ETF (510050) 的期权链
OptionChain chain = sdk.options.chain("510050");
System.out.println("期权标的: " + chain.getUnderlyingSymbol());
```

### 15.2 商品与股指期货行情
```java
import io.github.stock.sdk.model.futures.FutureQuote;
import java.util.List;

// 获取主力合约行情 (如 RB2405 螺纹钢、IF2403 沪深300股指)
List<FutureQuote> futures = sdk.futures.quotes(List.of("RB2405", "IF2403", "AU2406"));
```

---

## 16. 高级配置、本地缓存与多源降级 (Config & Cache)

SDK 内置了智能多源策略与自适应本地内存缓存：
- **实时行情**：默认以**腾讯财经**为主源（极速），若因断网、解析错误或被限流，SDK 自动降级至**新浪财经**备用源。
- **历史数据与板块**：默认以**东方财富**为主源。
- **内置原生内存缓存 (零外部依赖)**：默认对实时行情做 3 秒缓存（对齐交易所刷新频率），对 K 线做 5 分钟缓存，**即便业务代码写了高频死循环轮询，也会在内存层毫秒级拦截返回，彻底杜绝被封 IP 风险**。

### 16.1 自定义初始化配置与缓存时长

```java
import io.github.stock.sdk.StockSDK;
import io.github.stock.sdk.config.SdkConfig;
import io.github.stock.sdk.config.CacheConfig;
import java.time.Duration;

// 1. 自定义缓存配置
CacheConfig cacheConfig = CacheConfig.builder()
        .enabled(true)                          // 是否开启本地内存缓存（默认开启）
        .quoteTtl(Duration.ofSeconds(3))        // 实时行情缓存 3 秒（与交易所 3 秒切片一致）
        .klineTtl(Duration.ofMinutes(10))       // 历史 K 线缓存 10 分钟
        .timelineTtl(Duration.ofSeconds(30))    // 当日分时图缓存 30 秒
        .plateTtl(Duration.ofMinutes(30))       // 行业/概念板块列表缓存 30 分钟
        .fundTtl(Duration.ofMinutes(5))         // 基金数据缓存 5 分钟
        .maxEntries(20_000)                     // 内存最大条目容量（防内存溢出）
        .build();

// 2. 初始化全局 SDK
StockSDK sdk = StockSDK.builder()
        .config(SdkConfig.builder()
                .timeout(Duration.ofSeconds(15)) // 网络超时时间
                .maxRetries(3)                   // 网络抖动自动重试 3 次
                .fallbackEnabled(true)           // 开启多源自动降级
                .cacheConfig(cacheConfig)        // 装配缓存配置
                .userAgent("Custom-Finance-Client/2.0")
                .build())
        .build();
```

### 16.2 彻底关闭缓存（直连网络）
若某些极端实时场景需要完全关闭缓存：
```java
StockSDK noCacheSdk = StockSDK.builder()
        .config(SdkConfig.builder()
                .cacheConfig(CacheConfig.disabled()) // 完全禁用缓存
                .build())
        .build();
```

### 16.3 缓存运维与统计
```java
// 1. 手动清空所有内存缓存
sdk.clearCache();

// 2. 查看缓存命中情况与指标
System.out.println("当前缓存条目数: " + sdk.getCache().size());
System.out.println("缓存命中次数: " + sdk.getCache().getHitCount());
System.out.println("缓存未命中次数: " + sdk.getCache().getMissCount());
System.out.printf("缓存命中率: %.2f%%\n", sdk.getCache().getHitRate() * 100);
```

---

## 17. 最佳实践与常见问题 (FAQ)

### Q1: 在 Spring Boot 项目中如何优雅使用？
**A**: 推荐将其配置为 Spring 的 `@Bean`：
```java
@Configuration
public class StockSdkConfig {

    @Bean
    public StockSDK stockSDK() {
        return StockSDK.getDefault();
    }
}

@Service
public class StockMarketService {

    @Autowired
    private StockSDK stockSDK;

    public Quote getQuote(String symbol) {
        return stockSDK.quotes.cn(symbol);
    }
}
```

### Q2: 盘口返回的 GBK 乱码如何处理的？
**A**: SDK 内部已在 `HttpClientHelper` 中对不同数据源做了编码自适应。腾讯和新浪财经接口原生使用 `GBK` 字节流解析，东方财富接口采用 `UTF-8` 解析，输出的 Java 字符串与 `BigDecimal` 均为标准 Unicode，用户无需关心编码问题。

### Q3: 为什么数值字段（如价格、金额）均采用 `BigDecimal`？
**A**: 金融与证券业务对浮点数精度要求极其严苛，采用 `double` 计算容易产生精度丢失（如 `0.1 + 0.2 = 0.30000000000000004`）。SDK 模型层统一采用 `BigDecimal` 保障金额的绝对精确，仅在技术指标纯数学矩阵运算中提供 `double` 加速重载。

---

## 18. Spring Boot Starter、动态订阅与股价变动事件 (Spring Boot Integration)

为了与 Spring Boot 生态深度集成，项目提供了官方 Starter 模块：`stock-sdk-spring-boot-starter`。

### 18.1 引入 Starter 依赖
```xml
<dependency>
    <groupId>io.github.lmabbe</groupId>
    <artifactId>stock-sdk-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

### 18.2 在 `application.yml` 中配置参数
所有参数均支持 Spring Boot 配置文件自动补全与修改：

```yaml
stock:
  sdk:
    enabled: true                   # 是否启用 SDK (默认 true)
    timeout: 10s                    # 网络超时
    max-retries: 2                  # 失败重试次数
    fallback-enabled: true          # 是否启用多源自动降级
    proxy:                          # 网络代理（可选，支持大并发抓取与海外服务器部署）
      enabled: false                # 是否开启代理 (默认 false)
      type: HTTP                    # HTTP 或 SOCKS
      host: 127.0.0.1
      port: 7890
      username: ""                  # 可选用户名
      password: ""                  # 可选密码
    cache:
      enabled: true                 # 开启本地内存缓存
      quote-ttl: 3s                 # 实时行情缓存 3 秒
      kline-ttl: 5m                 # K线缓存 5 分钟
    subscription:
      enabled: true                 # 开启后台价格轮询与变动事件 (默认 true)
      trading-hours-guard: true     # 🌟 A股交易时段智能守护 (默认 true: 盘中 3s 监听，休市/周末自动休眠防封)
      inactive-interval: 60s        # 非交易时段降频间隔 (若设为 0 表示非交易时段完全暂停，开盘前唤醒)
      poll-interval: 3s             # 交易时段轮询扫描间隔 (默认 3 秒)
      price-threshold: 0            # 变动阈值：只要价格发生变动就发布事件
      symbols:                      # 初始订阅股票代码列表
        - "sh600519"
        - "sz000001"
        - "sz300750"
```

### 18.3 注入与动态管理订阅列表 (`StockSubscriptionService`)
在业务中随时注入并**动态添加、删除订阅股票**：

```java
import io.github.stock.sdk.spring.service.StockSubscriptionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

@RestController
@RequestMapping("/api/stock/subscription")
public class StockSubscriptionController {

    @Autowired
    private StockSubscriptionService subscriptionService;

    // 动态添加订阅
    @PostMapping("/add")
    public String subscribe(@RequestParam String symbol) {
        return subscriptionService.subscribe(symbol); // 支持 "600519", "sh600519"
    }

    // 动态取消订阅
    @PostMapping("/remove")
    public boolean unsubscribe(@RequestParam String symbol) {
        return subscriptionService.unsubscribe(symbol);
    }

    // 查询当前正在订阅的所有股票
    @GetMapping("/list")
    public Set<String> list() {
        return subscriptionService.getSubscribedSymbols();
    }
}
```

### 18.4 监听股价变动事件 (`StockPriceChangeEvent`)
无需编写轮询代码，只需声明标准的 Spring `@EventListener`，当被订阅的股票现价发生跳动时，自动接收事件：

```java
import io.github.stock.sdk.spring.event.StockPriceChangeEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
public class StockPriceAlertListener {

    private static final Logger log = LoggerFactory.getLogger(StockPriceAlertListener.class);

    /**
     * 接收股价变动事件
     */
    @EventListener
    public void onStockPriceChange(StockPriceChangeEvent event) {
        log.info("🔔 收到股价变动通知!");
        log.info("  股票: {} ({})", event.getName(), event.getSymbol());
        log.info("  原价: {} -> 最新价: {}", event.getOldPrice(), event.getNewPrice());
        log.info("  本次跳动差额: {}", event.getTickDiff());
        log.info("  今日累计涨跌幅: {}%", event.getTodayChangePercent());
        
        // 还可以直接从 event.getQuote() 获得完整的五档买卖盘口、成交量等
        log.info("  买一档: {} ({}手)", 
            event.getQuote().getBids().get(0).getPrice(),
            event.getQuote().getBids().get(0).getVolume());

        // 可以在此触发钉钉/企业微信群通知、WebSocket 前端推送、短信报警等业务逻辑
    }

    /**
     * 也支持异步处理（不阻塞调度线程）
     */
    @Async
    @EventListener
    public void onPriceChangeAsync(StockPriceChangeEvent event) {
        // 异步推送到消息队列或 WebSocket
    }
}
```

---

祝使用愉快！如遇问题欢迎查阅源码或提交反馈。
