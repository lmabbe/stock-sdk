package io.github.stock.sdk.config;

import java.time.Duration;

/**
 * SDK 本地内存缓存配置
 */
public class CacheConfig {

    /** 是否启用本地内存缓存，默认开启 */
    private boolean enabled = true;

    /** 实时行情缓存时长（交易所切片刷新间隔为3秒，默认 3 秒） */
    private Duration quoteTtl = Duration.ofSeconds(3);

    /** K 线历史数据缓存时长，默认 5 分钟 */
    private Duration klineTtl = Duration.ofMinutes(5);

    /** 当日分时走势图缓存时长，默认 30 秒 */
    private Duration timelineTtl = Duration.ofSeconds(30);

    /** 行业/概念板块列表缓存时长，默认 10 分钟 */
    private Duration plateTtl = Duration.ofMinutes(10);

    /** 基金数据缓存时长，默认 5 分钟 */
    private Duration fundTtl = Duration.ofMinutes(5);

    /** 缓存最大容量条目数，默认 10,000 */
    private int maxEntries = 10_000;

    public CacheConfig() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public static CacheConfig defaultConfig() {
        return new CacheConfig();
    }

    public static CacheConfig disabled() {
        CacheConfig config = new CacheConfig();
        config.setEnabled(false);
        return config;
    }

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

    public static class Builder {
        private final CacheConfig config = new CacheConfig();

        public Builder enabled(boolean enabled) {
            config.setEnabled(enabled);
            return this;
        }

        public Builder quoteTtl(Duration quoteTtl) {
            config.setQuoteTtl(quoteTtl);
            return this;
        }

        public Builder klineTtl(Duration klineTtl) {
            config.setKlineTtl(klineTtl);
            return this;
        }

        public Builder timelineTtl(Duration timelineTtl) {
            config.setTimelineTtl(timelineTtl);
            return this;
        }

        public Builder plateTtl(Duration plateTtl) {
            config.setPlateTtl(plateTtl);
            return this;
        }

        public Builder fundTtl(Duration fundTtl) {
            config.setFundTtl(fundTtl);
            return this;
        }

        public Builder maxEntries(int maxEntries) {
            config.setMaxEntries(maxEntries);
            return this;
        }

        public CacheConfig build() {
            return config;
        }
    }
}
