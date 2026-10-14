package io.github.stock.sdk;

import io.github.stock.sdk.enums.Market;
import io.github.stock.sdk.util.SymbolNormalizer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SymbolNormalizerTest {

    @Test
    public void testNormalizeSymbols() {
        // A 股沪市
        var s1 = SymbolNormalizer.normalize("600519");
        Assertions.assertEquals("sh600519", s1.getUnified());
        Assertions.assertEquals("1.600519", s1.getEastmoneySecId());
        Assertions.assertEquals(Market.CN, s1.getMarket());

        var s2 = SymbolNormalizer.normalize("sh600519");
        Assertions.assertEquals("sh600519", s2.getUnified());

        var s3 = SymbolNormalizer.normalize("600519.SH");
        Assertions.assertEquals("sh600519", s3.getUnified());

        // A 股深市
        var s4 = SymbolNormalizer.normalize("000001");
        Assertions.assertEquals("sz000001", s4.getUnified());
        Assertions.assertEquals("0.000001", s4.getEastmoneySecId());

        var s5 = SymbolNormalizer.normalize("300750"); // 宁德时代
        Assertions.assertEquals("sz300750", s5.getUnified());

        // 港股
        var s6 = SymbolNormalizer.normalize("00700");
        Assertions.assertEquals("hk00700", s6.getUnified());
        Assertions.assertEquals("116.00700", s6.getEastmoneySecId());
        Assertions.assertEquals(Market.HK, s6.getMarket());

        var s7 = SymbolNormalizer.normalize("hk700");
        Assertions.assertEquals("hk00700", s7.getUnified());

        // 美股
        var s8 = SymbolNormalizer.normalize("AAPL");
        Assertions.assertEquals("usAAPL", s8.getUnified());
        Assertions.assertEquals("105.AAPL", s8.getEastmoneySecId());
        Assertions.assertEquals(Market.US, s8.getMarket());

        var s9 = SymbolNormalizer.normalize("usTSLA");
        Assertions.assertEquals("usTSLA", s9.getUnified());
    }

    @Test
    public void testNormalizeSymbols1() {
        // A 股沪市
        var s1 = SymbolNormalizer.normalize("600519");
        System.out.println(s1);
    }
}
