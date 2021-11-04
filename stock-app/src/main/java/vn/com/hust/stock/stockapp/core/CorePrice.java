package vn.com.hust.stock.stockapp.core;

import vn.com.hust.stock.stockmodel.entity.PriceHistory;

import java.util.List;

public class CorePrice {

    private PriceHistory priceHistoryBefore;
    private PriceHistory priceHistory;
    private double cumulativeLog;

    public CorePrice(PriceHistory priceHistoryBefore, PriceHistory priceHistory, double cumulativeLog) {
        this.priceHistoryBefore = priceHistoryBefore;
        this.priceHistory = priceHistory;
        this.cumulativeLog = cumulativeLog;
        calculateNormal(priceHistory);
    }

    public PriceHistory calculateNormal(PriceHistory priceHistory) {
        double priceT = priceHistory.getClose();
        double priceT_1 = priceHistoryBefore.getClose();
        double simpleReturn = (priceT - priceT_1) / priceT_1;
        double grossReturn = 1 + simpleReturn;
        double logReturn = Math.log(grossReturn);
        cumulativeLog = cumulativeLog * (1 + logReturn);
        priceHistory.setSimpleReturn(simpleReturn * 100);
        priceHistory.setGrossReturn(grossReturn * 100);
        priceHistory.setLogReturn(logReturn * 100);
        priceHistory.setCumulativeLog(cumulativeLog);
        return priceHistory;
    }

    public double getCumulativeLogReturn() {
        return cumulativeLog;
    }

    public PriceHistory getPriceHistory() {
        return priceHistory;
    }

    public double getTargetWeight() {
        return priceHistory.getTargetWeights();
    }


    public PriceHistory calculateHigh(List<Double> simpleReturnDay_BeforeDay, int Day, double risk, double money, boolean isStart) {
        int stockHold = (int) priceHistoryBefore.getNumberStock();
        double cash = (int) priceHistoryBefore.getCash();
        double setVolatility = volatility(simpleReturnDay_BeforeDay, Day);
        if (!isStart == true) {
            money = (int) (stockHold * priceHistory.getClose() * 1000 + cash);
        }
        priceHistory.setVolatility(setVolatility);
        priceHistory.setAnnualisedStandardDeviation(setVolatility / 100);
        if ((risk * 100) > setVolatility) risk = setVolatility / 100;
        priceHistory.setTargetWeights(risk / (setVolatility / 100));
        priceHistory.setNumberOfSharesWithEquity(money * (risk * 100 / (setVolatility)));
        cash = money - priceHistory.getNumberOfSharesWithEquity();
        priceHistory.setCash(cash);
        stockHold = (int) ((money * priceHistory.getTargetWeights()) / (priceHistory.getClose() * 1000));
        priceHistory.setNumberStock(stockHold);
        priceHistory.setPriceStock(stockHold * priceHistory.getClose() * 1000);
        priceHistory.setMoney(money);
        return priceHistory;
    }

    public PriceHistory volatilityStrategy(List<Double> simpleReturnDay_BeforeDay, int Day, double risk) {
        double setVolatility = volatility(simpleReturnDay_BeforeDay, Day);
        priceHistory.setTargetWeights(risk / (setVolatility / 100));
        return priceHistory;
    }

    public double volatility(List<Double> numArray, int Day) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = Day;
        for (int j = 0; j < Day; j++) {
            sum += numArray.get(j);
        }
        double mean = sum / length;
        for (int j = 0; j < Day; j++) {
            standardDeviation += Math.pow(numArray.get(j) - mean, 2);
        }
        return Math.sqrt(standardDeviation / length) * Math.sqrt(252);
    }
}
