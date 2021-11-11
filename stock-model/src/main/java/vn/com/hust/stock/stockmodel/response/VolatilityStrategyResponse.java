package vn.com.hust.stock.stockmodel.response;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class VolatilityStrategyResponse {
    private LocalDate time;
    private long totalMoney;
    private long money;
    private long cash;
    private long remainMoney;
    private double targetWight;
    private double constrainedWeightsLeverage;
    private double numberOfSharesWithEquity;
    private List<VolatilitySymbolsResponse> volSymbols;
}
