package vn.com.hust.stock.stockapp.core;

import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.response.VolatilityStrategyResponse;
import vn.com.hust.stock.stockmodel.response.VolatilitySymbolsResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VolatilityStrategy {
    private Map<LocalDate, List<CorePrice>> mapPrice;
    private long money;


    public VolatilityStrategy(Map<LocalDate, List<CorePrice>> mapPrice,long money) {
        this.mapPrice = mapPrice;
        this.money = money;
    }

    public List<VolatilityStrategyResponse> volatilityStrategyResponse(){
        List<VolatilityStrategyResponse> responses = new ArrayList<>();
        for (Map.Entry<LocalDate,List<CorePrice>> map: mapPrice.entrySet()){

            VolatilityStrategyResponse volatilityStrategy = new VolatilityStrategyResponse();
            volatilityStrategy.setTime(map.getKey());
            double totalTargetWeight = map.getValue().stream().reduce(0D,(s ,e) -> {
                return s+e.getTargetWeight();
            },Double::sum);
            List<VolatilitySymbolsResponse> symbolsResponses = new ArrayList<>();
                map.getValue().forEach(m -> {
                    VolatilitySymbolsResponse volatility = new VolatilitySymbolsResponse();
                    volatility.setSymbols(m.getPriceHistory().getSym());
                    volatility.setSimpleReturn(m.getPriceHistory().getSimpleReturn());
                    volatility.setTargetWight(m.getTargetWeight());
                    volatility.setConstrainedWeightsLeverage(totalTargetWeight<1?volatility.getTargetWight():volatility.getTargetWight()/totalTargetWeight);
                    volatility.setNumberOfSharesWithEquity(money* volatility.getConstrainedWeightsLeverage());
                    volatility.setMoney((long) volatility.getNumberOfSharesWithEquity());
                    symbolsResponses.add(volatility);
                });
            double totalMoney =symbolsResponses.stream().reduce(0D,(s ,e) -> {
                return s+e.getMoney();
            },Double::sum);
            double constrainedWeightsLeverageTotal =symbolsResponses.stream().reduce(0D,(s ,e) -> {
                return s+e.getConstrainedWeightsLeverage();
            },Double::sum);
            double numberOfSharesWithEquityTotal =symbolsResponses.stream().reduce(0D,(s ,e) -> {
                return s+e.getConstrainedWeightsLeverage();
            },Double::sum);
            volatilityStrategy.setMoney((long) totalMoney);
            volatilityStrategy.setRemainMoney(money- volatilityStrategy.getMoney());
            volatilityStrategy.setTargetWight(totalTargetWeight);
            volatilityStrategy.setConstrainedWeightsLeverage(constrainedWeightsLeverageTotal);
            volatilityStrategy.setNumberOfSharesWithEquity(numberOfSharesWithEquityTotal);
            volatilityStrategy.setVolSymbols(symbolsResponses);
           responses.add(volatilityStrategy);
        }
        return responses;
    }

}
