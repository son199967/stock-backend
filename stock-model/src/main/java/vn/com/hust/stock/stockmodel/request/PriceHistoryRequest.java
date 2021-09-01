package vn.com.hust.stock.stockmodel.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.time.LocalDateTime;
@Data
public class PriceHistoryRequest {
    private String symbol;
    @JsonProperty("from_time")
    private LocalDateTime fromTime;
    @JsonProperty("to_time")
    private LocalDateTime toTime;
}
