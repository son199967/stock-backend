package vn.com.hust.stock.stockmodel.response;

import lombok.Data;

@Data
public class VolatilitySymbolsResponse {
    private String symbols;
    private long money;
    private int stockHold;
    private double price;
    private long remainMoney;
    private double simpleReturn;
    private double targetWight;
    private double constrainedWeightsLeverage;
    private double numberOfSharesWithEquity;
}