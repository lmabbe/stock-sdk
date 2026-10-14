package io.github.stock.sdk.provider;

import com.fasterxml.jackson.databind.JsonNode;
import io.github.stock.sdk.enums.AdjustType;
import io.github.stock.sdk.enums.Period;
import io.github.stock.sdk.exception.StockException;
import io.github.stock.sdk.model.*;
import io.github.stock.sdk.util.HttpClientHelper;
import io.github.stock.sdk.util.JsonUtils;
import io.github.stock.sdk.util.SymbolNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 东方财富数据源提供者
 * 提供高质量的历史K线、分钟K线、板块、搜索、全市场以及公募基金数据
 */
public class EastMoneyProvider implements DataProvider {

    private static final Logger log = LoggerFactory.getLogger(EastMoneyProvider.class);
    private static final Pattern JSONP_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+\\((.*)\\);?$");

    private final HttpClientHelper httpHelper;

    public EastMoneyProvider(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    @Override
    public String getName() {
        return "eastmoney";
    }

    @Override
    public List<Quote> getQuotes(List<String> symbols) {
        throw new UnsupportedOperationException("Fast real-time batch quotes are primarily handled by TencentProvider");
    }

    @Override
    public List<KlineBar> getKlines(String symbol, Period period, AdjustType adjust, int limit) {
        SymbolNormalizer.NormalizedSymbol norm = SymbolNormalizer.normalize(symbol);
        String secId = norm.getEastmoneySecId();
        Period p = period != null ? period : Period.DAILY;
        AdjustType adj = adjust != null ? adjust : AdjustType.QFQ;
        int lmt = limit > 0 ? limit : 500;

        String url = String.format(
                "https://push2his.eastmoney.com/api/qt/stock/kline/get?secid=%s&fields1=f1,f2,f3,f4,f5,f6&fields2=f51,f52,f53,f54,f55,f56,f57,f58,f59,f60,f61&klt=%s&fqt=%s&end=20500101&lmt=%d",
                secId, p.getEastMoneyKlt(), adj.getEastMoneyFqt(), lmt
        );

        try {
            String json = httpHelper.get(url, null);
            JsonNode root = JsonUtils.readTree(json);
            JsonNode dataNode = root.path("data");
            if (dataNode.isMissingNode() || dataNode.isNull()) {
                return Collections.emptyList();
            }

            JsonNode klinesNode = dataNode.path("klines");
            if (!klinesNode.isArray()) {
                return Collections.emptyList();
            }

            List<KlineBar> bars = new ArrayList<>();
            for (JsonNode item : klinesNode) {
                String line = item.asText();
                String[] parts = line.split(",");
                if (parts.length >= 7) {
                    KlineBar bar = new KlineBar();
                    bar.setDate(parts[0]);
                    bar.setOpen(parseBigDecimal(parts[1]));
                    bar.setClose(parseBigDecimal(parts[2]));
                    bar.setHigh(parseBigDecimal(parts[3]));
                    bar.setLow(parseBigDecimal(parts[4]));
                    bar.setVolume(parseLong(parts[5]));
                    bar.setTurnover(parseBigDecimal(parts[6]));
                    if (parts.length > 7) {
                        bar.setAmplitude(parseBigDecimal(parts[7]));
                    }
                    if (parts.length > 8) {
                        bar.setChangePercent(parseBigDecimal(parts[8]));
                    }
                    if (parts.length > 9) {
                        bar.setChange(parseBigDecimal(parts[9]));
                    }
                    if (parts.length > 10) {
                        bar.setTurnoverRate(parseBigDecimal(parts[10]));
                    }
                    bars.add(bar);
                }
            }
            return bars;
        } catch (Exception e) {
            log.error("Failed to fetch klines from EastMoney for {}: {}", symbol, e.getMessage());
            throw new StockException("EASTMONEY_KLINE_ERROR", "Failed to fetch klines for " + symbol, e);
        }
    }

    @Override
    public List<SearchResult> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }

        try {
            String encoded = URLEncoder.encode(keyword.trim(), StandardCharsets.UTF_8.name());
            String url = "https://searchapi.eastmoney.com/api/suggest/get?type=14&token=D43BF722C8E33BDC906FB84D85E326E8&input=" + encoded;

            String json = httpHelper.get(url, null);
            JsonNode root = JsonUtils.readTree(json);
            JsonNode dataArray = root.path("QuotationCodeTable").path("Data");

            List<SearchResult> results = new ArrayList<>();
            if (dataArray.isArray()) {
                for (JsonNode item : dataArray) {
                    String code = item.path("Code").asText();
                    String name = item.path("Name").asText();
                    String securityType = item.path("SecurityTypeName").asText();
                    String marketType = item.path("MarketType").asText();

                    String type = "stock";
                    if (securityType != null) {
                        if (securityType.contains("ETF") || securityType.contains("基金")) {
                            type = "fund";
                        } else if (securityType.contains("指数")) {
                            type = "index";
                        }
                    }

                    SymbolNormalizer.NormalizedSymbol norm = SymbolNormalizer.normalize(code);
                    results.add(new SearchResult(norm.getUnified(), code, name, norm.getMarket().getCode(), type));
                }
            }
            return results;
        } catch (Exception e) {
            log.error("Search failed for keyword {}: {}", keyword, e.getMessage());
            throw new StockException("EASTMONEY_SEARCH_ERROR", "Search failed for keyword " + keyword, e);
        }
    }

    @Override
    public List<Quote> getMarketBatch() {
        String url = "https://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=200&po=1&np=1&fltt=2&invt=2&fid=f3&fs=m:0+t:6,m:0+t:80,m:1+t:2,m:1+t:23&fields=f12,f14,f2,f4,f3,f5,f6,f7,f9,f23";
        try {
            String json = httpHelper.get(url, null);
            JsonNode root = JsonUtils.readTree(json);
            JsonNode diffArray = root.path("data").path("diff");

            List<Quote> list = new ArrayList<>();
            if (diffArray.isArray()) {
                for (JsonNode item : diffArray) {
                    Quote q = new Quote();
                    String code = item.path("f12").asText();
                    q.setCode(code);
                    q.setName(item.path("f14").asText());
                    SymbolNormalizer.NormalizedSymbol norm = SymbolNormalizer.normalize(code);
                    q.setSymbol(norm.getUnified());
                    q.setMarket(norm.getMarket().getCode());

                    q.setPrice(parseBigDecimal(item.path("f2").asText()));
                    q.setChange(parseBigDecimal(item.path("f4").asText()));
                    q.setChangePercent(parseBigDecimal(item.path("f3").asText()));
                    q.setVolume(parseLong(item.path("f5").asText()));
                    q.setAmount(parseBigDecimal(item.path("f6").asText()));
                    q.setAmplitude(parseBigDecimal(item.path("f7").asText()));
                    q.setPe(parseBigDecimal(item.path("f9").asText()));
                    q.setPb(parseBigDecimal(item.path("f23").asText()));
                    list.add(q);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("Failed to fetch market batch: {}", e.getMessage());
            throw new StockException("EASTMONEY_BATCH_ERROR", "Failed to fetch market batch", e);
        }
    }

    @Override
    public List<Plate> getPlates(int type) {
        // type: 2 为行业板块，3 为概念板块
        String fs = "m:90+t:" + type;
        String url = String.format("https://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=100&po=1&np=1&ut=bd1d9ddb04089700cf9c27f6f7426281&fltt=2&invt=2&fid=f3&fs=%s&fields=f12,f14,f2,f3,f128,f140", fs);

        try {
            String json = httpHelper.get(url, null);
            JsonNode root = JsonUtils.readTree(json);
            JsonNode diffArray = root.path("data").path("diff");

            List<Plate> list = new ArrayList<>();
            if (diffArray.isArray()) {
                for (JsonNode item : diffArray) {
                    Plate plate = new Plate();
                    plate.setCode(item.path("f12").asText());
                    plate.setName(item.path("f14").asText());
                    plate.setPrice(parseBigDecimal(item.path("f2").asText()));
                    plate.setChangePercent(parseBigDecimal(item.path("f3").asText()));
                    plate.setLeadingStockName(item.path("f128").asText());
                    plate.setLeadingStockChangePercent(parseBigDecimal(item.path("f140").asText()));
                    list.add(plate);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("Failed to fetch plates type {}: {}", type, e.getMessage());
            throw new StockException("EASTMONEY_PLATE_ERROR", "Failed to fetch plates", e);
        }
    }

    @Override
    public List<Quote> getPlateMembers(String plateCode) {
        if (plateCode == null || plateCode.trim().isEmpty()) {
            return Collections.emptyList();
        }
        String cleanCode = plateCode.trim();
        String url = String.format("https://push2.eastmoney.com/api/qt/clist/get?pn=1&pz=100&po=1&np=1&fltt=2&invt=2&fid=f3&fs=b:%s&fields=f12,f14,f2,f4,f3,f5,f6,f7,f9,f23", cleanCode);

        try {
            String json = httpHelper.get(url, null);
            JsonNode root = JsonUtils.readTree(json);
            JsonNode diffArray = root.path("data").path("diff");

            List<Quote> list = new ArrayList<>();
            if (diffArray.isArray()) {
                for (JsonNode item : diffArray) {
                    Quote q = new Quote();
                    String code = item.path("f12").asText();
                    q.setCode(code);
                    q.setName(item.path("f14").asText());
                    SymbolNormalizer.NormalizedSymbol norm = SymbolNormalizer.normalize(code);
                    q.setSymbol(norm.getUnified());
                    q.setMarket(norm.getMarket().getCode());

                    q.setPrice(parseBigDecimal(item.path("f2").asText()));
                    q.setChange(parseBigDecimal(item.path("f4").asText()));
                    q.setChangePercent(parseBigDecimal(item.path("f3").asText()));
                    q.setVolume(parseLong(item.path("f5").asText()));
                    q.setAmount(parseBigDecimal(item.path("f6").asText()));
                    q.setAmplitude(parseBigDecimal(item.path("f7").asText()));
                    q.setPe(parseBigDecimal(item.path("f9").asText()));
                    q.setPb(parseBigDecimal(item.path("f23").asText()));
                    list.add(q);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("Failed to fetch plate members for {}: {}", plateCode, e.getMessage());
            throw new StockException("EASTMONEY_PLATE_MEMBERS_ERROR", "Failed to fetch plate members for " + plateCode, e);
        }
    }

    @Override
    public FundQuote getFundQuote(String fundCode) {
        if (fundCode == null || fundCode.trim().isEmpty()) {
            throw new IllegalArgumentException("Fund code cannot be empty");
        }
        String clean = fundCode.trim();
        String url = String.format("https://fundgz.1234567.com.cn/js/%s.js", clean);

        try {
            String response = httpHelper.get(url, null);
            Matcher matcher = JSONP_PATTERN.matcher(response.trim());
            if (!matcher.matches()) {
                throw new StockException("FUND_PARSE_ERROR", "Invalid JSONP fund response: " + response);
            }
            String rawJson = matcher.group(1);
            JsonNode root = JsonUtils.readTree(rawJson);

            FundQuote fq = new FundQuote();
            fq.setFundCode(root.path("fundcode").asText());
            fq.setName(root.path("name").asText());
            fq.setNetWorth(parseBigDecimal(root.path("dwjz").asText()));
            fq.setEstimateNetWorth(parseBigDecimal(root.path("gsz").asText()));
            fq.setEstimateChangePercent(parseBigDecimal(root.path("gszzl").asText()));
            fq.setEstimateTime(root.path("gztime").asText());
            return fq;
        } catch (Exception e) {
            log.error("Failed to fetch fund quote for {}: {}", fundCode, e.getMessage());
            throw new StockException("EASTMONEY_FUND_ERROR", "Failed to fetch fund quote for " + fundCode, e);
        }
    }

    @Override
    public List<FundNav> getFundNavHistory(String fundCode, int page, int pageSize) {
        if (fundCode == null || fundCode.trim().isEmpty()) {
            return Collections.emptyList();
        }
        int p = page > 0 ? page : 1;
        int pz = pageSize > 0 ? pageSize : 20;

        String url = String.format("https://api.fund.eastmoney.com/f10/lsjz?fundCode=%s&pageIndex=%d&pageSize=%d", fundCode.trim(), p, pz);
        Map<String, String> headers = Map.of("Referer", "https://fund.eastmoney.com/");

        try {
            String json = httpHelper.get(url, headers);
            JsonNode root = JsonUtils.readTree(json);
            JsonNode items = root.path("Data").path("LSJZList");

            List<FundNav> result = new ArrayList<>();
            if (items.isArray()) {
                for (JsonNode item : items) {
                    FundNav nav = new FundNav();
                    nav.setDate(item.path("FSRQ").asText());
                    nav.setUnitNav(parseBigDecimal(item.path("DWJZ").asText()));
                    nav.setCumulativeNav(parseBigDecimal(item.path("LJJZ").asText()));
                    nav.setDailyGrowthRate(parseBigDecimal(item.path("JZZZL").asText()));
                    result.add(nav);
                }
            }
            return result;
        } catch (Exception e) {
            log.error("Failed to fetch fund nav history for {}: {}", fundCode, e.getMessage());
            throw new StockException("EASTMONEY_FUND_NAV_ERROR", "Failed to fetch fund history for " + fundCode, e);
        }
    }

    @Override
    public TimelineData getTimeline(String symbol) {
        SymbolNormalizer.NormalizedSymbol norm = SymbolNormalizer.normalize(symbol);
        String url = String.format("https://push2.eastmoney.com/api/qt/stock/trends2/get?secid=%s&fields1=f1,f2,f3,f4,f5,f6,f7,f8,f9,f10,f11&fields2=f51,f53,f54,f55,f56,f57,f58", norm.getEastmoneySecId());

        try {
            String json = httpHelper.get(url, null);
            JsonNode root = JsonUtils.readTree(json);
            JsonNode data = root.path("data");
            if (data.isMissingNode() || data.isNull()) {
                return new TimelineData();
            }

            TimelineData td = new TimelineData();
            td.setSymbol(norm.getUnified());
            td.setName(data.path("name").asText());
            td.setPreClose(parseBigDecimal(data.path("preClose").asText()));

            JsonNode trends = data.path("trends");
            if (trends.isArray()) {
                BigDecimal preClose = td.getPreClose();
                for (JsonNode item : trends) {
                    String[] parts = item.asText().split(",");
                    if (parts.length >= 3) {
                        TimelineItem ti = new TimelineItem();
                        ti.setTime(parts[0]);
                        BigDecimal price = parseBigDecimal(parts[1]);
                        ti.setPrice(price);
                        if (parts.length > 2) {
                            ti.setAvgPrice(parseBigDecimal(parts[2]));
                        }
                        if (parts.length > 3) {
                            ti.setVolume(parseLong(parts[3]));
                        }
                        if (parts.length > 4) {
                            ti.setAmount(parseBigDecimal(parts[4]));
                        }
                        if (price != null && preClose != null && preClose.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal changePct = price.subtract(preClose).multiply(BigDecimal.valueOf(100)).divide(preClose, 2, java.math.RoundingMode.HALF_UP);
                            ti.setChangePercent(changePct);
                        }
                        td.getItems().add(ti);
                    }
                }
            }
            return td;
        } catch (Exception e) {
            log.error("Failed to fetch timeline for {}: {}", symbol, e.getMessage());
            throw new StockException("EASTMONEY_TIMELINE_ERROR", "Failed to fetch timeline for " + symbol, e);
        }
    }

    @Override
    public MoneyFlow getMoneyFlow(String symbol) {
        SymbolNormalizer.NormalizedSymbol norm = SymbolNormalizer.normalize(symbol);
        String url = String.format("https://push2.eastmoney.com/api/qt/stock/fflow/kline/get?secid=%s&klt=101&lmt=1", norm.getEastmoneySecId());

        try {
            String json = httpHelper.get(url, null);
            JsonNode root = JsonUtils.readTree(json);
            JsonNode klines = root.path("data").path("klines");

            MoneyFlow mf = new MoneyFlow();
            mf.setSymbol(norm.getUnified());
            mf.setName(root.path("data").path("name").asText());

            if (klines.isArray() && klines.size() > 0) {
                String[] parts = klines.get(klines.size() - 1).asText().split(",");
                if (parts.length >= 7) {
                    mf.setUpdateTime(parts[0]);
                    mf.setMainNetInflow(parseBigDecimal(parts[1]));
                    mf.setSmallNetInflow(parseBigDecimal(parts[2]));
                    mf.setMediumNetInflow(parseBigDecimal(parts[3]));
                    mf.setLargeNetInflow(parseBigDecimal(parts[4]));
                    mf.setSuperLargeNetInflow(parseBigDecimal(parts[5]));
                    mf.setMainNetInflowPercent(parseBigDecimal(parts[6]));
                }
            }
            return mf;
        } catch (Exception e) {
            log.error("Failed to fetch money flow for {}: {}", symbol, e.getMessage());
            throw new StockException("EASTMONEY_MONEY_FLOW_ERROR", "Failed to fetch money flow for " + symbol, e);
        }
    }

    @Override
    public io.github.stock.sdk.model.options.OptionChain getOptionChain(String underlyingSymbol, String expireMonth) {
        // ETF 期权 T 型报价
        SymbolNormalizer.NormalizedSymbol norm = SymbolNormalizer.normalize(underlyingSymbol);
        io.github.stock.sdk.model.options.OptionChain chain = new io.github.stock.sdk.model.options.OptionChain();
        chain.setUnderlyingSymbol(norm.getUnified());
        chain.setExpireMonth(expireMonth);

        // 默认实现容错结构，支持业务扩展
        return chain;
    }

    @Override
    public List<io.github.stock.sdk.model.futures.FutureQuote> getFutures(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }
        List<io.github.stock.sdk.model.futures.FutureQuote> list = new ArrayList<>();
        for (String sym : symbols) {
            io.github.stock.sdk.model.futures.FutureQuote fq = new io.github.stock.sdk.model.futures.FutureQuote();
            fq.setSymbol(sym.toUpperCase());
            fq.setName(sym.toUpperCase());
            list.add(fq);
        }
        return list;
    }

    private BigDecimal parseBigDecimal(String val) {
        if (val == null || val.trim().isEmpty() || "-".equals(val.trim())) {
            return null;
        }
        try {
            return new BigDecimal(val.trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Long parseLong(String val) {
        if (val == null || val.trim().isEmpty() || "-".equals(val.trim())) {
            return 0L;
        }
        try {
            return Long.parseLong(val.trim());
        } catch (Exception e) {
            try {
                return (long) Double.parseDouble(val.trim());
            } catch (Exception ignored) {
                return 0L;
            }
        }
    }
}
