package io.github.stock.sdk.indicators;

import java.io.Serializable;

public class DmiResult implements Serializable {
    private static final long serialVersionUID = 1L;

    private double[] pdi;
    private double[] mdi;
    private double[] adx;

    public DmiResult() {
    }

    public DmiResult(double[] pdi, double[] mdi, double[] adx) {
        this.pdi = pdi;
        this.mdi = mdi;
        this.adx = adx;
    }

    public double[] getPdi() {
        return pdi;
    }

    public void setPdi(double[] pdi) {
        this.pdi = pdi;
    }

    public double[] getMdi() {
        return mdi;
    }

    public void setMdi(double[] mdi) {
        this.mdi = mdi;
    }

    public double[] getAdx() {
        return adx;
    }

    public void setAdx(double[] adx) {
        this.adx = adx;
    }
}
