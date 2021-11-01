package vn.com.hust.stock.stockmodel.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PriceHistoryRequest {
    private List<String> symbol;
    private int day;
    private int reDay;
    private long money;
    private double risk;
    @JsonProperty("from_time")
    private LocalDate fromTime;
    @JsonProperty("to_time")
    private LocalDate toTime;


}
