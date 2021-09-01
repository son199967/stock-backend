package vn.com.hust.stock.stockapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;

import java.util.List;

@RestController
@RequestMapping("/price")
public class PriceHistoryController {
    private PriceHistoryService priceHistoryService;
    @Autowired
    public PriceHistoryController(PriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }
    @GetMapping
    public List<PriceHistory> getPriceHistory(@ModelAttribute PriceHistoryRequest priceHistoryRequest){
       return priceHistoryService.getPriceHistory(priceHistoryRequest);
    }
}
