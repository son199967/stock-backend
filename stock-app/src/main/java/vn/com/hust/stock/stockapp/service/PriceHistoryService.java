package vn.com.hust.stock.stockapp.service;

import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;
import vn.com.hust.stock.stockmodel.response.NormalStrategyResponse;

import java.util.List;

public interface PriceHistoryService {
    List<PriceHistory> getPriceHistory(PriceHistoryRequest priceHistoryRequest);
    void calculateListSimplePrice();
    List<PriceHistory> calculateSimplePrice(PriceHistoryRequest priceHistoryRequest);
    List<PriceHistory> loadTest(PriceHistoryRequest priceHistoryRequest);

    List<NormalStrategyResponse> normalStrategy(PriceHistoryRequest priceHistoryRequest);

    List<PriceHistory> priceLast(String field , String order,List<String> syms);
    List<PriceHistory> histogram(String field , String order);
    List<PriceHistory> groupHistogram();
    List<PriceHistory> groupCommon();

    List<PriceHistory> updateData();

}
