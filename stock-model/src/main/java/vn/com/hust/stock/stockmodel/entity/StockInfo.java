package vn.com.hust.stock.stockmodel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.hust.stock.stockmodel.enumm.Unit;
import vn.com.hust.stock.stockmodel.until.MapConvert;
import vn.com.hust.stock.stockmodel.until.StringArraysConverter;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "stockInfo")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("length_ny")
    private Long lengthNy;
    @JsonProperty("length_lh")
    private Long lengthLh;
    @JsonProperty("capital_now")
    private Long capitalNow;
    @JsonProperty("unit_capital")
    private Unit unit;
    @JsonProperty("date_start")
    private LocalDate date_start;
    @JsonProperty("date_start_price")
    private LocalDate date_start_price;
    @JsonProperty("date_start_length")
    private LocalDate date_start_length;

    @JsonProperty("history_dividend")
    @Convert(converter = MapConvert.class)
    private HashMap<LocalDate,String> historyDividend;
    @JsonProperty("custom_eps")
    private double customEps;
    @JsonProperty("washy_eps")
    private double washyEps;
    @JsonProperty("pe")
    private double pe;
    @JsonProperty("book_value")
    private double bookValue;
    @JsonProperty("unit_book_value")
    private Unit unitBookValue;
    @Convert(converter = StringArraysConverter.class)
    @JsonProperty("history_company_detail")
    private List<String> historyCompanyDetail;

}
