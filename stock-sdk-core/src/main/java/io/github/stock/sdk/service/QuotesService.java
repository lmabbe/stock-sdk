package io.github.stock.sdk.service;

import io.github.stock.sdk.config.SdkConfig;
import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.model.SimpleQuote;
import io.github.stock.sdk.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * 实时行情命名空间服务 (sdk.quotes.*)
 */
public class QuotesService {

    private static final Logger log = LoggerFactory.getLogger(QuotesService.class);

    private final DataProvider primaryProvider;
    private final DataProvider fallbackProvider;
    private final SdkConfig config;
    private final io.github.stock.sdk.cache.SdkCache cache;

    public QuotesService(DataProvider primaryProvider, DataProvider fallbackProvider, SdkConfig config, io.github.stock.sdk.cache.SdkCache cache) {
        this.primaryProvider = primaryProvider;
        this.fallbackProvider = fallbackProvider;
        this.config = config;
        this.cache = cache;
    }

    /**
     * 获取单个 A 股股票实时行情
     */
    public Quote cn(String symbol) {
        List<Quote> list = cn(Collections.singletonList(symbol));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 批量获取 A 股股票实时行情
     */
    public List<Quote> cn(List<String> symbols) {
        return fetchWithFallback(symbols);
    }

    /**
     * 获取单个港股实时行情
     */
    public Quote hk(String symbol) {
        List<Quote> list = hk(Collections.singletonList(symbol));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 批量获取港股实时行情
     */
    public List<Quote> hk(List<String> symbols) {
        return fetchWithFallback(symbols);
    }

    /**
     * 获取单个美股实时行情
     */
    public Quote us(String symbol) {
        List<Quote> list = us(Collections.singletonList(symbol));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 批量获取美股实时行情
     */
    public List<Quote> us(List<String> symbols) {
        return fetchWithFallback(symbols);
    }

    /**
     * 获取极简行情结构
     */
    public List<SimpleQuote> cnSimple(List<String> symbols) {
        List<Quote> quotes = cn(symbols);
        return quotes.stream()
                .map(q -> new SimpleQuote(
                        q.getSymbol(),
                        q.getName(),
                        q.getPrice(),
                        q.getChangePercent(),
                        q.getChange(),
                        q.getVolume(),
                        q.getAmount()
                ))
                .collect(Collectors.toList());
    }

    public SimpleQuote cnSimple(String symbol) {
        List<SimpleQuote> list = cnSimple(Collections.singletonList(symbol));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 异步批量获取行情
     */
    public CompletableFuture<List<Quote>> cnAsync(List<String> symbols) {
        return CompletableFuture.supplyAsync(() -> cn(symbols));
    }

    private List<Quote> fetchWithFallback(List<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }

        // 1. 如果启用了缓存，先从本地内存缓存查找
        boolean cacheEnabled = cache != null && config.getCacheConfig().isEnabled();
        Map<String, Quote> resultMap = new HashMap<>();
        List<String> needFetchSymbols = new ArrayList<>();

        if (cacheEnabled) {
            for (String sym : symbols) {
                String cacheKey = "quote:" + sym.toLowerCase();
                Quote cached = cache.get(cacheKey);
                if (cached != null) {
                    resultMap.put(sym, cached);
                } else {
                    needFetchSymbols.add(sym);
                }
            }
        } else {
            needFetchSymbols.addAll(symbols);
        }

        // 如果全部命中缓存，直接组装返回，完全零网络 IO！
        if (needFetchSymbols.isEmpty()) {
            return symbols.stream().map(resultMap::get).filter(Objects::nonNull).collect(Collectors.toList());
        }

        // 2. 仅对未命中的股票发起网络请求
        List<Quote> fetched;
        try {
            fetched = primaryProvider.getQuotes(needFetchSymbols);
        } catch (Exception e) {
            if (config.isFallbackEnabled() && fallbackProvider != null) {
                log.warn("Primary quotes provider failed, falling back to {}: {}", fallbackProvider.getName(), e.getMessage());
                try {
                    fetched = fallbackProvider.getQuotes(needFetchSymbols);
                } catch (Exception fe) {
                    log.error("Fallback quotes provider also failed: {}", fe.getMessage());
                    throw fe;
                }
            } else {
                throw e;
            }
        }

        // 3. 将新拉取的数据存入缓存
        if (cacheEnabled && fetched != null) {
            var ttl = config.getCacheConfig().getQuoteTtl();
            for (Quote q : fetched) {
                if (q != null && q.getSymbol() != null) {
                    cache.put("quote:" + q.getSymbol().toLowerCase(), q, ttl);
                    if (q.getCode() != null) {
                        cache.put("quote:" + q.getCode().toLowerCase(), q, ttl);
                    }
                    resultMap.put(q.getSymbol(), q);
                    resultMap.put(q.getCode(), q);
                }
            }
        } else if (fetched != null) {
            for (Quote q : fetched) {
                if (q != null) {
                    resultMap.put(q.getSymbol(), q);
                    resultMap.put(q.getCode(), q);
                }
            }
        }

        // 按入参原始顺序组装结果返回
        List<Quote> ordered = new ArrayList<>();
        for (String sym : symbols) {
            Quote q = resultMap.get(sym);
            if (q != null && !ordered.contains(q)) {
                ordered.add(q);
            }
        }
        return ordered;
    }
}
