package vn.com.hust.stock.stockapp.service;

import vn.com.hust.stock.stockmodel.entity.Stock;
import vn.com.hust.stock.stockmodel.request.StockRequest;

import java.util.List;

public interface StockService {
    Stock createNewStock(Stock stockRe);
    Stock updateStock(Stock stockRe);
    Stock getStockById(Long id);
    void deleteById(Long id);
    Stock getStockByCode(String  name);
    List<Stock> filterStockBy(StockRequest stockRequest);
    List<String> search(String sym);
    void importData(String sym);

}
