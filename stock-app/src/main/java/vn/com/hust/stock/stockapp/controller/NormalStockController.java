package vn.com.hust.stock.stockapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import vn.com.hust.stock.stockapp.service.NormalService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;

import java.util.List;

@RestController
@RequestMapping("/normal")
public class NormalStockController {

    private NormalService normalService;


    @Autowired
    public NormalStockController(NormalService normalService) {
        this.normalService = normalService;
    }

    @GetMapping("/groupHistogram")
    public List<PriceHistory> groupHistogram()
    {
        return normalService.groupHistogram();
    }

    @GetMapping("/pricelast")
    public List<PriceHistory> priceLast(@RequestParam String field , @RequestParam String order, @RequestParam(required = false) List<String> sym)
    {
        return normalService.priceLast(field,order,sym);
    }
    @GetMapping("/customIndex")
    public List<PriceHistory> customIndex(@RequestParam String sym)
    {
        return normalService.customIndex(sym);
    }
    @GetMapping("/initStock")
    public void initStock()
    {
        normalService.resetCache();
    }
}
