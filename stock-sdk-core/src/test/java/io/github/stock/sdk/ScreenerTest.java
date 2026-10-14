package io.github.stock.sdk;

import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.screener.Screener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ScreenerTest {

    @Test
    public void testChainScreening() {
        List<Quote> quotes = new ArrayList<>();

        Quote q1 = createQuote("sh600519", "贵州茅台", 1680.0, 15.0, 2.5, 500_000_000.0);
        Quote q2 = createQuote("sz000001", "平安银行", 12.5, 5.0, -1.0, 100_000_000.0);
        Quote q3 = createQuote("sz300750", "宁德时代", 220.0, 25.0, 4.2, 800_000_000.0);
        Quote q4 = createQuote("sh601398", "工商银行", 5.6, 4.5, 0.5, 300_000_000.0);

        quotes.add(q1);
        quotes.add(q2);
        quotes.add(q3);
        quotes.add(q4);

        // 筛选：PE < 20 且 涨跌幅 > 0，按成交额降序，取 Top 1
        List<Quote> filtered = Screener.screen(quotes)
                .where(q -> q.getPe() != null && q.getPe().doubleValue() < 20.0)
                .where(q -> q.getChangePercent() != null && q.getChangePercent().doubleValue() > 0)
                .sortBy(Quote::getAmount, "desc")
                .top(1)
                .toList();

        Assertions.assertEquals(1, filtered.size());
        Assertions.assertEquals("贵州茅台", filtered.get(0).getName());
    }

    private Quote createQuote(String symbol, String name, double price, double pe, double changePct, double amount) {
        Quote q = new Quote();
        q.setSymbol(symbol);
        q.setName(name);
        q.setPrice(BigDecimal.valueOf(price));
        q.setPe(BigDecimal.valueOf(pe));
        q.setChangePercent(BigDecimal.valueOf(changePct));
        q.setAmount(BigDecimal.valueOf(amount));
        return q;
    }
}
