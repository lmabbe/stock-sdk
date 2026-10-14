package io.github.stock.sdk.model;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 盘口档位信息（买一至买五、卖一至卖五）
 */
public class OrderBookLevel implements Serializable {
    private static final long serialVersionUID = 1L;

    private BigDecimal price;
    private Long volume;

    public OrderBookLevel() {
    }

    public OrderBookLevel(BigDecimal price, Long volume) {
        this.price = price;
        this.volume = volume;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Long getVolume() {
        return volume;
    }

    public void setVolume(Long volume) {
        this.volume = volume;
    }

    @Override
    public String toString() {
        return "{" + price + ", " + volume + "}";
    }
}
