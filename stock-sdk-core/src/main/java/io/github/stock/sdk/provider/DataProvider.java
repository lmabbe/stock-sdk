package io.github.stock.sdk.provider;

import io.github.stock.sdk.enums.AdjustType;
import io.github.stock.sdk.enums.Period;
import io.github.stock.sdk.model.*;

import java.util.List;
import java.util.Map;

/**
 * 股票数据提供者接口
 */
public interface DataProvider {

    /**
     * 提供者名称
     */
    String getName();

    /**
     * 批量查询实时行情
     */
    List<Quote> getQuotes(List<String> symbols);

    /**
     * 查询历史/分钟 K 线
     */
    List<KlineBar> getKlines(String symbol, Period period, AdjustType adjust, int limit);

    /**
     * 证券/代码联想搜索
     */
    List<SearchResult> search(String keyword);

    /**
     * 获取全市场快照列表（批量）
     */
    List<Quote> getMarketBatch();

    /**
     * 查询行业/概念板块列表
     * @param type 2: 行业板块, 3: 概念板块
     */
    List<Plate> getPlates(int type);

    /**
     * 查询板块成分股
     */
    List<Quote> getPlateMembers(String plateCode);

    /**
     * 获取公募基金实时估算净值
     */
    FundQuote getFundQuote(String fundCode);

    /**
     * 获取公募基金历史公布净值序列
     */
    List<FundNav> getFundNavHistory(String fundCode, int page, int pageSize);

    /**
     * 获取股票当日分时图分钟走势数据
     */
    TimelineData getTimeline(String symbol);

    /**
     * 获取个股主力资金流向
     */
    MoneyFlow getMoneyFlow(String symbol);

    /**
     * 获取 ETF 期权 T 型报价链
     */
    io.github.stock.sdk.model.options.OptionChain getOptionChain(String underlyingSymbol, String expireMonth);

    /**
     * 获取主流商品/股指期货实时行情
     */
    List<io.github.stock.sdk.model.futures.FutureQuote> getFutures(List<String> symbols);
}
