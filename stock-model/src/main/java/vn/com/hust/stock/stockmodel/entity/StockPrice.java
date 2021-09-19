package vn.com.hust.stock.stockmodel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "stockPrice")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockPrice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    //
    @JsonProperty("trade_price")
    private double tradePrice;
    @JsonProperty("percent_fluctuate")
    private double percent_fluctuate;
    @JsonProperty("price_fluctuate")
    private double price_fluctuate;
    @JsonProperty("color")
    private char color;
    //
    @JsonProperty("custom_price")
    private double customPrice;
    @JsonProperty("floor_price")
    private double floorPrice;
    @JsonProperty("ceil_price")
    private double ceilPrice;
    @JsonProperty("open_price")
    private double openPrice;
    @JsonProperty("height_price")
    private double heightPrice;
    @JsonProperty("low_price")
    private double lowPrice;
    @JsonProperty("length")
    private Long length;
    //
}
