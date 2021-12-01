package vn.com.hust.stock.stockmodel.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import vn.com.hust.stock.stockmodel.enumm.GroupCompany;

import javax.persistence.*;
import java.util.List;

@Entity
@Table(name = "stock")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @JsonProperty("code")
    private String code;
    @JsonProperty("name_company")
    private String nameCompany;
    @JsonProperty("logo")
    private String logo;
    @JsonProperty("group")
    private GroupCompany groupCompany;
    @JsonProperty("stock_price")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stockPrice_id", referencedColumnName = "id")
    private StockPrice stockPrice;
    @JsonManagedReference
    @OneToMany(mappedBy = "stock",cascade = CascadeType.MERGE)
    private List<Indicator> indicators;

}
