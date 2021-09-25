package vn.com.hust.stock.stockmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mysql.cj.xdevapi.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class StockHold {
    private String sym;
    private Long length;
    private double percent;
    @JsonProperty("to_date")
    private LocalDate toDate;
    private double value;

}
