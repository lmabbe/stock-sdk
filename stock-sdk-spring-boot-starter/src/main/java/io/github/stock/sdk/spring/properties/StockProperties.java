package io.github.stock.sdk.spring.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Stock SDK Spring Boot 自动配置属性
 *
 * <pre>
 * stock:
 *   sdk:
 *     enabled: true
 *     timeout: 10s
 *     max-retries: 2
 *     fallback-enabled: true
 *     cache:
 *       enabled: true
 *       quote-ttl: 3s
 *       kline-ttl: 5m
 *     subscription:
 *       enabled: true
 *       poll-interval: 3s
 *       symbols:
 *         - "sh600519"
 *         - "sz000001"
 * </pre>
 */
@ConfigurationProperties(prefix = "stock.sdk")
public class StockProperties {

    /** 是否启用 Stock SDK，默认 true */
    private boolean enabled = true;

    /** 网络请求超时时间，默认 15 秒 */
    private Duration timeout = Duration.ofSeconds(15);

    /** 失败重试次数，默认 2 次 */
    private int maxRetries = 2;

    /** 是否启用备用源智能降级，默认开启 */
    private boolean fallbackEnabled = true;

    /** 自定义 User-Agent 请求头 */
    private String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) StockSDK-SpringBoot";

    /** 本地缓存配置 */
    private CacheProperties cache = new CacheProperties();

    /** 网络代理配置 (HTTP / SOCKS) */
    private ProxyProperties proxy = new ProxyProperties();

    /** 股票动态订阅与变动通知配置 */
    private SubscriptionProperties subscription = new SubscriptionProperties();

    public static class ProxyProperties {
        private boolean enabled = false;
        private String type = "HTTP"; // HTTP 或 SOCKS
        private String host;
        private int port = 8080;
        private String username;
        private String password;

        public boolean isEnabled() {
            return enabled && host != null && !host.trim().isEmpty() && port > 0;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class CacheProperties {
        /** 是否开启本地内存缓存，默认 true */
        private boolean enabled = true;
        /** 实时行情缓存时长，默认 3 秒（与交易所 3 秒切片对齐） */
        private Duration quoteTtl = Duration.ofSeconds(3);
        /** K 线历史数据缓存时长，默认 5 分钟 */
        private Duration klineTtl = Duration.ofMinutes(5);
        /** 当日分时图缓存时长，默认 30 秒 */
        private Duration timelineTtl = Duration.ofSeconds(30);
        /** 板块数据缓存时长，默认 10 分钟 */
        private Duration plateTtl = Duration.ofMinutes(10);
        /** 基金数据缓存时长，默认 5 分钟 */
        private Duration fundTtl = Duration.ofMinutes(5);
        /** 最大缓存条目数，默认 10,000 */
        private int maxEntries = 10_000;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public Duration getQuoteTtl() {
            return quoteTtl;
        }

        public void setQuoteTtl(Duration quoteTtl) {
            this.quoteTtl = quoteTtl;
        }

        public Duration getKlineTtl() {
            return klineTtl;
        }

        public void setKlineTtl(Duration klineTtl) {
            this.klineTtl = klineTtl;
        }

        public Duration getTimelineTtl() {
            return timelineTtl;
        }

        public void setTimelineTtl(Duration timelineTtl) {
            this.timelineTtl = timelineTtl;
        }

        public Duration getPlateTtl() {
            return plateTtl;
        }

        public void setPlateTtl(Duration plateTtl) {
            this.plateTtl = plateTtl;
        }

        public Duration getFundTtl() {
            return fundTtl;
        }

        public void setFundTtl(Duration fundTtl) {
            this.fundTtl = fundTtl;
        }

        public int getMaxEntries() {
            return maxEntries;
        }

        public void setMaxEntries(int maxEntries) {
            this.maxEntries = maxEntries;
        }
    }

    public static class SubscriptionProperties {
        /** 是否启用后台动态订阅轮询与事件发布，默认 true */
        private boolean enabled = true;
        /** 是否启用交易时段智能守护（非交易时段自动降频/休眠防封），默认 true */
        private boolean tradingHoursGuard = true;
        /** 非交易时段的降频轮询间隔，默认 60 秒（设为 0 表示完全暂停直至开盘前自动唤醒） */
        private Duration inactiveInterval = Duration.ofSeconds(60);
        /** 初始订阅的股票代码列表（如 sh600519, sz000001 等） */
        private List<String> symbols = new ArrayList<>();
        /** 价格监控轮询间隔，默认 3 秒（与交易所切片节奏对齐） */
        private Duration pollInterval = Duration.ofSeconds(3);
        /** 触发事件的价格最小变动阈值（绝对差值），默认 0（任何价格变动都触发） */
        private BigDecimal priceThreshold = BigDecimal.ZERO;

        public boolean isTradingHoursGuard() {
            return tradingHoursGuard;
        }

        public void setTradingHoursGuard(boolean tradingHoursGuard) {
            this.tradingHoursGuard = tradingHoursGuard;
        }

        public Duration getInactiveInterval() {
            return inactiveInterval;
        }

        public void setInactiveInterval(Duration inactiveInterval) {
            this.inactiveInterval = inactiveInterval;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<String> getSymbols() {
            return symbols;
        }

        public void setSymbols(List<String> symbols) {
            this.symbols = symbols;
        }

        public Duration getPollInterval() {
            return pollInterval;
        }

        public void setPollInterval(Duration pollInterval) {
            this.pollInterval = pollInterval;
        }

        public BigDecimal getPriceThreshold() {
            return priceThreshold;
        }

        public void setPriceThreshold(BigDecimal priceThreshold) {
            this.priceThreshold = priceThreshold;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        this.timeout = timeout;
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public boolean isFallbackEnabled() {
        return fallbackEnabled;
    }

    public void setFallbackEnabled(boolean fallbackEnabled) {
        this.fallbackEnabled = fallbackEnabled;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public CacheProperties getCache() {
        return cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    public SubscriptionProperties getSubscription() {
        return subscription;
    }

    public void setSubscription(SubscriptionProperties subscription) {
        this.subscription = subscription;
    }

    public ProxyProperties getProxy() {
        return proxy;
    }

    public void setProxy(ProxyProperties proxy) {
        this.proxy = proxy;
    }
}
