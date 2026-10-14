package io.github.stock.sdk.indicators;

import java.io.Serializable;
import java.util.Arrays;

public class MacdResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private double[] dif;
    private double[] dea;
    private double[] histogram;

    public MacdResult() {
    }

    public MacdResult(double[] dif, double[] dea, double[] histogram) {
        this.dif = dif;
        this.dea = dea;
        this.histogram = histogram;
    }

    public double[] getDif() {
        return dif;
    }

    public void setDif(double[] dif) {
        this.dif = dif;
    }

    public double[] getDea() {
        return dea;
    }

    public void setDea(double[] dea) {
        this.dea = dea;
    }

    public double[] getHistogram() {
        return histogram;
    }

    public void setHistogram(double[] histogram) {
        this.histogram = histogram;
    }

    @Override
    public String toString() {
        return "MACD{len=" + (dif != null ? dif.length : 0) + "}";
    }
}
