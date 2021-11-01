package vn.com.hust.stock.stockmodel.response;

import lombok.Data;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;

import java.util.List;
@Data
public class GroupStockUserResponse {
    private String groupName;
    private List<PriceHistory> priceHistoryList;
}
