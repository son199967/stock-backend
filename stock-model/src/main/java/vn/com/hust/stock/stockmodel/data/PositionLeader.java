package vn.com.hust.stock.stockmodel.data;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.hust.stock.stockmodel.enumm.Position;
import vn.com.hust.stock.stockmodel.until.CustomConverter;
import vn.com.hust.stock.stockmodel.until.StockHoldConverter;
import vn.com.hust.stock.stockmodel.until.StringArraysConverter;

import javax.persistence.Convert;
import java.time.LocalDate;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PositionLeader {
    @JsonProperty("position")
    private Position position;
    @JsonProperty("group_position")
    private String groupPosition;
    @JsonProperty("time_apply")
    private LocalDate timeApply;
    @JsonProperty("stock_holds")
    @Convert(converter = StockHoldConverter.class)
    private List<StockHold> stockHolds;
    @JsonProperty("represent_holds")
    @Convert(converter = StockHoldConverter.class)
    private List<StockHold> representHolds;
    @JsonProperty("history_studys")
    @Convert(converter = StringArraysConverter.class)
    private List<String> historyStudys;
    @JsonProperty("history_works")
    @Convert(converter = StringArraysConverter.class)
    private List<String> historyWorks;

}
