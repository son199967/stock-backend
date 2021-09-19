package vn.com.hust.stock.stockmodel.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter
@Setter
public class PriceHistoryRequest {
    private String symbol;
    @JsonProperty("from_time")
    private LocalDate fromTime;
    @JsonProperty("to_time")
    private LocalDate toTime;
}
