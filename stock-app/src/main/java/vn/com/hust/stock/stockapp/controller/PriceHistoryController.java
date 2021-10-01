package vn.com.hust.stock.stockapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@RestController
@RequestMapping("/price")
public class PriceHistoryController {

    private ScheduledExecutorService scheduledExecutor;
    private PriceHistoryService priceHistoryService;
    @Autowired
    public PriceHistoryController(PriceHistoryService priceHistoryService) {
        this.priceHistoryService = priceHistoryService;
    }
    @PutMapping
    public void calculateSimPleReturn(){
        priceHistoryService.calculateListSimplePrice();
    }
    @GetMapping("")
    public List<PriceHistory> getPriceHistory(@ModelAttribute PriceHistoryRequest priceHistoryRe)
    {
       return priceHistoryService.getPriceHistory(priceHistoryRe);
    }
    @GetMapping("/calculate")
    public List<PriceHistory> calculateSimplePrice(@ModelAttribute PriceHistoryRequest priceHistoryRe)
    {
            return priceHistoryService.calculateSimplePrice(priceHistoryRe);
    }
}
