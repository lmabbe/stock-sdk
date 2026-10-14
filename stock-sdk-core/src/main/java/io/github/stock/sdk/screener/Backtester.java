package io.github.stock.sdk.screener;

import io.github.stock.sdk.model.KlineBar;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 单标的量化策略轻量级本地回测引擎（Single-Asset Long-Only Local Backtester）
 * 对应 Node 版 stock-sdk/screener 模块中的 backtest()
 */
public class Backtester {

    public enum Action {
        BUY,
        SELL,
        HOLD
    }

    @FunctionalInterface
    public interface StrategyFunction {
        /**
         * 针对每一根 K 线 Bar 执行策略逻辑
         * @param bar 当前 K 线
         * @param index 当前 K 线索引
         * @param series 完整历史 K 线序列
         * @return 交易动作 BUY, SELL 或 HOLD
         */
        Action evaluate(KlineBar bar, int index, List<KlineBar> series);
    }

    public static class BacktestOptions {
        private List<KlineBar> klines;
        private StrategyFunction strategy;
        private double initialCapital = 100_000.0;
        private double commissionRate = 0.0003; // 手续费率万分之三

        public BacktestOptions() {
        }

        public BacktestOptions(List<KlineBar> klines, StrategyFunction strategy) {
            this.klines = klines;
            this.strategy = strategy;
        }

        public List<KlineBar> getKlines() {
            return klines;
        }

        public void setKlines(List<KlineBar> klines) {
            this.klines = klines;
        }

        public StrategyFunction getStrategy() {
            return strategy;
        }

        public void setStrategy(StrategyFunction strategy) {
            this.strategy = strategy;
        }

        public double getInitialCapital() {
            return initialCapital;
        }

        public void setInitialCapital(double initialCapital) {
            this.initialCapital = initialCapital;
        }

        public double getCommissionRate() {
            return commissionRate;
        }

        public void setCommissionRate(double commissionRate) {
            this.commissionRate = commissionRate;
        }
    }

    public static class TradeRecord implements Serializable {
        private static final long serialVersionUID = 1L;

        private int buyIndex;
        private String buyDate;
        private double buyPrice;
        private int sellIndex;
        private String sellDate;
        private double sellPrice;
        private double returnRate; // 单笔收益率（例如 0.08 表示 +8%）
        private double profit;     // 单笔盈亏金额
        private long shares;       // 持股数量

        public TradeRecord() {
        }

        public int getBuyIndex() {
            return buyIndex;
        }

        public void setBuyIndex(int buyIndex) {
            this.buyIndex = buyIndex;
        }

        public String getBuyDate() {
            return buyDate;
        }

        public void setBuyDate(String buyDate) {
            this.buyDate = buyDate;
        }

        public double getBuyPrice() {
            return buyPrice;
        }

        public void setBuyPrice(double buyPrice) {
            this.buyPrice = buyPrice;
        }

        public int getSellIndex() {
            return sellIndex;
        }

        public void setSellIndex(int sellIndex) {
            this.sellIndex = sellIndex;
        }

        public String getSellDate() {
            return sellDate;
        }

        public void setSellDate(String sellDate) {
            this.sellDate = sellDate;
        }

        public double getSellPrice() {
            return sellPrice;
        }

        public void setSellPrice(double sellPrice) {
            this.sellPrice = sellPrice;
        }

        public double getReturnRate() {
            return returnRate;
        }

        public void setReturnRate(double returnRate) {
            this.returnRate = returnRate;
        }

        public double getProfit() {
            return profit;
        }

        public void setProfit(double profit) {
            this.profit = profit;
        }

        public long getShares() {
            return shares;
        }

        public void setShares(long shares) {
            this.shares = shares;
        }

        @Override
        public String toString() {
            return "Trade{" + buyDate + " -> " + sellDate + ", ret=" + String.format("%.2f%%", returnRate * 100) + ", profit=" + String.format("%.2f", profit) + "}";
        }
    }

    public static class EquityPoint implements Serializable {
        private static final long serialVersionUID = 1L;

        private String date;
        private double equity;
        private double benchmark;

        public EquityPoint() {
        }

        public EquityPoint(String date, double equity, double benchmark) {
            this.date = date;
            this.equity = equity;
            this.benchmark = benchmark;
        }

        public String getDate() {
            return date;
        }

        public double getEquity() {
            return equity;
        }

        public double getBenchmark() {
            return benchmark;
        }
    }

    public static class BacktestReport implements Serializable {
        private static final long serialVersionUID = 1L;

        private double initialCapital;
        private double finalValue;
        private double totalReturnRate;     // 总收益率 (例如 0.35 表示 35%)
        private double annualizedReturnRate;// 年化收益率
        private double maxDrawdown;         // 最大回撤 (例如 0.12 表示 12%)
        private double winRate;             // 胜率 (0.0 ~ 1.0)
        private int totalTrades;
        private int winningTrades;
        private double profitFactor;        // 盈亏比
        private List<TradeRecord> trades = new ArrayList<>();
        private List<EquityPoint> equityCurve = new ArrayList<>();

        public BacktestReport() {
        }

        public double getInitialCapital() {
            return initialCapital;
        }

        public void setInitialCapital(double initialCapital) {
            this.initialCapital = initialCapital;
        }

        public double getFinalValue() {
            return finalValue;
        }

        public void setFinalValue(double finalValue) {
            this.finalValue = finalValue;
        }

        public double getTotalReturnRate() {
            return totalReturnRate;
        }

        public void setTotalReturnRate(double totalReturnRate) {
            this.totalReturnRate = totalReturnRate;
        }

        public double getAnnualizedReturnRate() {
            return annualizedReturnRate;
        }

        public void setAnnualizedReturnRate(double annualizedReturnRate) {
            this.annualizedReturnRate = annualizedReturnRate;
        }

        public double getMaxDrawdown() {
            return maxDrawdown;
        }

        public void setMaxDrawdown(double maxDrawdown) {
            this.maxDrawdown = maxDrawdown;
        }

        public double getWinRate() {
            return winRate;
        }

        public void setWinRate(double winRate) {
            this.winRate = winRate;
        }

        public int getTotalTrades() {
            return totalTrades;
        }

        public void setTotalTrades(int totalTrades) {
            this.totalTrades = totalTrades;
        }

        public int getWinningTrades() {
            return winningTrades;
        }

        public void setWinningTrades(int winningTrades) {
            this.winningTrades = winningTrades;
        }

        public double getProfitFactor() {
            return profitFactor;
        }

        public void setProfitFactor(double profitFactor) {
            this.profitFactor = profitFactor;
        }

        public List<TradeRecord> getTrades() {
            return trades;
        }

        public void setTrades(List<TradeRecord> trades) {
            this.trades = trades;
        }

        public List<EquityPoint> getEquityCurve() {
            return equityCurve;
        }

        public void setEquityCurve(List<EquityPoint> equityCurve) {
            this.equityCurve = equityCurve;
        }

        @Override
        public String toString() {
            return "BacktestReport{" +
                    "init=" + initialCapital +
                    ", final=" + String.format("%.2f", finalValue) +
                    ", totalReturn=" + String.format("%.2f%%", totalReturnRate * 100) +
                    ", maxDrawdown=" + String.format("%.2f%%", maxDrawdown * 100) +
                    ", winRate=" + String.format("%.2f%%", winRate * 100) +
                    ", tradesCount=" + totalTrades +
                    '}';
        }
    }

    /**
     * 运行量化回测
     */
    public static BacktestReport run(BacktestOptions options) {
        if (options == null || options.getKlines() == null || options.getKlines().isEmpty() || options.getStrategy() == null) {
            throw new IllegalArgumentException("Invalid backtest options");
        }

        List<KlineBar> klines = options.getKlines();
        StrategyFunction strategy = options.getStrategy();
        double cash = options.getInitialCapital();
        double commRate = options.getCommissionRate();
        double initialCapital = options.getInitialCapital();

        long holdingShares = 0L;
        double buyPrice = 0.0;
        int buyIndex = -1;
        String buyDate = null;

        List<TradeRecord> trades = new ArrayList<>();
        List<EquityPoint> equityCurve = new ArrayList<>();

        double peakEquity = initialCapital;
        double maxDrawdown = 0.0;
        double totalGains = 0.0;
        double totalLosses = 0.0;
        int winCount = 0;

        double initialBenchmarkPrice = klines.get(0).closeVal();

        for (int i = 0; i < klines.size(); i++) {
            KlineBar bar = klines.get(i);
            double currentPrice = bar.closeVal();
            Action action = strategy.evaluate(bar, i, klines);

            if (action == Action.BUY && holdingShares == 0 && cash > 0) {
                // 买入信号：全仓按 100 股一手买入
                long canBuy = (long) Math.floor((cash / (1.0 + commRate)) / currentPrice / 100.0) * 100L;
                if (canBuy > 0) {
                    holdingShares = canBuy;
                    buyPrice = currentPrice;
                    buyIndex = i;
                    buyDate = bar.getDate();
                    double cost = canBuy * currentPrice;
                    double commission = cost * commRate;
                    cash -= (cost + commission);
                }
            } else if (action == Action.SELL && holdingShares > 0) {
                // 卖出平仓信号
                double revenue = holdingShares * currentPrice;
                double commission = revenue * commRate;
                cash += (revenue - commission);

                double profit = (currentPrice - buyPrice) * holdingShares - (buyPrice * holdingShares * commRate + commission);
                double retRate = buyPrice > 0 ? (currentPrice - buyPrice) / buyPrice : 0.0;

                TradeRecord record = new TradeRecord();
                record.setBuyIndex(buyIndex);
                record.setBuyDate(buyDate);
                record.setBuyPrice(buyPrice);
                record.setSellIndex(i);
                record.setSellDate(bar.getDate());
                record.setSellPrice(currentPrice);
                record.setShares(holdingShares);
                record.setProfit(profit);
                record.setReturnRate(retRate);
                trades.add(record);

                if (profit > 0) {
                    winCount++;
                    totalGains += profit;
                } else {
                    totalLosses += Math.abs(profit);
                }

                holdingShares = 0L;
                buyPrice = 0.0;
                buyIndex = -1;
                buyDate = null;
            }

            // 计算当日总权益
            double currentEquity = cash + (holdingShares * currentPrice);
            peakEquity = Math.max(peakEquity, currentEquity);
            double currentDrawdown = peakEquity > 0 ? (peakEquity - currentEquity) / peakEquity : 0.0;
            maxDrawdown = Math.max(maxDrawdown, currentDrawdown);

            double benchmark = initialBenchmarkPrice > 0 ? initialCapital * (currentPrice / initialBenchmarkPrice) : initialCapital;
            equityCurve.add(new EquityPoint(bar.getDate(), currentEquity, benchmark));
        }

        // 若回测结束时仍有持仓，强制在最后一日收盘价清算
        if (holdingShares > 0) {
            KlineBar lastBar = klines.get(klines.size() - 1);
            double currentPrice = lastBar.closeVal();
            double revenue = holdingShares * currentPrice;
            double commission = revenue * commRate;
            cash += (revenue - commission);

            double profit = (currentPrice - buyPrice) * holdingShares - (buyPrice * holdingShares * commRate + commission);
            double retRate = buyPrice > 0 ? (currentPrice - buyPrice) / buyPrice : 0.0;

            TradeRecord record = new TradeRecord();
            record.setBuyIndex(buyIndex);
            record.setBuyDate(buyDate);
            record.setBuyPrice(buyPrice);
            record.setSellIndex(klines.size() - 1);
            record.setSellDate(lastBar.getDate());
            record.setSellPrice(currentPrice);
            record.setShares(holdingShares);
            record.setProfit(profit);
            record.setReturnRate(retRate);
            trades.add(record);

            if (profit > 0) {
                winCount++;
                totalGains += profit;
            } else {
                totalLosses += Math.abs(profit);
            }
        }

        double finalEquity = cash;
        double totalReturn = (finalEquity - initialCapital) / initialCapital;
        double days = Math.max(1, klines.size());
        double annualized = Math.pow(1.0 + totalReturn, 250.0 / days) - 1.0;

        BacktestReport report = new BacktestReport();
        report.setInitialCapital(initialCapital);
        report.setFinalValue(finalEquity);
        report.setTotalReturnRate(totalReturn);
        report.setAnnualizedReturnRate(annualized);
        report.setMaxDrawdown(maxDrawdown);
        report.setTotalTrades(trades.size());
        report.setWinningTrades(winCount);
        report.setWinRate(trades.isEmpty() ? 0.0 : (double) winCount / trades.size());
        report.setProfitFactor(totalLosses > 0 ? totalGains / totalLosses : (totalGains > 0 ? 99.0 : 0.0));
        report.setTrades(trades);
        report.setEquityCurve(equityCurve);

        return report;
    }
}
