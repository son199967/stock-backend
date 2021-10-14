package vn.com.hust.stock.stockapp.service;

import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface GroupStockUserService
{
    List<PriceHistory> getListPriceByUser(HttpServletRequest httpServletRequest, PriceHistoryRequest priceHistoryRequest);
    List<PriceHistory> addStock(String sym,HttpServletRequest httpServletRequest);
    List<PriceHistory> removeStock(String sym,HttpServletRequest httpServletRequest);

    List<String> focusByUser(HttpServletRequest httpServletRequest);
}
