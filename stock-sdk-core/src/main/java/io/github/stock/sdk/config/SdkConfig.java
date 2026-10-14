package io.github.stock.sdk.config;

import java.time.Duration;

/**
 * Stock SDK 全局配置
 */
public class SdkConfig {

    /** HTTP 连接和读取超时时间，默认 15 秒 */
    private Duration timeout = Duration.ofSeconds(15);

    /** 失败重试次数，默认 2 次 */
    private int maxRetries = 2;

    /** 是否启用多源降级（主源失败时自动降级到备用源），默认开启 */
    private boolean fallbackEnabled = true;

    /** 自定义 User-Agent */
    private String userAgent = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    /** 本地内存缓存配置 */
    private CacheConfig cacheConfig = CacheConfig.defaultConfig();

    /** 网络代理配置 */
    private ProxyConfig proxyConfig = ProxyConfig.disabled();

    public SdkConfig() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static SdkConfig defaultConfig() {
        return new SdkConfig();
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

    public CacheConfig getCacheConfig() {
        return cacheConfig;
    }

    public void setCacheConfig(CacheConfig cacheConfig) {
        this.cacheConfig = cacheConfig != null ? cacheConfig : CacheConfig.defaultConfig();
    }

    public ProxyConfig getProxyConfig() {
        return proxyConfig;
    }

    public void setProxyConfig(ProxyConfig proxyConfig) {
        this.proxyConfig = proxyConfig != null ? proxyConfig : ProxyConfig.disabled();
    }

    public static class Builder {
        private final SdkConfig config = new SdkConfig();

        public Builder timeout(Duration timeout) {
            config.setTimeout(timeout);
            return this;
        }

        public Builder maxRetries(int maxRetries) {
            config.setMaxRetries(maxRetries);
            return this;
        }

        public Builder fallbackEnabled(boolean fallbackEnabled) {
            config.setFallbackEnabled(fallbackEnabled);
            return this;
        }

        public Builder userAgent(String userAgent) {
            config.setUserAgent(userAgent);
            return this;
        }

        public Builder cacheConfig(CacheConfig cacheConfig) {
            config.setCacheConfig(cacheConfig);
            return this;
        }

        public Builder proxyConfig(ProxyConfig proxyConfig) {
            config.setProxyConfig(proxyConfig);
            return this;
        }

        public SdkConfig build() {
            return config;
        }
    }
}
