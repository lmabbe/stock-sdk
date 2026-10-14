package io.github.stock.sdk.service;

import io.github.stock.sdk.model.MoneyFlow;
import io.github.stock.sdk.provider.DataProvider;

/**
 * 资金流向命名空间服务 (sdk.moneyflow.*)
 */
public class MoneyFlowService {

    private final DataProvider provider;

    public MoneyFlowService(DataProvider provider) {
        this.provider = provider;
    }

    /**
     * 获取股票主力资金净流入及大单/中单/散户资金分布
     * @param symbol 股票代码，如 "600519"
     */
    public MoneyFlow cn(String symbol) {
        return provider.getMoneyFlow(symbol);
    }
}
