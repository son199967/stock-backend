package vn.com.hust.stock.stockmodel.response;

import lombok.Data;

import java.time.LocalDate;
@Data
public class NormalStrategyResponse {
    private LocalDate time;
    private String symbol;
    private long money;

}
