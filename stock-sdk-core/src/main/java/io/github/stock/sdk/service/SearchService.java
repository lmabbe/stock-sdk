package io.github.stock.sdk.service;

import io.github.stock.sdk.model.SearchResult;
import io.github.stock.sdk.provider.DataProvider;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 证券搜索与自动补全命名空间服务 (sdk.search.*)
 */
public class SearchService {

    private final DataProvider provider;

    public SearchService(DataProvider provider) {
        this.provider = provider;
    }

    /**
     * 根据关键字（代码、中文名称、拼音首字母）搜索证券
     * @param keyword 搜索关键字，如 "茅台", "600519", "GZMT", "腾讯", "00700"
     */
    public List<SearchResult> search(String keyword) {
        return provider.search(keyword);
    }

    /**
     * 异步搜索
     */
    public CompletableFuture<List<SearchResult>> searchAsync(String keyword) {
        return CompletableFuture.supplyAsync(() -> search(keyword));
    }
}
