package vn.com.hust.stock.stockmodel.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "indicator")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Indicator implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int year;


    private double eps;

    private double bv;

    private double pe;

    private double roa;

    private double roe;

    private double ros;

    private double gos;

    private double dar;


    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stockInfo")
    private StockInfo stockInfo;



}
