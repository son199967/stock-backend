package vn.com.hust.stock.stockmodel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.hust.stock.stockmodel.enumm.Floor;
import vn.com.hust.stock.stockmodel.until.StringArraysConverter;

import javax.persistence.*;
import java.time.LocalDate;
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
    private double open;
    private double high;
    private double low;
    private double close;
    private double volume;
    private LocalDate time;
    @Enumerated(EnumType.STRING)
    private Floor floor;
    private double grossReturn;
    private double simpleReturn;
    private double logReturn;
    private double volatility;
    private double cumulativeLog;
    private double targetWeights;
    private double annualisedStandardDeviation;
    private double constrainedWeightsLeverage;
    private double numberOfSharesWithEquity;
    private double cash;
    private double numberStock;
    private double money;
    private double priceStock;

}
