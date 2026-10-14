package io.github.stock.sdk.spring;

import io.github.stock.sdk.StockSDK;
import io.github.stock.sdk.spring.autoconfigure.StockAutoConfiguration;
import io.github.stock.sdk.spring.event.StockPriceChangeEvent;
import io.github.stock.sdk.spring.service.StockSubscriptionService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StockSpringBootStarterTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(StockAutoConfiguration.class));

    @Configuration
    static class TestEventListenerConfig {
        final List<StockPriceChangeEvent> receivedEvents = new ArrayList<>();

        @EventListener
        public void handlePriceChange(StockPriceChangeEvent event) {
            receivedEvents.add(event);
        }
    }

    @Test
    public void testAutoConfigurationAndSubscription() {
        contextRunner
                .withUserConfiguration(TestEventListenerConfig.class)
                .withPropertyValues(
                        "stock.sdk.timeout=10s",
                        "stock.sdk.proxy.enabled=true",
                        "stock.sdk.proxy.host=127.0.0.1",
                        "stock.sdk.proxy.port=7890",
                        "stock.sdk.cache.enabled=true",
                        "stock.sdk.cache.quote-ttl=2s",
                        "stock.sdk.subscription.symbols=sh600519,sz000001",
                        "stock.sdk.subscription.poll-interval=1s",
                        "stock.sdk.subscription.trading-hours-guard=true",
                        "stock.sdk.subscription.inactive-interval=30s"
                )
                .run(context -> {
                    // 1. 验证 Bean 正常自动装配
                    Assertions.assertTrue(context.containsBean("stockSDK"));
                    Assertions.assertTrue(context.containsBean("stockSubscriptionService"));
                    Assertions.assertTrue(context.containsBean("stockSubscriptionScheduler"));

                    StockSDK sdk = context.getBean(StockSDK.class);
                    Assertions.assertNotNull(sdk);

                    StockSubscriptionService subService = context.getBean(StockSubscriptionService.class);
                    Assertions.assertNotNull(subService);

                    // 2. 验证初始订阅列表是否已加载
                    Assertions.assertTrue(subService.isSubscribed("600519"));
                    Assertions.assertTrue(subService.isSubscribed("000001"));
                    Assertions.assertEquals(2, subService.getSubscribedSymbols().size());

                    // 3. 测试动态新增订阅
                    String added = subService.subscribe("300750"); // 宁德时代
                    Assertions.assertEquals("sz300750", added);
                    Assertions.assertTrue(subService.isSubscribed("300750"));
                    Assertions.assertEquals(3, subService.getSubscribedSymbols().size());

                    // 4. 测试动态退订
                    boolean removed = subService.unsubscribe("000001");
                    Assertions.assertTrue(removed);
                    Assertions.assertFalse(subService.isSubscribed("000001"));
                    Assertions.assertEquals(2, subService.getSubscribedSymbols().size());

                    // 5. 模拟价格变动事件发布与接收
                    TestEventListenerConfig listenerConfig = context.getBean(TestEventListenerConfig.class);
                    StockPriceChangeEvent mockEvent = new StockPriceChangeEvent(
                            this,
                            "sh600519",
                            "贵州茅台",
                            BigDecimal.valueOf(1680.0),
                            BigDecimal.valueOf(1690.0),
                            BigDecimal.valueOf(10.0),
                            BigDecimal.valueOf(20.0),
                            BigDecimal.valueOf(1.2),
                            null
                    );
                    context.publishEvent(mockEvent);

                    Assertions.assertEquals(1, listenerConfig.receivedEvents.size());
                    StockPriceChangeEvent received = listenerConfig.receivedEvents.get(0);
                    Assertions.assertEquals("sh600519", received.getSymbol());
                    Assertions.assertEquals(BigDecimal.valueOf(1680.0), received.getOldPrice());
                    Assertions.assertEquals(BigDecimal.valueOf(1690.0), received.getNewPrice());
                });
    }
}
