package io.github.stock.sdk.service;

import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.provider.DataProvider;
import io.github.stock.sdk.provider.TencentProvider;

import java.util.Collections;
import java.util.List;

/**
 * 批量数据命名空间服务 (sdk.batch.*)
 * 对齐 Node 版 stock-sdk 架构设计，支持全市场行情拉取与多代码并发批量分片
 */
public class BatchService {

    private final DataProvider eastMoneyProvider;
    private final TencentProvider tencentProvider;

    public BatchService(DataProvider eastMoneyProvider) {
        this(eastMoneyProvider, null);
    }

    public BatchService(DataProvider eastMoneyProvider, TencentProvider tencentProvider) {
        this.eastMoneyProvider = eastMoneyProvider;
        this.tencentProvider = tencentProvider;
    }

    /**
     * 获取全市场股票批量快照（A 股大盘排行榜与概览数据）
     * 采用双源高可用策略：优先尝试腾讯财经分片批量（速度快、并发高），异常时自动降级至东方财富
     */
    public List<Quote> cn() {
        if (tencentProvider != null) {
            try {
                List<Quote> quotes = tencentProvider.getMarketBatch();
                if (quotes != null && !quotes.isEmpty()) {
                    return quotes;
                }
            } catch (Exception ignored) {
            }
        }
        if (eastMoneyProvider != null) {
            return eastMoneyProvider.getMarketBatch();
        }
        return Collections.emptyList();
    }

    /**
     * 兼容直接调用 getMarketBatch() 的开发者
     */
    public List<Quote> getMarketBatch() {
        return cn();
    }

    /**
     * 批量查询指定股票代码列表的行情（自动分片并发）
     *
     * @param codes 股票代码列表（如 ["sh600519", "sz000001", ...]）
     */
    public List<Quote> byCodes(List<String> codes) {
        return byCodes(codes, 500, 5);
    }

    /**
     * 自定义分片与并发数批量查询指定股票代码列表
     *
     * @param codes       股票代码列表
     * @param batchSize   每批次股票数量（建议 200~500）
     * @param concurrency 最大并发数（建议 3~7）
     */
    public List<Quote> byCodes(List<String> codes, int batchSize, int concurrency) {
        if (codes == null || codes.isEmpty()) {
            return Collections.emptyList();
        }
        if (tencentProvider != null) {
            return tencentProvider.getQuotesInChunks(codes, batchSize, concurrency);
        }
        return eastMoneyProvider != null ? eastMoneyProvider.getQuotes(codes) : Collections.emptyList();
    }

    /**
     * 获取 A 股全市场代码列表 (如 ["sh600519", "sz000001", ...])
     */
    public List<String> codes() {
        if (tencentProvider != null) {
            return tencentProvider.getAShareCodeList();
        }
        return Collections.emptyList();
    }
}
