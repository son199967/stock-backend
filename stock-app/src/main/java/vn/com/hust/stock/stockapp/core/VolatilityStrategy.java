package vn.com.hust.stock.stockapp.core;

import vn.com.hust.stock.stockmodel.response.VolatilityStrategyResponse;
import vn.com.hust.stock.stockmodel.response.VolatilitySymbolsResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VolatilityStrategy {
    private Map<LocalDate, List<CorePrice>> mapPrice;
    private long money;


    public VolatilityStrategy(Map<LocalDate, List<CorePrice>> mapPrice, long money) {
        this.mapPrice = mapPrice;
        this.money = money;
    }

    public List<VolatilityStrategyResponse> volatilityStrategyResponse() {
        List<VolatilityStrategyResponse> responses = new ArrayList<>();
        Map<String, Integer> asset = new HashMap<>();
        double cash = 0;
        boolean start = true;
        for (Map.Entry<LocalDate, List<CorePrice>> map : mapPrice.entrySet()) {
            VolatilityStrategyResponse volatilityStrategy = new VolatilityStrategyResponse();
            volatilityStrategy.setTime(map.getKey());
            double totalTargetWeight = map.getValue().stream().reduce(0D, (s, e) -> {
                return s + e.getTargetWeight();
            }, Double::sum);
            List<VolatilitySymbolsResponse> symbolsResponses = new ArrayList<>();
            if (start==false) {
                money = (long) (map.getValue().stream().reduce(0D, (s, l) -> {
                    if (!asset.containsKey(l.getPriceHistory().getSym())) return 0D;
                    return s + asset.get(l.getPriceHistory().getSym()) * l.getPriceHistory().getClose();
                }, Double::sum).doubleValue() + cash);
            }
            start= false;
            map.getValue().forEach(m -> {
                VolatilitySymbolsResponse volatility = new VolatilitySymbolsResponse();
                volatility.setSymbols(m.getPriceHistory().getSym());
                volatility.setSimpleReturn(m.getPriceHistory().getSimpleReturn());
                volatility.setTargetWight(m.getTargetWeight());
                volatility.setConstrainedWeightsLeverage(totalTargetWeight < 1 ? volatility.getTargetWight() : volatility.getTargetWight() / totalTargetWeight);
                volatility.setNumberOfSharesWithEquity(money * volatility.getConstrainedWeightsLeverage());
                int stockHold = (int) ((money * volatility.getTargetWight()) / (m.getPriceHistory().getClose() * 1000));
                volatility.setMoney((long) volatility.getNumberOfSharesWithEquity());
                symbolsResponses.add(volatility);
                asset.put(volatility.getSymbols(), stockHold);
            });
            double totalMoney = symbolsResponses.stream().reduce(0D, (s, e) -> {
                return s + e.getMoney();
            }, Double::sum);
            double constrainedWeightsLeverageTotal = symbolsResponses.stream().reduce(0D, (s, e) -> {
                return s + e.getConstrainedWeightsLeverage();
            }, Double::sum);
            double numberOfSharesWithEquityTotal = symbolsResponses.stream().reduce(0D, (s, e) -> {
                return s + e.getConstrainedWeightsLeverage();
            }, Double::sum);
            volatilityStrategy.setCash(money - (long) totalMoney);
            cash = volatilityStrategy.getCash();
            volatilityStrategy.setMoney((long) totalMoney);
            volatilityStrategy.setRemainMoney(money - volatilityStrategy.getMoney());
            volatilityStrategy.setTargetWight(totalTargetWeight);
            volatilityStrategy.setConstrainedWeightsLeverage(constrainedWeightsLeverageTotal);
            volatilityStrategy.setNumberOfSharesWithEquity(numberOfSharesWithEquityTotal);
            volatilityStrategy.setVolSymbols(symbolsResponses);
            responses.add(volatilityStrategy);
        }
        return responses;
    }

}
