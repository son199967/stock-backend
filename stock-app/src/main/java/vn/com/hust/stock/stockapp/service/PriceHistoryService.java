package vn.com.hust.stock.stockapp.service;

import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;

import java.util.List;

public interface PriceHistoryService {
    List<PriceHistory> getPriceHistory(PriceHistoryRequest priceHistoryRequest);
    void calculateListSimplePrice();
    List<PriceHistory> calculateSimplePrice(PriceHistoryRequest priceHistoryRequest);
    List<PriceHistory> loadtest(PriceHistoryRequest priceHistoryRequest);
}
