package vn.com.hust.stock.stockmodel.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import vn.com.hust.stock.stockmodel.enumm.Group;

import javax.persistence.*;

@Entity
@Table(name = "stock")
@Builder
@Getter
@Setter
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
    @JsonProperty("description")
    private String description;
    @JsonProperty("address")
    private String address;
    @JsonProperty("phone")
    private String phone;
    @JsonProperty("email")
    private String email;
    @JsonProperty("website")
    private String website;
    @JsonProperty("price")
    private double price;
    @JsonProperty("logo")
    private String logo;
    @JsonProperty("stock_info")
    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stockInfo_id", referencedColumnName = "id")
    private StockInfo stockInfo;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "stockPrice_id", referencedColumnName = "id")
    private StockPrice stockPrice;
    @JsonProperty("group")
    @Enumerated(EnumType.STRING)
    private Group group;

}
