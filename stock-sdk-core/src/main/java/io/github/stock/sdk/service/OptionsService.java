package io.github.stock.sdk.service;

import io.github.stock.sdk.model.options.OptionChain;
import io.github.stock.sdk.provider.DataProvider;

/**
 * ETF 期权命名空间服务 (sdk.options.*)
 */
public class OptionsService {

    private final DataProvider provider;

    public OptionsService(DataProvider provider) {
        this.provider = provider;
    }

    /**
     * 获取 ETF 期权 T 型报价链
     * @param underlyingSymbol 标的代码，例如 "510050" (华夏上证50ETF) 或 "510300" (华泰柏瑞沪深300ETF)
     * @param expireMonth 到期月份，如 "2026-03" 或 null（默认当月）
     */
    public OptionChain chain(String underlyingSymbol, String expireMonth) {
        return provider.getOptionChain(underlyingSymbol, expireMonth);
    }

    public OptionChain chain(String underlyingSymbol) {
        return chain(underlyingSymbol, null);
    }
}
