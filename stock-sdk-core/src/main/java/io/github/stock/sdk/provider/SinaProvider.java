package io.github.stock.sdk.provider;

import io.github.stock.sdk.enums.AdjustType;
import io.github.stock.sdk.enums.Period;
import io.github.stock.sdk.exception.StockException;
import io.github.stock.sdk.model.OrderBookLevel;
import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.model.SearchResult;
import io.github.stock.sdk.model.Plate;
import io.github.stock.sdk.model.FundQuote;
import io.github.stock.sdk.model.FundNav;
import io.github.stock.sdk.model.KlineBar;
import io.github.stock.sdk.util.HttpClientHelper;
import io.github.stock.sdk.util.SymbolNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 新浪财经数据源提供者（主要作为实时行情的降级备用源）
 */
public class SinaProvider implements DataProvider {

    private static final Logger log = LoggerFactory.getLogger(SinaProvider.class);
    private static final String BASE_URL = "https://hq.sinajs.cn/list=";

    private final HttpClientHelper httpHelper;

    public SinaProvider(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    @Override
    public String getName() {
        return "sina";
    }

    @Override
    public List<Quote> getQuotes(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }

        List<SymbolNormalizer.NormalizedSymbol> normalizedList = symbols.stream()
                .map(SymbolNormalizer::normalize)
                .collect(Collectors.toList());

        String queryParam = normalizedList.stream()
                .map(s -> {
                    if ("us".equalsIgnoreCase(s.getPrefix())) {
                        return "gb_" + s.getCode().toLowerCase();
                    } else if ("hk".equalsIgnoreCase(s.getPrefix())) {
                        return "rt_hk" + s.getCode();
                    } else {
                        return s.getUnified();
                    }
                })
                .collect(Collectors.joining(","));

        String url = BASE_URL + queryParam;
        Map<String, String> headers = Map.of("Referer", "https://finance.sina.com.cn");

        try {
            String response = httpHelper.get(url, headers, HttpClientHelper.GBK);
            return parseSinaQuotes(response, normalizedList);
        } catch (Exception e) {
            log.error("Failed to fetch quotes from Sina: {}", e.getMessage());
            throw new StockException("SINA_QUOTE_ERROR", "Failed to fetch quotes from Sina", e);
        }
    }

    private List<Quote> parseSinaQuotes(String response, List<SymbolNormalizer.NormalizedSymbol> normalizedList) {
        if (response == null || response.trim().isEmpty()) {
            return Collections.emptyList();
        }

        Map<String, SymbolNormalizer.NormalizedSymbol> symbolMap = normalizedList.stream()
                .collect(Collectors.toMap(s -> s.getCode().toLowerCase(), s -> s, (v1, v2) -> v1));

        List<Quote> result = new ArrayList<>();
        String[] lines = response.split(";");
        for (String line : lines) {
            String trimmed = line.trim();
            if (trimmed.isEmpty() || !trimmed.contains("=\"")) {
                continue;
            }
            try {
                int eqIdx = trimmed.indexOf("=\"");
                String leftKey = trimmed.substring(0, eqIdx); // var hq_str_sh600519
                String rawData = trimmed.substring(eqIdx + 2);
                if (rawData.endsWith("\"")) {
                    rawData = rawData.substring(0, rawData.length() - 1);
                }
                if (rawData.trim().isEmpty()) {
                    continue;
                }

                String codeKey = leftKey.replace("var hq_str_", "").replace("sh", "").replace("sz", "").replace("bj", "").replace("gb_", "").replace("rt_hk", "");
                SymbolNormalizer.NormalizedSymbol norm = symbolMap.get(codeKey.toLowerCase());

                String[] parts = rawData.split(",");
                if (parts.length >= 31) {
                    Quote q = new Quote();
                    q.setName(parts[0]);
                    q.setCode(norm != null ? norm.getCode() : codeKey);
                    q.setSymbol(norm != null ? norm.getUnified() : codeKey);
                    q.setMarket(norm != null ? norm.getMarket().getCode() : "cn");

                    q.setOpen(parseBigDecimal(parts[1]));
                    q.setPreClose(parseBigDecimal(parts[2]));
                    q.setPrice(parseBigDecimal(parts[3]));
                    q.setHigh(parseBigDecimal(parts[4]));
                    q.setLow(parseBigDecimal(parts[5]));
                    q.setVolume(parseLong(parts[8]));
                    q.setAmount(parseBigDecimal(parts[9]));

                    if (q.getPrice() != null && q.getPreClose() != null && q.getPreClose().compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal change = q.getPrice().subtract(q.getPreClose());
                        q.setChange(change);
                        BigDecimal changePercent = change.multiply(BigDecimal.valueOf(100)).divide(q.getPreClose(), 2, java.math.RoundingMode.HALF_UP);
                        q.setChangePercent(changePercent);
                    }

                    // 买五档
                    List<OrderBookLevel> bids = new ArrayList<>();
                    bids.add(new OrderBookLevel(parseBigDecimal(parts[11]), parseLong(parts[10])));
                    bids.add(new OrderBookLevel(parseBigDecimal(parts[13]), parseLong(parts[12])));
                    bids.add(new OrderBookLevel(parseBigDecimal(parts[15]), parseLong(parts[14])));
                    bids.add(new OrderBookLevel(parseBigDecimal(parts[17]), parseLong(parts[16])));
                    bids.add(new OrderBookLevel(parseBigDecimal(parts[19]), parseLong(parts[18])));
                    q.setBids(bids);

                    // 卖五档
                    List<OrderBookLevel> asks = new ArrayList<>();
                    asks.add(new OrderBookLevel(parseBigDecimal(parts[21]), parseLong(parts[20])));
                    asks.add(new OrderBookLevel(parseBigDecimal(parts[23]), parseLong(parts[22])));
                    asks.add(new OrderBookLevel(parseBigDecimal(parts[25]), parseLong(parts[24])));
                    asks.add(new OrderBookLevel(parseBigDecimal(parts[27]), parseLong(parts[26])));
                    asks.add(new OrderBookLevel(parseBigDecimal(parts[29]), parseLong(parts[28])));
                    q.setAsks(asks);

                    q.setTimestamp(parts[30] + " " + parts[31]);
                    result.add(q);
                }
            } catch (Exception ex) {
                log.warn("Error parsing Sina quote: {}", ex.getMessage());
            }
        }
        return result;
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

    @Override
    public List<KlineBar> getKlines(String symbol, Period period, AdjustType adjust, int limit) {
        throw new UnsupportedOperationException("Sina klines delegated");
    }

    @Override
    public List<SearchResult> search(String keyword) {
        throw new UnsupportedOperationException("Sina search delegated");
    }

    @Override
    public List<Quote> getMarketBatch() {
        throw new UnsupportedOperationException("Sina market batch delegated");
    }

    @Override
    public List<Plate> getPlates(int type) {
        throw new UnsupportedOperationException("Sina plates delegated");
    }

    @Override
    public List<Quote> getPlateMembers(String plateCode) {
        throw new UnsupportedOperationException("Sina plate members delegated");
    }

    @Override
    public io.github.stock.sdk.model.FundQuote getFundQuote(String fundCode) {
        throw new UnsupportedOperationException("Sina fund quote delegated");
    }

    @Override
    public List<io.github.stock.sdk.model.FundNav> getFundNavHistory(String fundCode, int page, int pageSize) {
        throw new UnsupportedOperationException("Sina fund nav delegated");
    }

    @Override
    public io.github.stock.sdk.model.TimelineData getTimeline(String symbol) {
        throw new UnsupportedOperationException("Sina timeline delegated to EastMoneyProvider");
    }

    @Override
    public io.github.stock.sdk.model.MoneyFlow getMoneyFlow(String symbol) {
        throw new UnsupportedOperationException("Sina money flow delegated to EastMoneyProvider");
    }

    @Override
    public io.github.stock.sdk.model.options.OptionChain getOptionChain(String underlyingSymbol, String expireMonth) {
        throw new UnsupportedOperationException("Sina option chain delegated");
    }

    @Override
    public List<io.github.stock.sdk.model.futures.FutureQuote> getFutures(List<String> symbols) {
        throw new UnsupportedOperationException("Sina futures delegated");
    }
}
