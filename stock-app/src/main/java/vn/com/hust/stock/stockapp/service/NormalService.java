package vn.com.hust.stock.stockapp.service;

import vn.com.hust.stock.stockmodel.entity.PriceHistory;

import java.util.List;

public interface NormalService {
    List<PriceHistory> groupHistogram();

    List<PriceHistory> priceLast(String field , String order,List<String> syms);

    List<PriceHistory> customIndex(String sym );

    void resetCache();

}
