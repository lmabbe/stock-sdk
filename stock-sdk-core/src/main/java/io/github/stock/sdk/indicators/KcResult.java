package io.github.stock.sdk.indicators;

import java.io.Serializable;

public class KcResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private double[] upper;
    private double[] middle;
    private double[] lower;

    public KcResult() {
    }

    public KcResult(double[] upper, double[] middle, double[] lower) {
        this.upper = upper;
        this.middle = middle;
        this.lower = lower;
    }

    public double[] getUpper() {
        return upper;
    }

    public void setUpper(double[] upper) {
        this.upper = upper;
    }

    public double[] getMiddle() {
        return middle;
    }

    public void setMiddle(double[] middle) {
        this.middle = middle;
    }

    public double[] getLower() {
        return lower;
    }

    public void setLower(double[] lower) {
        this.lower = lower;
    }
}
