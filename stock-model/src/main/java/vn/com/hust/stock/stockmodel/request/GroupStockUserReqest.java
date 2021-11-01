package vn.com.hust.stock.stockmodel.request;

import lombok.Data;

import java.util.List;

@Data
public class GroupStockUserReqest {
    private String nameGroup;
    private List<String> syms;

}
