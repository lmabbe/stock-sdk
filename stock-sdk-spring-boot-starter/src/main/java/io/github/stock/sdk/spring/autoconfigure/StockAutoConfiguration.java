package io.github.stock.sdk.spring.autoconfigure;

import io.github.stock.sdk.StockSDK;
import io.github.stock.sdk.config.CacheConfig;
import io.github.stock.sdk.config.SdkConfig;
import io.github.stock.sdk.spring.properties.StockProperties;
import io.github.stock.sdk.spring.service.StockSubscriptionScheduler;
import io.github.stock.sdk.spring.service.StockSubscriptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Stock SDK Spring Boot 自动装配配置类
 */
@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(StockSDK.class)
@EnableConfigurationProperties(StockProperties.class)
@ConditionalOnProperty(prefix = "stock.sdk", name = "enabled", havingValue = "true", matchIfMissing = true)
public class StockAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(StockAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public StockSDK stockSDK(StockProperties properties) {
        log.info("Initializing StockSDK with Spring Boot properties");

        // 1. 构建缓存配置
        StockProperties.CacheProperties cp = properties.getCache();
        CacheConfig cacheConfig = CacheConfig.builder()
                .enabled(cp.isEnabled())
                .quoteTtl(cp.getQuoteTtl())
                .klineTtl(cp.getKlineTtl())
                .timelineTtl(cp.getTimelineTtl())
                .plateTtl(cp.getPlateTtl())
                .fundTtl(cp.getFundTtl())
                .maxEntries(cp.getMaxEntries())
                .build();

        // 2. 构建代理配置
        StockProperties.ProxyProperties pp = properties.getProxy();
        io.github.stock.sdk.config.ProxyConfig proxyConfig = io.github.stock.sdk.config.ProxyConfig.builder()
                .enabled(pp.isEnabled())
                .type("SOCKS".equalsIgnoreCase(pp.getType()) ? io.github.stock.sdk.config.ProxyConfig.Type.SOCKS : io.github.stock.sdk.config.ProxyConfig.Type.HTTP)
                .host(pp.getHost())
                .port(pp.getPort())
                .username(pp.getUsername())
                .password(pp.getPassword())
                .build();

        // 3. 构建 SDK 核心配置
        SdkConfig sdkConfig = SdkConfig.builder()
                .timeout(properties.getTimeout())
                .maxRetries(properties.getMaxRetries())
                .fallbackEnabled(properties.isFallbackEnabled())
                .userAgent(properties.getUserAgent())
                .cacheConfig(cacheConfig)
                .proxyConfig(proxyConfig)
                .build();

        return StockSDK.builder()
                .config(sdkConfig)
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public StockSubscriptionService stockSubscriptionService(StockSDK stockSDK,
                                                             ApplicationEventPublisher eventPublisher,
                                                             StockProperties properties) {
        return new StockSubscriptionService(stockSDK, eventPublisher, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "stock.sdk.subscription", name = "enabled", havingValue = "true", matchIfMissing = true)
    public StockSubscriptionScheduler stockSubscriptionScheduler(StockSubscriptionService subscriptionService,
                                                                 StockProperties properties) {
        return new StockSubscriptionScheduler(subscriptionService, properties);
    }
}
