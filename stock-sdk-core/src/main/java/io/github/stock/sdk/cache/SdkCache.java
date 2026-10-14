package io.github.stock.sdk.cache;

import io.github.stock.sdk.config.CacheConfig;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

/**
 * SDK 轻量级本地内存缓存（零外部依赖，纯原生线程安全实现）
 */
public class SdkCache {

    private static class CacheEntry {
        final Object value;
        final long expireAtTimestamp;

        CacheEntry(Object value, long expireAtTimestamp) {
            this.value = value;
            this.expireAtTimestamp = expireAtTimestamp;
        }

        boolean isExpired(long now) {
            return now > expireAtTimestamp;
        }
    }

    private final CacheConfig config;
    private final ConcurrentHashMap<String, CacheEntry> store = new ConcurrentHashMap<>();
    private final AtomicLong hitCount = new AtomicLong();
    private final AtomicLong missCount = new AtomicLong();

    public SdkCache(CacheConfig config) {
        this.config = config != null ? config : CacheConfig.defaultConfig();
    }

    /**
     * 获取缓存数据，若已过期则返回 null
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        if (!config.isEnabled() || key == null) {
            return null;
        }

        CacheEntry entry = store.get(key);
        if (entry == null) {
            missCount.incrementAndGet();
            return null;
        }

        long now = System.currentTimeMillis();
        if (entry.isExpired(now)) {
            store.remove(key, entry);
            missCount.incrementAndGet();
            return null;
        }

        hitCount.incrementAndGet();
        return (T) entry.value;
    }

    /**
     * 写入缓存并指定 TTL
     */
    public void put(String key, Object value, Duration ttl) {
        if (!config.isEnabled() || key == null || value == null) {
            return;
        }

        long durationMs = ttl != null ? ttl.toMillis() : 3000L;
        long expireAt = System.currentTimeMillis() + Math.max(100L, durationMs);

        // 容量保护
        if (store.size() >= config.getMaxEntries()) {
            evictExpiredOrOldest();
        }

        store.put(key, new CacheEntry(value, expireAt));
    }

    /**
     * 获取缓存或计算
     */
    public <T> T getOrCompute(String key, Duration ttl, Supplier<T> supplier) {
        if (!config.isEnabled()) {
            return supplier.get();
        }

        T cached = get(key);
        if (cached != null) {
            return cached;
        }

        T computed = supplier.get();
        if (computed != null) {
            put(key, computed, ttl);
        }
        return computed;
    }

    /**
     * 清理所有过期数据，若仍超限则清理部分最早数据
     */
    private void evictExpiredOrOldest() {
        long now = System.currentTimeMillis();
        store.entrySet().removeIf(entry -> entry.getValue().isExpired(now));

        // 若依然超过最大容量，移除 1/5 批次防止无休止增长
        if (store.size() >= config.getMaxEntries()) {
            int removeCount = Math.max(1, config.getMaxEntries() / 5);
            var iterator = store.keySet().iterator();
            while (iterator.hasNext() && removeCount > 0) {
                iterator.next();
                iterator.remove();
                removeCount--;
            }
        }
    }

    /**
     * 手动清空所有缓存
     */
    public void clear() {
        store.clear();
    }

    /**
     * 移除特定前缀的所有缓存项
     */
    public void clearPrefix(String prefix) {
        if (prefix == null) return;
        store.keySet().removeIf(key -> key.startsWith(prefix));
    }

    public int size() {
        return store.size();
    }

    public long getHitCount() {
        return hitCount.get();
    }

    public long getMissCount() {
        return missCount.get();
    }

    public double getHitRate() {
        long total = hitCount.get() + missCount.get();
        return total > 0 ? (double) hitCount.get() / total : 0.0;
    }
}
