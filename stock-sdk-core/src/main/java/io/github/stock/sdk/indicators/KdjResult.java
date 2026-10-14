package io.github.stock.sdk.indicators;

import java.io.Serializable;

public class KdjResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private double[] k;
    private double[] d;
    private double[] j;

    public KdjResult() {
    }

    public KdjResult(double[] k, double[] d, double[] j) {
        this.k = k;
        this.d = d;
        this.j = j;
    }

    public double[] getK() {
        return k;
    }

    public void setK(double[] k) {
        this.k = k;
    }

    public double[] getD() {
        return d;
    }

    public void setD(double[] d) {
        this.d = d;
    }

    public double[] getJ() {
        return j;
    }

    public void setJ(double[] j) {
        this.j = j;
    }
}
