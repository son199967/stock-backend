package vn.com.hust.stock.stockmodel.request;

import lombok.Data;

import java.util.List;

@Data
public class SymbolsAddRequest {
    private List<String> symbols;
    private Long idGroup;
}
