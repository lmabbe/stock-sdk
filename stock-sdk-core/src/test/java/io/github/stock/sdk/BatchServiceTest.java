package io.github.stock.sdk;

import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.provider.EastMoneyProvider;
import io.github.stock.sdk.provider.TencentProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

public class BatchServiceTest {

    @Test
    public void testBatchServiceStructure() {
        StockSDK sdk = StockSDK.getDefault();
        Assertions.assertNotNull(sdk.batch);
        List<Quote> cn = sdk.batch.cn();
        System.out.println(cn);
    }

    @Test
    public void testBatchByCodes() {
        StockSDK sdk = StockSDK.getDefault();
        List<Quote> quotes = sdk.batch.byCodes(Arrays.asList("sh600519", "sz000001"));
        Assertions.assertNotNull(quotes);
    }
}
