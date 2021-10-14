package vn.com.hust.stock.stockapp.core;

import vn.com.hust.stock.stockmodel.entity.PriceHistory;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

public class PriceCalculate implements Callable<List<PriceHistory>> {

    private final List<PriceHistory> priceHistories;

    private final int DAY;

    private final double RISK;

    private  int MONEY;

    private ScheduledExecutorService executorService;

    public PriceCalculate(List<PriceHistory> priceHistories, int DAY, double RISK, int MONEY) {
        this.priceHistories = priceHistories;
        this.DAY = DAY;
        this.RISK = RISK;
        this.MONEY = MONEY;
        new Thread(() -> {
            executorService = Executors.newScheduledThreadPool(10);
        }).start();
    }

    @Override
    public List<PriceHistory> call() throws Exception {

        double cumulativeLog = 1;
        for (int i = 1; i < priceHistories.size(); i++) {
            cumulativeLog = coreCalculatePrice(priceHistories, cumulativeLog, i);
        }
        int numberStock =0;
        double cash =0.0d;
        List<Double> simpleReturn = priceHistories.stream().map(p -> p.getSimpleReturn()).collect(Collectors.toList());

        for (int k=0;k<DAY;k++){
            simpleReturn.add(0D);
        }
        for (int i = 1; i < priceHistories.size()-DAY; i++) {
            double setVolatility = executorService.submit(new CalculateSD(DAY,simpleReturn,i)).get();
            if (i>1){
                MONEY = (int) (numberStock * priceHistories.get(i+DAY-1).getClose() +cash);
            }
            priceHistories.get(i+DAY-1).setVolatility(setVolatility);
            priceHistories.get(i+DAY-1).setAnnualisedStandardDeviation(setVolatility/100);
            priceHistories.get(i+DAY-1).setTargetWeights(RISK/(setVolatility/100));
            priceHistories.get(i+DAY-1).setNumberOfSharesWithEquity(MONEY*(RISK/(setVolatility/100)));
            cash = MONEY - priceHistories.get(i+DAY-1).getNumberOfSharesWithEquity();
            priceHistories.get(i+DAY-1).setCash(cash);
            numberStock = (int) ((MONEY* priceHistories.get(i+DAY-1).getTargetWeights())/priceHistories.get(i+DAY-1).getClose());
            priceHistories.get(i+DAY-1).setNumberStock(numberStock);
            priceHistories.get(i+DAY-1).setPriceStock(numberStock*priceHistories.get(i+DAY-1).getClose());
            priceHistories.get(i+DAY-1).setMoney(MONEY);
        }
        return priceHistories;
    }
    private double coreCalculatePrice(List<PriceHistory> priceHistories, double cumulativeLog, int i) {

        PriceHistory priceHistory = priceHistories.get(i);
        double priceT = priceHistory.getClose();
        double priceT_1 = priceHistories.get(i - 1).getClose();
        double simpleReturn = (priceT - priceT_1) / priceT_1 ;
        double grossReturn = 1 + simpleReturn;
        double logReturn = Math.log(grossReturn);
        cumulativeLog = cumulativeLog * (1 + logReturn);
        priceHistory.setSimpleReturn(simpleReturn*100);
        priceHistory.setGrossReturn(grossReturn*100);
        priceHistory.setLogReturn(logReturn*100);
        priceHistory.setCumulativeLog(cumulativeLog);
        return cumulativeLog;
    }
}
