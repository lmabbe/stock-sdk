package io.github.stock.sdk;

import io.github.stock.sdk.cache.SdkCache;
import io.github.stock.sdk.config.CacheConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;

public class SdkCacheTest {

    @Test
    public void testBasicPutAndGet() {
        CacheConfig config = CacheConfig.builder()
                .enabled(true)
                .quoteTtl(Duration.ofSeconds(2))
                .build();

        SdkCache cache = new SdkCache(config);
        cache.put("quote:sh600519", "MAOTAI", Duration.ofSeconds(2));

        String val = cache.get("quote:sh600519");
        Assertions.assertEquals("MAOTAI", val);
        Assertions.assertEquals(1, cache.getHitCount());
    }

    @Test
    public void testTtlExpiration() throws InterruptedException {
        CacheConfig config = CacheConfig.builder().enabled(true).build();
        SdkCache cache = new SdkCache(config);

        // 写入 150 毫秒 TTL
        cache.put("kline:tmp", "KLINE_DATA", Duration.ofMillis(150));
        Assertions.assertEquals("KLINE_DATA", cache.get("kline:tmp"));

        // 等待 200 毫秒让其过期
        Thread.sleep(200);

        String expired = cache.get("kline:tmp");
        Assertions.assertNull(expired);
        Assertions.assertTrue(cache.getMissCount() >= 1);
    }

    @Test
    public void testCacheDisabled() {
        CacheConfig config = CacheConfig.disabled();
        SdkCache cache = new SdkCache(config);

        cache.put("key1", "val1", Duration.ofSeconds(10));
        Assertions.assertNull(cache.get("key1"));
    }

    @Test
    public void testClear() {
        CacheConfig config = CacheConfig.defaultConfig();
        SdkCache cache = new SdkCache(config);

        cache.put("quote:1", "A", Duration.ofSeconds(10));
        cache.put("quote:2", "B", Duration.ofSeconds(10));
        Assertions.assertEquals(2, cache.size());

        cache.clear();
        Assertions.assertEquals(0, cache.size());
        Assertions.assertNull(cache.get("quote:1"));
    }
}
