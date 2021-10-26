package vn.com.hust.stock.stockmodel.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import vn.com.hust.stock.stockmodel.enumm.Floor;
import javax.persistence.*;
import java.time.LocalDate;


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
    private double percent;
    private double grossReturn;
    private double simpleReturn;
    @ManyToOne
    private Stock stock;
}
