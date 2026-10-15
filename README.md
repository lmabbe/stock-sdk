# Stock SDK (Java 11+)

[![Java](https://img.shields.io/badge/Java-11%2B-orange.svg)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-Build-blue.svg)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

本项目是著名 Node.js 开源股票 SDK **[stock-sdk](https://github.com/chengzuopeng/stock-sdk)** 的 **Java 11 Maven 复刻版**。

旨在为 Java 后端、微服务、量化交易策略原型及 Spring Boot 项目提供一个**无需 Python、无需自有数据清洗服务、轻量且可被其它项目直接依赖引入的股票金融数据 SDK**。

---

## 🌟 核心特性

- **多市场全覆盖**：A 股（沪深京）、港股、美股实时行情与五档盘口、历史/分钟级 K 线。
- **命名空间架构（Namespace API）**：完全对齐 Node 版直观流畅的调用设计（`sdk.quotes.*`, `sdk.kline.*`, `sdk.fund.*`, `sdk.plate.*`, `sdk.chips.*`, `sdk.search.*`）。
- **统一代码规范模型（Unified Symbol Model）**：自适应容错解析（如 `600519`、`sh600519`、`600519.SH`、`00700`、`AAPL` 等）。
- **内置 17 种技术指标（纯算法引擎）**：MA、SMA、EMA、WMA、MACD、BOLL、KDJ、RSI、WR、BIAS、CCI、ATR、OBV、ROC、DMI、SAR、KC。
- **筹码分布算法（CYQ）**：基于历史换手率衰减模型，计算平均成本、获利盘比例、70%/90% 集中度与 100 价格分箱。
- **流畅链式选股器（Screener）**：支持多重断言筛选、多维度排序与 Top 截断。
- **交易信号识别与回测引擎（Backtester）**：识别均线金叉/死叉、MACD 交叉等信号，支持策略回调、资金曲线与胜率回测统计。
- **多源智能降级与重试**：整合东方财富、腾讯财经、新浪财经接口，主源异常自动降级切换。
- **Spring Boot 官方 Starter 支持**：支持 `application.yml` 参数修改、动态修改自选股订阅列表、股价跳动 Spring 事件通知。
- **A 股交易时段智能守护**：盘中高频监控，闭市、中午休市与周末自动休眠/降频，彻底断绝被封 IP 风险。
- **HTTP / SOCKS 代理支持**：支持配置网络代理与认证，满足海外云服务器或大规模分布式数据抓取场景。

---

## 📦 Maven 依赖引入

### 方式 1：Spring Boot 项目（推荐，自动装配 + 动态订阅 + 价格变动 Event）
```xml
<dependency>
    <groupId>io.github.lmabbe</groupId>
    <artifactId>stock-sdk-spring-boot-starter</artifactId>
    <version>2.0.0</version>
</dependency>
```

### 方式 2：纯 Java 原生项目（无 Spring 强依赖）
```xml
<dependency>
    <groupId>io.github.lmabbe</groupId>
    <artifactId>stock-sdk-core</artifactId>
    <version>2.0.0</version>
</dependency>
```

本地安装至本地 Maven 仓库：
```bash
cd stock-sdk-java
mvn clean install
```

---

## 🚀 快速上手 (Quick Start)

### 1. 初始化 SDK

```java
import io.github.stock.sdk.StockSDK;
import io.github.stock.sdk.config.SdkConfig;
import java.time.Duration;

// 方式 1：获取全局默认单例实例
StockSDK sdk = StockSDK.getDefault();

// 方式 2：通过 Builder 自定义配置（超时时间、重试次数、多源降级开关等）
StockSDK customSdk = StockSDK.builder()
    .config(SdkConfig.builder()
        .timeout(Duration.ofSeconds(10))
        .maxRetries(2)
        .fallbackEnabled(true)
        .build())
    .build();
```

---

### 2. 实时行情与五档盘口 (`sdk.quotes`)

```java
import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.model.SimpleQuote;
import java.util.List;

// 1. 获取单只 A 股行情（自动解析代码）
Quote quote = sdk.quotes.cn("sh600519");
System.out.println("名称: " + quote.getName());
System.out.println("最新价: " + quote.getPrice());
System.out.println("涨跌幅: " + quote.getChangePercent() + "%");
System.out.println("市盈率(PE): " + quote.getPe());
System.out.println("买一档: " + quote.getBids().get(0).getPrice() + " 量: " + quote.getBids().get(0).getVolume());

// 2. 批量查询
List<Quote> list = sdk.quotes.cn(List.of("600519", "000001", "300750"));

// 3. 极简轻量行情（适合首页滚动与长列表展示）
SimpleQuote simple = sdk.quotes.cnSimple("600519");

// 4. 港股与美股行情
Quote hkQuote = sdk.quotes.hk("00700");  // 腾讯控股
Quote usQuote = sdk.quotes.us("AAPL");   // 苹果
```

---

### 3. K 线与自动指标计算 (`sdk.kline`)

```java
import io.github.stock.sdk.enums.Period;
import io.github.stock.sdk.enums.AdjustType;
import io.github.stock.sdk.model.KlineBar;
import io.github.stock.sdk.model.EnrichedKline;
import java.util.List;

// 1. 获取日 K 线（默认前复权，最新 500 根）
List<KlineBar> dailyKlines = sdk.kline.cn("600519");

// 2. 自定义参数获取（周 K 线、后复权、100 根）
List<KlineBar> weeklyKlines = sdk.kline.cn("600519", Period.WEEKLY, AdjustType.HFQ, 100);

// 3. 分钟级别 K 线 (1 / 5 / 15 / 30 / 60 分钟)
List<KlineBar> minuteBars = sdk.kline.cnMinute("600519", "15", 120);

// 4. 当日分时图分钟走势（包含现价、黄线均线、成交量、成交额）
TimelineData timeline = sdk.kline.timeline("600519");
System.out.println("分时昨收基准: " + timeline.getPreClose());
timeline.getItems().forEach(item -> {
    System.out.println(item.getTime() + " 现价: " + item.getPrice() + " 均价: " + item.getAvgPrice());
});

// 5. 获取 K 线并自动装配核心技术指标 (MA5/10/20, MACD, BOLL, KDJ, RSI)
List<EnrichedKline> enriched = sdk.kline.withIndicators("600519");
for (EnrichedKline bar : enriched) {
    System.out.println(bar.getDate() + " 收盘: " + bar.getClose() 
        + ", MA5=" + bar.getIndicator("ma5")
        + ", MACD.DIF=" + bar.getIndicator("macd.dif")
        + ", KDJ.K=" + bar.getIndicator("kdj.k"));
}
```

---

### 4. 筹码分布算法 CYQ (`sdk.chips`)

```java
import io.github.stock.sdk.model.ChipData;

// 计算贵州茅台的持仓筹码分布指标
ChipData chips = sdk.chips.cn("600519");

System.out.println("平均成本价: " + chips.getAverageCost());
System.out.println("当前获利盘比例: " + chips.getProfitRatio().multiply(BigDecimal.valueOf(100)) + "%");
System.out.println("70% 筹码集中度: " + chips.getConcentration70());
System.out.println("70% 成本区间: [" + chips.getCostRange70Low() + " ~ " + chips.getCostRange70High() + "]");
System.out.println("90% 筹码集中度: " + chips.getConcentration90());
// 100 分箱价格直方图节点：
chips.getBins().forEach(bin -> {
    // bin.getPrice(), bin.getVolume(), bin.getPercent()
});
```

---

### 5. 批量与全市场行情 (`sdk.batch`)

100% 对齐 Node 原版架构，内置**腾讯高并发分片（500 只/批次）+ 东方财富智能双源降级**：

```java
// 1. 获取全市场 A 股批量行情（5000+ 只股票，并发拉取，毫秒级返回）
List<Quote> allMarket = sdk.batch.cn(); 
// 也可直接调用别名方法:
List<Quote> allMarketAlias = sdk.batch.getMarketBatch();

// 2. 批量并发获取指定代码列表（支持几百或几千只股票，自动按 500 一组分片并发）
List<String> myCodes = List.of("600519", "000001", "300750", "688981");
List<Quote> customQuotes = sdk.batch.byCodes(myCodes);

// 3. 自定义分片大小与并发数（batchSize=200, concurrency=5）
List<Quote> customQuotes2 = sdk.batch.byCodes(myCodes, 200, 5);

// 4. 获取全市场 A 股有效代码清单（约 5000+ 只，如 sh600519, sz000001 等）
List<String> codeList = sdk.batch.codes();
```

---

### 6. 链式选股器 (`Screener`)

```java
import io.github.stock.sdk.screener.Screener;
import io.github.stock.sdk.model.Quote;
import java.util.List;

// 获取全市场快照批量数据
List<Quote> allQuotes = sdk.batch.cn();

// 链式表达多条件筛选
List<Quote> picks = Screener.screen(allQuotes)
    // 过滤市值大于 100 亿
    .where(q -> q.getTotalMarketCap() != null && q.getTotalMarketCap().doubleValue() >= 10_000_000_000.0)
    // 过滤市盈率在 0 ~ 30 之间
    .where(q -> q.getPe() != null && q.getPe().doubleValue() > 0 && q.getPe().doubleValue() < 30)
    // 过滤今日涨跌幅 > 2%
    .where(q -> q.getChangePercent() != null && q.getChangePercent().doubleValue() > 2.0)
    // 按成交额降序排序
    .sortBy(Quote::getAmount, "desc")
    // 取前 20 只股票
    .top(20)
    .toList();
```

---

### 7. 技术指标纯计算引擎 (`Indicators`)

可脱离网络独立使用：

```java
import io.github.stock.sdk.indicators.Indicators;
import io.github.stock.sdk.indicators.MacdResult;
import io.github.stock.sdk.indicators.BollResult;
import io.github.stock.sdk.indicators.KdjResult;

double[] closes = { 10.0, 10.5, 11.2, 10.8, 12.0, ... };

// 计算 5 日均线
double[] ma5 = Indicators.calcSMA(closes, 5);

// 计算 MACD
MacdResult macd = Indicators.calcMACD(closes, 12, 26, 9);
double[] dif = macd.getDif();
double[] dea = macd.getDea();
double[] hist = macd.getHistogram();

// 计算布林带
BollResult boll = Indicators.calcBOLL(closes, 20, 2.0);

// 其它指标：calcRSI, calcWR, calcBIAS, calcCCI, calcATR, calcOBV, calcROC, calcDMI, calcSAR, calcKC
```

---

### 8. 交易信号与量化回测 (`Signals` & `Backtester`)

```java
import io.github.stock.sdk.signals.Signals;
import io.github.stock.sdk.signals.SignalEvent;
import io.github.stock.sdk.screener.Backtester;
import java.util.List;

List<KlineBar> klines = sdk.kline.cn("600519");

// 1. 扫描 K 线中的全部技术指标买卖点与警告
List<SignalEvent> signals = Signals.scanAllSignals(klines);
signals.forEach(s -> System.out.println(s.getDate() + " [" + s.getType() + "] " + s.getDescription()));

// 2. 运行单标的回测
Backtester.BacktestReport report = Backtester.run(new Backtester.BacktestOptions(
    klines,
    (bar, idx, series) -> {
        // 自定义双均线突破策略或任意量化逻辑
        if (idx > 20 && bar.closeVal() > 1500) {
            return Backtester.Action.BUY;
        } else if (bar.closeVal() < 1400) {
            return Backtester.Action.SELL;
        }
        return Backtester.Action.HOLD;
    }
));

System.out.println("初始资金: " + report.getInitialCapital());
System.out.println("最终资产: " + report.getFinalValue());
System.out.println("累计收益率: " + String.format("%.2f%%", report.getTotalReturnRate() * 100));
System.out.println("最大回撤: " + String.format("%.2f%%", report.getMaxDrawdown() * 100));
System.out.println("交易胜率: " + String.format("%.2f%%", report.getWinRate() * 100));
```

---

### 9. 公募基金、板块与搜索 (`fund`, `plate`, `search`)

```java
// 1. 基金实时估算净值
FundQuote fundQuote = sdk.fund.realtime("000001");
System.out.println("基金名称: " + fundQuote.getName() + ", 估算净值: " + fundQuote.getEstimateNetWorth());

// 2. 行业板块与概念板块排行
List<Plate> industries = sdk.plate.industry();
List<Plate> concepts = sdk.plate.concept();
// 查询贵金属板块 (BK0420) 的成分股
List<Quote> members = sdk.plate.members("BK0420");

// 3. 股票/基金/代码联想搜索
List<SearchResult> searchResults = sdk.search.search("茅台");
searchResults.forEach(r -> System.out.println(r.getSymbol() + " - " + r.getName() + " (" + r.getType() + ")"));
```

---

## 🏗️ 模块结构一览

```
stock-sdk-java/
├── pom.xml
└── src/
    ├── main/java/io/github/stock/sdk/
    │   ├── StockSDK.java               # 统一入口门面
    │   ├── config/                     # 全局配置 (SdkConfig)
    │   ├── enums/                      # 市场/周期/复权枚举 (Market, Period, AdjustType)
    │   ├── exception/                  # 异常体系 (StockException)
    │   ├── indicators/                 # 17种纯数学指标计算 (Indicators, MacdResult, BollResult...)
    │   ├── model/                      # 实体数据契约 (Quote, KlineBar, ChipData, FundQuote...)
    │   ├── provider/                   # 数据适配层 (TencentProvider, EastMoneyProvider, SinaProvider)
    │   ├── screener/                   # 链式选股器 (Screener) 与量化回测 (Backtester)
    │   ├── service/                    # 命名空间服务 (QuotesService, KlineService, ChipsService...)
    │   ├── signals/                    # 交易信号识别 (Signals, SignalEvent)
    │   └── util/                       # 筹码分布算法 (ChipsEngine)、代码解析 (SymbolNormalizer) 等
    └── test/java/io/github/stock/sdk/  # 全套单元测试与集成测试
```

---

## 📄 License

MIT License
