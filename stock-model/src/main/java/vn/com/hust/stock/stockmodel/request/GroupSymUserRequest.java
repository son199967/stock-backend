package vn.com.hust.stock.stockmodel.request;

import lombok.Data;

import java.util.List;

@Data
public class GroupSymUserRequest {
    private String groupName;
    private List<String> symbols;
}
