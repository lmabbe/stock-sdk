package io.github.stock.sdk.provider;

import io.github.stock.sdk.enums.AdjustType;
import io.github.stock.sdk.enums.Period;
import io.github.stock.sdk.exception.StockException;
import io.github.stock.sdk.model.KlineBar;
import io.github.stock.sdk.model.OrderBookLevel;
import io.github.stock.sdk.model.Plate;
import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.model.SearchResult;
import io.github.stock.sdk.model.SimpleQuote;
import io.github.stock.sdk.util.HttpClientHelper;
import io.github.stock.sdk.util.JsonUtils;
import io.github.stock.sdk.util.SymbolNormalizer;
import com.fasterxml.jackson.databind.JsonNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 腾讯财经行情数据源提供者
 * 主要用于实时行情批量获取与五档买卖盘口
 */
public class TencentProvider implements DataProvider {

    private static final Logger log = LoggerFactory.getLogger(TencentProvider.class);
    private static final String BASE_URL = "https://qt.gtimg.cn/q=";

    private final HttpClientHelper httpHelper;

    public TencentProvider(HttpClientHelper httpHelper) {
        this.httpHelper = httpHelper;
    }

    @Override
    public String getName() {
        return "tencent";
    }

    @Override
    public List<Quote> getQuotes(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }

        // 规范化代码并格式化为腾讯支持的前缀
        List<SymbolNormalizer.NormalizedSymbol> normalizedList = symbols.stream()
                .map(SymbolNormalizer::normalize)
                .collect(Collectors.toList());

        String queryParam = normalizedList.stream()
                .map(s -> {
                    if ("us".equalsIgnoreCase(s.getPrefix())) {
                        return "us" + s.getCode();
                    } else if ("hk".equalsIgnoreCase(s.getPrefix())) {
                        return "hk" + s.getCode();
                    } else {
                        return s.getUnified();
                    }
                })
                .collect(Collectors.joining(","));

        String url = BASE_URL + queryParam;
        try {
            // 腾讯财经返回 GBK 编码
            String response = httpHelper.get(url, null, HttpClientHelper.GBK);
            return parseTencentQuotes(response, normalizedList);
        } catch (Exception e) {
            log.error("Failed to fetch quotes from Tencent: {}", e.getMessage());
            throw new StockException("TENCENT_QUOTE_ERROR", "Failed to fetch quotes from Tencent", e);
        }
    }

    private List<Quote> parseTencentQuotes(String response, List<SymbolNormalizer.NormalizedSymbol> normalizedList) {
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
                // v_sh600519="1~贵州茅台~600519~...
                int eqIdx = trimmed.indexOf("=\"");
                String rawData = trimmed.substring(eqIdx + 2);
                if (rawData.endsWith("\"")) {
                    rawData = rawData.substring(0, rawData.length() - 1);
                }

                String[] parts = rawData.split("~");
                if (parts.length < 30) {
                    continue;
                }

                Quote q = new Quote();
                q.setName(parts[1]);
                q.setCode(parts[2]);

                SymbolNormalizer.NormalizedSymbol norm = symbolMap.get(parts[2].toLowerCase());
                if (norm != null) {
                    q.setSymbol(norm.getUnified());
                    q.setMarket(norm.getMarket().getCode());
                } else {
                    q.setSymbol(parts[2]);
                    q.setMarket("cn");
                }

                q.setPrice(parseBigDecimal(parts[3]));
                q.setPreClose(parseBigDecimal(parts[4]));
                q.setOpen(parseBigDecimal(parts[5]));
                q.setVolume(parseLong(parts[6]));

                // 五档买盘
                List<OrderBookLevel> bids = new ArrayList<>();
                bids.add(new OrderBookLevel(parseBigDecimal(parts[9]), parseLong(parts[10])));
                bids.add(new OrderBookLevel(parseBigDecimal(parts[11]), parseLong(parts[12])));
                bids.add(new OrderBookLevel(parseBigDecimal(parts[13]), parseLong(parts[14])));
                bids.add(new OrderBookLevel(parseBigDecimal(parts[15]), parseLong(parts[16])));
                bids.add(new OrderBookLevel(parseBigDecimal(parts[17]), parseLong(parts[18])));
                q.setBids(bids);

                // 五档卖盘
                List<OrderBookLevel> asks = new ArrayList<>();
                asks.add(new OrderBookLevel(parseBigDecimal(parts[19]), parseLong(parts[20])));
                asks.add(new OrderBookLevel(parseBigDecimal(parts[21]), parseLong(parts[22])));
                asks.add(new OrderBookLevel(parseBigDecimal(parts[23]), parseLong(parts[24])));
                asks.add(new OrderBookLevel(parseBigDecimal(parts[25]), parseLong(parts[26])));
                asks.add(new OrderBookLevel(parseBigDecimal(parts[27]), parseLong(parts[28])));
                q.setAsks(asks);

                if (parts.length > 30) {
                    q.setTimestamp(parts[30]);
                }
                if (parts.length > 31) {
                    q.setChange(parseBigDecimal(parts[31]));
                }
                if (parts.length > 32) {
                    q.setChangePercent(parseBigDecimal(parts[32]));
                }
                if (parts.length > 33) {
                    q.setHigh(parseBigDecimal(parts[33]));
                }
                if (parts.length > 34) {
                    q.setLow(parseBigDecimal(parts[34]));
                }
                if (parts.length > 37) {
                    // 成交额（万 -> 元）
                    BigDecimal amountTenThousand = parseBigDecimal(parts[37]);
                    if (amountTenThousand != null) {
                        q.setAmount(amountTenThousand.multiply(BigDecimal.valueOf(10000)));
                    }
                }
                if (parts.length > 38) {
                    q.setTurnoverRate(parseBigDecimal(parts[38]));
                }
                if (parts.length > 39) {
                    q.setPe(parseBigDecimal(parts[39]));
                }
                if (parts.length > 43) {
                    q.setAmplitude(parseBigDecimal(parts[43]));
                }
                if (parts.length > 44) {
                    BigDecimal floatCap = parseBigDecimal(parts[44]);
                    if (floatCap != null) {
                        q.setFloatMarketCap(floatCap.multiply(BigDecimal.valueOf(100000000))); // 亿 -> 元
                    }
                }
                if (parts.length > 45) {
                    BigDecimal totalCap = parseBigDecimal(parts[45]);
                    if (totalCap != null) {
                        q.setTotalMarketCap(totalCap.multiply(BigDecimal.valueOf(100000000)));
                    }
                }
                if (parts.length > 46) {
                    q.setPb(parseBigDecimal(parts[46]));
                }

                result.add(q);
            } catch (Exception ex) {
                log.warn("Error parsing quote row: {}, line: {}", ex.getMessage(), trimmed);
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
        throw new UnsupportedOperationException("TencentProvider does not support comprehensive historical klines, delegating to EastMoneyProvider");
    }

    @Override
    public List<SearchResult> search(String keyword) {
        throw new UnsupportedOperationException("TencentProvider does not support search, delegating to EastMoneyProvider");
    }

    public static final String A_SHARE_LIST_URL = "https://assets.linkdiary.cn/shares/zh_a_list.json";

    @Override
    public List<Quote> getMarketBatch() {
        List<String> codes = getAShareCodeList();
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        return getQuotesInChunks(codes, 500, 5);
    }

    /**
     * 从远程获取全市场 A 股代码列表（约 5000+ 股票代码，如 sh600519, sz000001, bj920000）
     */
    public List<String> getAShareCodeList() {
        try {
            String json = httpHelper.get(A_SHARE_LIST_URL, null);
            JsonNode root = JsonUtils.readTree(json);
            JsonNode listNode = root.path("list");
            if (listNode.isArray()) {
                List<String> list = new ArrayList<>();
                for (JsonNode item : listNode) {
                    list.add(item.asText());
                }
                return list;
            }
        } catch (Exception e) {
            log.warn("Failed to fetch A-share code list from remote: {}", e.getMessage());
        }
        return Collections.emptyList();
    }

    /**
     * 分片并发批量获取股票实时行情（支持几百或几千只股票分块批量查询）
     *
     * @param codes        股票代码列表
     * @param batchSize    每个分片包含的代码数（建议 200~500）
     * @param concurrency  最大并发请求数（建议 3~7）
     */
    public List<Quote> getQuotesInChunks(List<String> codes, int batchSize, int concurrency) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        int size = Math.max(1, Math.min(batchSize, 800));
        int threads = Math.max(1, Math.min(concurrency, 10));

        List<List<String>> chunks = new ArrayList<>();
        for (int i = 0; i < codes.size(); i += size) {
            chunks.add(codes.subList(i, Math.min(i + size, codes.size())));
        }

        ExecutorService executor = Executors.newFixedThreadPool(threads);
        try {
            List<CompletableFuture<List<Quote>>> futures = chunks.stream()
                    .map(chunk -> CompletableFuture.supplyAsync(() -> getQuotes(chunk), executor))
                    .collect(Collectors.toList());

            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            List<Quote> allQuotes = new ArrayList<>();
            for (CompletableFuture<List<Quote>> f : futures) {
                try {
                    allQuotes.addAll(f.get());
                } catch (Exception e) {
                    log.warn("Chunk fetch failed: {}", e.getMessage());
                }
            }
            return allQuotes;
        } finally {
            executor.shutdown();
        }
    }

    @Override
    public List<Plate> getPlates(int type) {
        throw new UnsupportedOperationException("TencentProvider plates delegated");
    }

    @Override
    public List<Quote> getPlateMembers(String plateCode) {
        throw new UnsupportedOperationException("TencentProvider plate members delegated");
    }

    @Override
    public io.github.stock.sdk.model.FundQuote getFundQuote(String fundCode) {
        throw new UnsupportedOperationException("TencentProvider fund quote delegated");
    }

    @Override
    public List<io.github.stock.sdk.model.FundNav> getFundNavHistory(String fundCode, int page, int pageSize) {
        throw new UnsupportedOperationException("TencentProvider fund nav delegated");
    }

    @Override
    public io.github.stock.sdk.model.TimelineData getTimeline(String symbol) {
        throw new UnsupportedOperationException("TencentProvider timeline delegated to EastMoneyProvider");
    }

    @Override
    public io.github.stock.sdk.model.MoneyFlow getMoneyFlow(String symbol) {
        throw new UnsupportedOperationException("TencentProvider money flow delegated to EastMoneyProvider");
    }

    @Override
    public io.github.stock.sdk.model.options.OptionChain getOptionChain(String underlyingSymbol, String expireMonth) {
        throw new UnsupportedOperationException("TencentProvider option chain delegated");
    }

    @Override
    public List<io.github.stock.sdk.model.futures.FutureQuote> getFutures(List<String> symbols) {
        throw new UnsupportedOperationException("TencentProvider futures delegated");
    }
}
