package vn.com.hust.stock.stockmodel.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class PriceHistoryRequest {
    private List<String> symbol;
    private int day;
    private int reDay;
    private int money;
    private double risk;
    @JsonProperty("from_time")
    private LocalDate fromTime;
    @JsonProperty("to_time")
    private LocalDate toTime;

}
