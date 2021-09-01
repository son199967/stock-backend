package vn.com.hust.stock.stockmodel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.hust.stock.stockmodel.until.StringArraysConverter;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "priceHistory")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PriceHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String sym;
    private double priceCeil;
    private double priceFloor;
    private double priceTc;
    @JsonProperty("g1")
    @Convert(converter = StringArraysConverter.class)
    private List<String> g1;
    @JsonProperty("g2")
    @Convert(converter = StringArraysConverter.class)
    private List<String> g2;
    @JsonProperty("g3")
    @Convert(converter = StringArraysConverter.class)
    private List<String> g3;
    @JsonProperty("g4")
    @Convert(converter = StringArraysConverter.class)
    private List<String> g4;
    @JsonProperty("g5")
    @Convert(converter = StringArraysConverter.class)
    private List<String> g5;
    @JsonProperty("g6")
    @Convert(converter = StringArraysConverter.class)
    private List<String> g6;
    @JsonProperty("g7")
    @Convert(converter = StringArraysConverter.class)
    private List<String> g7;
    @JsonProperty("total_lot")
    private Long totalLot;
    @JsonProperty("change_pc")
    private double changePc;
    @JsonProperty("ave_price")
    private double avePrice;
    @JsonProperty("high_price")
    private double highPrice;
    @JsonProperty("low_price")
    private double lowPrice;
    @JsonProperty("fB_vol")
    private double fBVol;
    @JsonProperty("fB_value")
    private double fBValue;
    @JsonProperty("fS_volume")
    private double fSVolume;
    @JsonProperty("fS_value")
    private double fSValue;
    @JsonProperty("time")
    private LocalDateTime time;

}
