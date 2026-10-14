package io.github.stock.sdk.service;

import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.provider.DataProvider;

import java.util.List;

/**
 * 批量数据命名空间服务 (sdk.batch.*)
 */
public class BatchService {

    private final DataProvider provider;

    public BatchService(DataProvider provider) {
        this.provider = provider;
    }

    /**
     * 获取全市场股票批量快照（A 股大盘排行榜与概览数据）
     */
    public List<Quote> cn() {
        return provider.getMarketBatch();
    }
}
