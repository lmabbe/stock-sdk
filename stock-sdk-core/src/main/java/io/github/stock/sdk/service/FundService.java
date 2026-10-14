package io.github.stock.sdk.service;

import io.github.stock.sdk.model.FundNav;
import io.github.stock.sdk.model.FundQuote;
import io.github.stock.sdk.provider.DataProvider;

import java.util.Collections;
import java.util.List;

/**
 * 基金数据命名空间服务 (sdk.fund.*)
 */
public class FundService {

    private final DataProvider provider;

    public FundService(DataProvider provider) {
        this.provider = provider;
    }

    /**
     * 获取公募基金实时估算净值 (盘中实时估算)
     */
    public FundQuote realtime(String fundCode) {
        return provider.getFundQuote(fundCode);
    }

    /**
     * 获取公募基金最新公布的官方净值
     */
    public FundNav nav(String fundCode) {
        List<FundNav> list = history(fundCode, 1, 1);
        return list.isEmpty() ? null : list.get(0);
    }

    /**
     * 分页获取公募基金历史净值时间序列
     */
    public List<FundNav> history(String fundCode, int page, int pageSize) {
        return provider.getFundNavHistory(fundCode, page, pageSize);
    }

    public List<FundNav> history(String fundCode) {
        return history(fundCode, 1, 30);
    }
}
