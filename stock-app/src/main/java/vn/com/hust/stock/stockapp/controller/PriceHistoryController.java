package vn.com.hust.stock.stockapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;
import vn.com.hust.stock.stockmodel.response.NormalStrategyResponse;

import java.util.List;
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
    @GetMapping("/updateData")
    public List<PriceHistory> updateData()
    {
        return priceHistoryService.updateData();
    }

    @GetMapping("/calculate")
    public List<PriceHistory> calculateSimplePrice(@ModelAttribute PriceHistoryRequest priceHistoryRe)
    {
            return priceHistoryService.calculateSimplePrice(priceHistoryRe);
    }
    @GetMapping("/volatilityStrategy")
    public List<PriceHistory> volatilityStrategy(@ModelAttribute PriceHistoryRequest priceHistoryRe)
    {
        return priceHistoryService.loadTest(priceHistoryRe);
    }
    @GetMapping("/normalStrategy")
    public List<NormalStrategyResponse> normalStrategy(@ModelAttribute PriceHistoryRequest priceHistoryRe)
    {
        return priceHistoryService.normalStrategy(priceHistoryRe);
    }
    @GetMapping("/pricelast")
    public List<PriceHistory> priceLast(@RequestParam String field ,@RequestParam String order,@RequestParam(required = false) List<String> sym)
    {
        return priceHistoryService.priceLast(field,order,sym);
    }
    @GetMapping("/histogram")
    public List<PriceHistory> histogram()
    {
        return priceHistoryService.histogram("simpleReturn","asc");
    }


    @GetMapping("/groupHistogram")
    public List<PriceHistory> groupHistogram()
    {
        return priceHistoryService.groupHistogram();
    }

    @GetMapping("/groupCommom")
    public List<PriceHistory> groupCommom()
    {
        return priceHistoryService.groupCommon();
    }

}
