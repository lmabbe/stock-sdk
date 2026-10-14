package io.github.stock.sdk.service;

import io.github.stock.sdk.model.futures.FutureQuote;
import io.github.stock.sdk.provider.DataProvider;

import java.util.Collections;
import java.util.List;

/**
 * 期货命名空间服务 (sdk.futures.*)
 */
public class FuturesService {

    private final DataProvider provider;

    public FuturesService(DataProvider provider) {
        this.provider = provider;
    }

    /**
     * 获取单个期货合约实时行情
     * @param symbol 期货代码，如 "RB2405", "IF2403", "AU2406"
     */
    public FutureQuote quote(String symbol) {
        List<FutureQuote> list = quotes(Collections.singletonList(symbol));
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 批量获取期货合约行情
     */
    public List<FutureQuote> quotes(List<String> symbols) {
        return provider.getFutures(symbols);
    }
}
