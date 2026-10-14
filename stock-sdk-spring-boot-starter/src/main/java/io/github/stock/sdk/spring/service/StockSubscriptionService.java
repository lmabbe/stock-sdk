package io.github.stock.sdk.spring.service;

import io.github.stock.sdk.StockSDK;
import io.github.stock.sdk.model.Quote;
import io.github.stock.sdk.spring.event.StockPriceChangeEvent;
import io.github.stock.sdk.spring.properties.StockProperties;
import io.github.stock.sdk.util.SymbolNormalizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 股票动态订阅管理与变动事件检测服务
 */
public class StockSubscriptionService {

    private static final Logger log = LoggerFactory.getLogger(StockSubscriptionService.class);

    private final StockSDK stockSDK;
    private final ApplicationEventPublisher eventPublisher;
    private final StockProperties properties;

    /** 线程安全的动态订阅股票代码集合（存储规范化后的 symbol，如 sh600519） */
    private final Set<String> subscribedSymbols = ConcurrentHashMap.newKeySet();

    /** 记录每只股票上一次扫描到的价格，用于检测价格变动 */
    private final Map<String, BigDecimal> lastPriceMap = new ConcurrentHashMap<>();

    public StockSubscriptionService(StockSDK stockSDK, ApplicationEventPublisher eventPublisher, StockProperties properties) {
        this.stockSDK = stockSDK;
        this.eventPublisher = eventPublisher;
        this.properties = properties;

        // 加载配置文件中预设的初始订阅列表
        if (properties.getSubscription() != null && properties.getSubscription().getSymbols() != null) {
            subscribe(properties.getSubscription().getSymbols());
        }
    }

    /**
     * 动态订阅单只股票
     * @param rawSymbol 股票代码，如 "600519", "sh600519", "000001"
     * @return 规范化后的统一代码
     */
    public String subscribe(String rawSymbol) {
        if (rawSymbol == null || rawSymbol.trim().isEmpty()) {
            return null;
        }
        try {
            var norm = SymbolNormalizer.normalize(rawSymbol);
            String unified = norm.getUnified();
            subscribedSymbols.add(unified);
            log.info("Stock subscribed: {} (raw: {})", unified, rawSymbol);
            return unified;
        } catch (Exception e) {
            log.warn("Failed to subscribe symbol {}: {}", rawSymbol, e.getMessage());
            return null;
        }
    }

    /**
     * 批量动态订阅股票
     */
    public List<String> subscribe(Collection<String> symbols) {
        if (symbols == null || symbols.isEmpty()) {
            return Collections.emptyList();
        }
        List<String> added = new ArrayList<>();
        for (String sym : symbols) {
            String u = subscribe(sym);
            if (u != null) {
                added.add(u);
            }
        }
        return added;
    }

    /**
     * 取消订阅某只股票
     */
    public boolean unsubscribe(String rawSymbol) {
        if (rawSymbol == null || rawSymbol.trim().isEmpty()) {
            return false;
        }
        try {
            var norm = SymbolNormalizer.normalize(rawSymbol);
            String unified = norm.getUnified();
            boolean removed = subscribedSymbols.remove(unified);
            lastPriceMap.remove(unified);
            log.info("Stock unsubscribed: {}, removed: {}", unified, removed);
            return removed;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取当前所有已被订阅的股票代码列表
     */
    public Set<String> getSubscribedSymbols() {
        return Collections.unmodifiableSet(subscribedSymbols);
    }

    /**
     * 检查某只股票是否已被订阅
     */
    public boolean isSubscribed(String rawSymbol) {
        if (rawSymbol == null) return false;
        try {
            var norm = SymbolNormalizer.normalize(rawSymbol);
            return subscribedSymbols.contains(norm.getUnified());
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 清空所有订阅
     */
    public void clearSubscriptions() {
        subscribedSymbols.clear();
        lastPriceMap.clear();
        log.info("All stock subscriptions cleared");
    }

    /**
     * 获取某只股票上次记录的价格
     */
    public BigDecimal getLastKnownPrice(String symbol) {
        if (symbol == null) return null;
        try {
            var norm = SymbolNormalizer.normalize(symbol);
            return lastPriceMap.get(norm.getUnified());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 执行一轮价格扫描与变动检测（由后台调度器定时触发，也可业务手动触发）
     */
    public void pollAndCheckPriceChanges() {
        if (subscribedSymbols.isEmpty()) {
            return;
        }

        List<String> symbolsToQuery = new ArrayList<>(subscribedSymbols);
        try {
            // 一次 HTTP 批量查询所有订阅的股票
            List<Quote> quotes = stockSDK.quotes.cn(symbolsToQuery);
            if (quotes == null || quotes.isEmpty()) {
                return;
            }

            BigDecimal threshold = properties.getSubscription().getPriceThreshold();
            if (threshold == null) {
                threshold = BigDecimal.ZERO;
            }

            for (Quote quote : quotes) {
                if (quote == null || quote.getPrice() == null || quote.getSymbol() == null) {
                    continue;
                }

                String sym = quote.getSymbol().toLowerCase();
                BigDecimal currentPrice = quote.getPrice();
                BigDecimal previousPrice = lastPriceMap.get(sym);

                if (previousPrice == null) {
                    // 首次记录基准价格，不发送变动事件，仅记录当前价
                    lastPriceMap.put(sym, currentPrice);
                    continue;
                }

                // 计算价格变动绝对差值
                BigDecimal diff = currentPrice.subtract(previousPrice);
                if (diff.abs().compareTo(threshold) > 0) {
                    // 价格发生变化，触发 Spring ApplicationEvent 事件！
                    lastPriceMap.put(sym, currentPrice);

                    StockPriceChangeEvent event = new StockPriceChangeEvent(
                            this,
                            quote.getSymbol(),
                            quote.getName(),
                            previousPrice,
                            currentPrice,
                            diff,
                            quote.getChange(),
                            quote.getChangePercent(),
                            quote
                    );

                    log.debug("Stock price changed event published: {}", event);
                    eventPublisher.publishEvent(event);
                }
            }
        } catch (Exception e) {
            log.warn("Exception during stock price change polling: {}", e.getMessage());
        }
    }
}
