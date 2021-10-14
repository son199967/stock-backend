package vn.com.hust.stock.stockapp.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.com.hust.stock.stockapp.service.GroupStockUserService;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockapp.service.UserService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;
import vn.com.hust.stock.stockmodel.user.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Service
public class GroupStockUserServiceImpl implements GroupStockUserService {
    private final UserService userService;
    private final PriceHistoryService priceHistoryService;

    @Autowired
    public GroupStockUserServiceImpl(UserService userService, PriceHistoryService priceHistoryService) {
        this.userService = userService;
        this.priceHistoryService = priceHistoryService;
    }


    @Override
    public List<PriceHistory> getListPriceByUser(HttpServletRequest request, PriceHistoryRequest priceHistoryRequest) {
        User user = userService.whoami(request);
        List<String> holdStock = user.getStockHole();
            if (holdStock.isEmpty()) return null;
        priceHistoryRequest.setSymbol(holdStock);
        return priceHistoryService.calculateSimplePrice(priceHistoryRequest);
    }

    @Override
    public List<PriceHistory> addStock(String sym, HttpServletRequest httpServletRequest) {
        User user = userService.addSymbolsWithUser(sym,httpServletRequest);

        PriceHistoryRequest priceHistoryRequest = new PriceHistoryRequest();
        priceHistoryRequest.setSymbol(user.getStockHole());
        return priceHistoryService.calculateSimplePrice(priceHistoryRequest);
    }

    @Override
    public List<PriceHistory> removeStock(String sym, HttpServletRequest httpServletRequest) {
        User user = userService.removeSymbolsWithUser(sym,httpServletRequest);

        PriceHistoryRequest priceHistoryRequest = new PriceHistoryRequest();
        priceHistoryRequest.setSymbol(user.getStockHole());
        return priceHistoryService.calculateSimplePrice(priceHistoryRequest);
    }

    @Override
    public List<String> focusByUser(HttpServletRequest httpServletRequest) {
        User user = userService.whoami(httpServletRequest);
        List<String> holdStock = user.getStockHole();
        return holdStock;
    }
}
