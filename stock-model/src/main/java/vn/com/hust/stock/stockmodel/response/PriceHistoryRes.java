package vn.com.hust.stock.stockmodel.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;



@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistoryRes {

    private Long id;
    private String sym;
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private LocalDate time;
    private double percent;
    private double grossReturn;
    private double simpleReturn;
    private double logReturn;
    private double volatility;
    private double cumulativeLog;
    private double targetWeights;
    private double annualisedStandardDeviation;
    private double constrainedWeightsLeverage;
    private double numberOfSharesWithEquity;
    private double cash;
    private double numberStock;
    private double money;
    private double priceStock;
}
