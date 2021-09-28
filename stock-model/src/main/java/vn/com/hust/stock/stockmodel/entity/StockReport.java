package vn.com.hust.stock.stockmodel.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "stockReport")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int step;

    private int precious;

    private int year;

    private String item;

    private Long value;
    @JsonBackReference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="stockInfo")
    private StockInfo stockInfo;

}
