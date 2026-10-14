package io.github.stock.sdk.spring.service;

import io.github.stock.sdk.spring.properties.StockProperties;
import io.github.stock.sdk.util.MarketTradingHours;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 股票订阅后台自适应轮询调度器
 * 支持 A 股交易时段智能守护（开盘高频 3s 监听，闭市/周末智能休眠/降频）
 */
public class StockSubscriptionScheduler implements InitializingBean, DisposableBean {

    private static final Logger log = LoggerFactory.getLogger(StockSubscriptionScheduler.class);

    private final StockSubscriptionService subscriptionService;
    private final StockProperties properties;
    private ScheduledExecutorService executor;
    private volatile long lastRunTimestamp = 0L;

    public StockSubscriptionScheduler(StockSubscriptionService subscriptionService, StockProperties properties) {
        this.subscriptionService = subscriptionService;
        this.properties = properties;
    }

    @Override
    public void afterPropertiesSet() {
        if (!properties.getSubscription().isEnabled()) {
            log.info("Stock subscription polling is disabled by configuration");
            return;
        }

        Duration activeInterval = properties.getSubscription().getPollInterval();
        long activeIntervalMs = activeInterval != null ? Math.max(500L, activeInterval.toMillis()) : 3000L;

        Duration inactiveInterval = properties.getSubscription().getInactiveInterval();
        long inactiveIntervalMs = inactiveInterval != null ? inactiveInterval.toMillis() : 60_000L;

        boolean guardEnabled = properties.getSubscription().isTradingHoursGuard();

        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "stock-subscription-poller");
            t.setDaemon(true);
            return t;
        });

        // 统一心跳调度（每秒唤醒一次，内部根据交易时段自适应决定是否需要触发拉取）
        executor.scheduleWithFixedDelay(() -> {
            try {
                long now = System.currentTimeMillis();
                boolean isTrading = !guardEnabled || MarketTradingHours.isTradingTime();

                if (isTrading) {
                    // 交易时段：按 activeIntervalMs (如 3000ms) 触发
                    if (now - lastRunTimestamp >= activeIntervalMs) {
                        lastRunTimestamp = now;
                        subscriptionService.pollAndCheckPriceChanges();
                    }
                } else {
                    // 非交易时段（休市/夜间/周末）：
                    if (inactiveIntervalMs <= 0) {
                        // 设为 0 表示非交易时段完全暂停，不发起任何网络请求
                        return;
                    }
                    if (now - lastRunTimestamp >= inactiveIntervalMs) {
                        lastRunTimestamp = now;
                        log.debug("Market is closed. Running slow background check (interval: {} ms)", inactiveIntervalMs);
                        subscriptionService.pollAndCheckPriceChanges();
                    }
                }
            } catch (Throwable t) {
                log.warn("Error during scheduled stock polling: {}", t.getMessage());
            }
        }, 1000L, 1000L, TimeUnit.MILLISECONDS);

        log.info("Stock subscription scheduler started. Active interval: {} ms, Inactive interval: {} ms, TradingHoursGuard: {}",
                activeIntervalMs, inactiveIntervalMs, guardEnabled);
    }

    @Override
    public void destroy() {
        if (executor != null) {
            executor.shutdownNow();
            log.info("Stock subscription scheduler stopped");
        }
    }
}
