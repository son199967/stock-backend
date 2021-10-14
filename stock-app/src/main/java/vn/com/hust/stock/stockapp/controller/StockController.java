package vn.com.hust.stock.stockapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import vn.com.hust.stock.stockapp.service.StockService;
import vn.com.hust.stock.stockmodel.entity.Stock;
import vn.com.hust.stock.stockmodel.request.StockRequest;

import java.util.List;

@RestController
@RequestMapping("/stock")
public class StockController {
    private final StockService stockService;

    @Autowired
    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/")
    public Stock createNewStock(@RequestBody Stock stockRe){
       return stockService.createNewStock(stockRe);
    }
    @PutMapping("/")
    public Stock updateStock(@RequestBody Stock stockRe){
        return stockService.updateStock(stockRe);
    }
    @GetMapping("/")
    public List<Stock> filterStock(@ModelAttribute StockRequest stockRequest){
        return stockService.filterStockBy(stockRequest);
    }
    @GetMapping("/symbol")
    public List<String> getSymbol(@RequestParam(required = false) String sym){
        return stockService.search(sym);
    }
    @GetMapping("/{id}")
    public Stock getById(@PathVariable Long id){
        return stockService.getStockById(id);
    }
    @DeleteMapping("/{id}")
    public void deleteStockById(@PathVariable Long id){
        stockService.deleteById(id);
    }

    @DeleteMapping("/{code}")
    public void findStockByCode(@PathVariable String code){
        stockService.getStockByCode(code);
    }

}
