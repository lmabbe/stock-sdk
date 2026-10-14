package io.github.stock.sdk.model;

import java.util.HashMap;
import java.util.Map;

/**
 * 附加了技术指标计算结果的丰富 K 线
 */
public class EnrichedKline extends KlineBar {
    private static final long serialVersionUID = 1L;

    /** 动态附加指标映射，例如 "ma5" -> 1680.5, "macd.dif" -> 12.3 等 */
    private Map<String, Object> indicators = new HashMap<>();

    public EnrichedKline() {
        super();
    }

    public EnrichedKline(KlineBar bar) {
        super(bar.getDate(), bar.getOpen(), bar.getHigh(), bar.getLow(), bar.getClose(), bar.getVolume(), bar.getTurnover());
        setAmplitude(bar.getAmplitude());
        setChange(bar.getChange());
        setChangePercent(bar.getChangePercent());
        setTurnoverRate(bar.getTurnoverRate());
    }

    public Map<String, Object> getIndicators() {
        return indicators;
    }

    public void setIndicators(Map<String, Object> indicators) {
        this.indicators = indicators;
    }

    public void addIndicator(String key, Object value) {
        if (this.indicators == null) {
            this.indicators = new HashMap<>();
        }
        this.indicators.put(key, value);
    }

    public Object getIndicator(String key) {
        return this.indicators != null ? this.indicators.get(key) : null;
    }
}
