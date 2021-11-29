package vn.com.hust.stock.stockapp.core;

import vn.com.hust.stock.stockmodel.response.VolatilityStrategyResponse;
import vn.com.hust.stock.stockmodel.response.VolatilitySymbolsResponse;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        Map<String,Double> priceLast = new HashMap<>();
        double cash = 0;
        boolean start = true;
        for (Map.Entry<LocalDate, List<CorePrice>> map : mapPrice.entrySet()) {
            VolatilityStrategyResponse volatilityStrategy = new VolatilityStrategyResponse();
            volatilityStrategy.setTime(map.getKey());
            long moneyStart = money;
            money =0;

            double totalTargetWeight = map.getValue().stream().reduce(0D, (s, e) -> s + e.getTargetWeight(), Double::sum);
            List<VolatilitySymbolsResponse> symbolsResponses = new ArrayList<>();
            if (start==false) {
                for (Map.Entry<String,Integer> assetData : asset.entrySet()){
                    boolean check = false;
                   for (CorePrice corePrice : map.getValue()){
                       if (assetData.getKey().equals(corePrice.getPriceHistory().getSym())){
                           money += (long) (corePrice.getPriceHistory().getClose()*1000)* assetData.getValue();
                           check = true;
                           break;
                       }
                   }
                   if (check == false)
                   money  += priceLast.get(assetData.getKey())*1000*assetData.getValue();
                }
                money +=  cash;
            }
            volatilityStrategy.setTotalMoney(money);
            if (money==0) money = moneyStart;
            long moneyData = money;
            List<String> syms = map.getValue().stream().map(c->c.getPriceHistory().getSym()).collect(Collectors.toList());

            for (Map.Entry<String,Integer> m : asset.entrySet()){
                if (!syms.contains(m.getKey())){
                    moneyData -= m.getValue() * priceLast.get(m.getKey())*1000;
                }
            }

            long finalMoneyData = moneyData;
            map.getValue().forEach(m -> {
                VolatilitySymbolsResponse volatility = new VolatilitySymbolsResponse();
                volatility.setSymbols(m.getPriceHistory().getSym());
                volatility.setSimpleReturn(m.getPriceHistory().getSimpleReturn());
                volatility.setTargetWight(m.getTargetWeight());
                volatility.setConstrainedWeightsLeverage(totalTargetWeight < 1 ? volatility.getTargetWight() : volatility.getTargetWight() / totalTargetWeight);
                volatility.setNumberOfSharesWithEquity(finalMoneyData * volatility.getConstrainedWeightsLeverage());
                int stockHold = (int) ((volatility.getNumberOfSharesWithEquity()) / (m.getPriceHistory().getClose() * 1000));
                volatility.setStockHold(stockHold);
                volatility.setPrice(m.getPriceHistory().getClose()*1000);
                volatility.setMoney((long) volatility.getNumberOfSharesWithEquity());
                symbolsResponses.add(volatility);
                asset.put(volatility.getSymbols(), stockHold);
                priceLast.put(volatility.getSymbols(),m.getPriceHistory().getClose());
            });

            if (!asset.isEmpty()) start= false;
            double totalMoney = 0D;
            for(Map.Entry<String,Integer> holeStock: asset.entrySet()){
                totalMoney += holeStock.getValue()* priceLast.get(holeStock.getKey())*1000;
            }

            double constrainedWeightsLeverageTotal = symbolsResponses.stream().reduce(0D, (s, e) -> s + e.getConstrainedWeightsLeverage(), Double::sum);
            double numberOfSharesWithEquityTotal = symbolsResponses.stream().reduce(0D, (s, e) -> s + e.getConstrainedWeightsLeverage(), Double::sum);
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
