package vn.com.hust.stock.stockmodel.request;

import lombok.Data;
import vn.com.hust.stock.stockmodel.enumm.GroupCompany;

@Data
public class StockRequest {
    private String code;
    private String name;
    private double priceFrom;
    private double priceTo;
    private String email;
    private GroupCompany groupCompany;
}
