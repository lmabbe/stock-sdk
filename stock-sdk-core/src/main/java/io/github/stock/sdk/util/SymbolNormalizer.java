package io.github.stock.sdk.util;

import io.github.stock.sdk.enums.Market;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 证券代码标准化解析器（Unified Symbol Model）
 * 支持容错解析 A股/港股/美股等各种格式
 */
public class SymbolNormalizer {

    private static final Pattern CODE_PATTERN = Pattern.compile("^(?:(sh|sz|bj|hk|us)[._]?)?([a-zA-Z0-9]+)(?:[._]?(sh|sz|bj|hk|us))?$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EASTMONEY_SECID_PATTERN = Pattern.compile("^([01]|105|106|107|116)\\.([a-zA-Z0-9]+)$");

    public static class NormalizedSymbol {
        private final String raw;
        private final String prefix;     // sh, sz, bj, hk, us
        private final String code;       // 600519, 00700, AAPL
        private final String unified;    // sh600519, hk00700, usAAPL
        private final String eastmoneySecId; // 1.600519, 0.000001, 116.00700, 105.AAPL
        private final Market market;

        public NormalizedSymbol(String raw, String prefix, String code, String unified, String eastmoneySecId, Market market) {
            this.raw = raw;
            this.prefix = prefix;
            this.code = code;
            this.unified = unified;
            this.eastmoneySecId = eastmoneySecId;
            this.market = market;
        }

        public String getRaw() {
            return raw;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getCode() {
            return code;
        }

        public String getUnified() {
            return unified;
        }

        public String getEastmoneySecId() {
            return eastmoneySecId;
        }

        public Market getMarket() {
            return market;
        }

        @Override
        public String toString() {
            return unified;
        }
    }

    /**
     * 将输入代码格式化为 NormalizedSymbol
     * 例如 "600519", "sh600519", "600519.SH", "00700", "AAPL"
     */
    public static NormalizedSymbol normalize(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Symbol code cannot be null or empty");
        }
        String clean = input.trim();

        // 检查是否本身就是东财 secid (如 1.600519)
        Matcher emMatcher = EASTMONEY_SECID_PATTERN.matcher(clean);
        if (emMatcher.matches()) {
            String mktId = emMatcher.group(1);
            String code = emMatcher.group(2);
            String prefix = "sh";
            Market market = Market.CN;
            if ("0".equals(mktId)) {
                prefix = "sz";
            } else if ("116".equals(mktId)) {
                prefix = "hk";
                market = Market.HK;
            } else if ("105".equals(mktId) || "106".equals(mktId) || "107".equals(mktId)) {
                prefix = "us";
                market = Market.US;
            }
            return new NormalizedSymbol(clean, prefix, code, prefix + code, clean, market);
        }

        Matcher matcher = CODE_PATTERN.matcher(clean);
        if (!matcher.matches()) {
            // 纯英文字符串默认为美股
            if (clean.matches("^[a-zA-Z]+$")) {
                String upper = clean.toUpperCase();
                return new NormalizedSymbol(clean, "us", upper, "us" + upper, "105." + upper, Market.US);
            }
            // 兜底返回原样
            return new NormalizedSymbol(clean, "", clean, clean, clean, Market.CN);
        }

        String pre = matcher.group(1);
        String code = matcher.group(2);
        String post = matcher.group(3);

        String marketPrefix = pre != null ? pre.toLowerCase() : (post != null ? post.toLowerCase() : null);

        // 如果未显式带市场前缀/后缀，根据代码特征推导
        if (marketPrefix == null) {
            marketPrefix = deduceMarketPrefix(code);
        }

        Market market = Market.CN;
        if ("hk".equalsIgnoreCase(marketPrefix)) {
            market = Market.HK;
            // 港股代码补全 5 位
            if (code.matches("^\\d+$") && code.length() < 5) {
                code = String.format("%05d", Integer.parseInt(code));
            }
        } else if ("us".equalsIgnoreCase(marketPrefix)) {
            market = Market.US;
            code = code.toUpperCase();
        }

        String unified = marketPrefix + code;
        String eastmoneySecId = toEastmoneySecId(marketPrefix, code);

        return new NormalizedSymbol(clean, marketPrefix, code, unified, eastmoneySecId, market);
    }

    private static String deduceMarketPrefix(String code) {
        if (code.matches("^[a-zA-Z]+$")) {
            return "us";
        }
        if (code.length() == 5 && code.startsWith("0")) {
            return "hk";
        }
        if (code.startsWith("60") || code.startsWith("68") || code.startsWith("90") || code.startsWith("11") || code.startsWith("51") || code.startsWith("000001")) {
            // 000001 特例：如不带前缀，上证指数通常写作 sh000001，但平安银行也是 000001 (sz000001)。默认000开头的6位按深市
            if ("000001".equals(code)) {
                return "sz"; // 平安银行
            }
            return "sh";
        }
        if (code.startsWith("00") || code.startsWith("30") || code.startsWith("15") || code.startsWith("16") || code.startsWith("39")) {
            return "sz";
        }
        if (code.startsWith("43") || code.startsWith("83") || code.startsWith("87") || code.startsWith("88") || code.startsWith("92")) {
            return "bj";
        }
        return "sh";
    }

    private static String toEastmoneySecId(String prefix, String code) {
        switch (prefix) {
            case "sh":
                return "1." + code;
            case "sz":
            case "bj":
                return "0." + code;
            case "hk":
                return "116." + code;
            case "us":
                return "105." + code;
            default:
                return "1." + code;
        }
    }
}
