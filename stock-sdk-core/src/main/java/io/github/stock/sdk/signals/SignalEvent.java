package io.github.stock.sdk.signals;

import java.io.Serializable;

/**
 * 识别出的技术指标交易信号事件
 */
public class SignalEvent implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Type {
        BUY,
        SELL,
        WARNING
    }

    private int index;
    private String date;
    private String signalName; // 例如 "MA_GOLDEN_CROSS", "MACD_GOLDEN_CROSS", "RSI_OVERSOLD"
    private Type type;
    private String description;
    private double price;

    public SignalEvent() {
    }

    public SignalEvent(int index, String date, String signalName, Type type, String description, double price) {
        this.index = index;
        this.date = date;
        this.signalName = signalName;
        this.type = type;
        this.description = description;
        this.price = price;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getSignalName() {
        return signalName;
    }

    public void setSignalName(String signalName) {
        this.signalName = signalName;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Signal{" + date + " [" + type + "] " + signalName + ": " + description + " @ " + price + "}";
    }
}
