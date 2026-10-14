package io.github.stock.sdk.screener;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * 链式轻量选股与数据筛选器（Zero-Dependency Client-Side Screener）
 * 对应 Node 版 stock-sdk/screener 模块
 *
 * <pre>
 * List&lt;Quote&gt; results = Screener.screen(quotes)
 *     .where(q -&gt; q.getPe() != null &amp;&amp; q.getPe().doubleValue() &gt; 0 &amp;&amp; q.getPe().doubleValue() &lt; 30)
 *     .where(q -&gt; q.getChangePercent() != null &amp;&amp; q.getChangePercent().doubleValue() &gt; 2.0)
 *     .sortBy(Quote::getAmount, "desc")
 *     .top(20)
 *     .toList();
 * </pre>
 */
public class Screener<T> {

    private final List<T> dataset;
    private final List<Predicate<T>> predicates = new ArrayList<>();
    private Comparator<T> comparator;
    private Integer limit;

    private Screener(Collection<T> items) {
        this.dataset = items != null ? new ArrayList<>(items) : new ArrayList<>();
    }

    /**
     * 创建一个选股器实例（不可变原数据，复制工作副本）
     */
    public static <T> Screener<T> screen(Collection<T> items) {
        return new Screener<>(items);
    }

    /**
     * 链式添加筛选条件断言
     */
    public Screener<T> where(Predicate<T> predicate) {
        if (predicate != null) {
            this.predicates.add(predicate);
        }
        return this;
    }

    /**
     * 根据提取的 Comparable 字段排序
     * @param extractor 属性抽取函数
     * @param direction "asc" 或 "desc"
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <U extends Comparable<? super U>> Screener<T> sortBy(Function<T, U> extractor, String direction) {
        boolean desc = "desc".equalsIgnoreCase(direction);
        Comparator<T> comp = (a, b) -> {
            U valA = extractor.apply(a);
            U valB = extractor.apply(b);
            if (valA == null && valB == null) return 0;
            if (valA == null) return desc ? 1 : -1;
            if (valB == null) return desc ? -1 : 1;
            return valA.compareTo(valB);
        };
        this.comparator = desc ? comp.reversed() : comp;
        return this;
    }

    public <U extends Comparable<? super U>> Screener<T> sortBy(Function<T, U> extractor) {
        return sortBy(extractor, "asc");
    }

    /**
     * 截取前 N 项结果
     */
    public Screener<T> top(int n) {
        this.limit = Math.max(0, n);
        return this;
    }

    /**
     * 执行链式筛选与排序并返回结果列表
     */
    public List<T> toList() {
        var stream = dataset.stream();
        for (Predicate<T> predicate : predicates) {
            stream = stream.filter(predicate);
        }
        if (comparator != null) {
            stream = stream.sorted(comparator);
        }
        if (limit != null) {
            stream = stream.limit(limit);
        }
        return stream.collect(Collectors.toList());
    }

    public List<T> get() {
        return toList();
    }
}
