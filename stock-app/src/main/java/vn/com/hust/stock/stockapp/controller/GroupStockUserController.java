package vn.com.hust.stock.stockapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.hust.stock.stockapp.service.GroupStockUserService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/group")
public class GroupStockUserController {
    private final GroupStockUserService groupStockUserService;

    @Autowired
    public GroupStockUserController(GroupStockUserService groupStockUserService) {
        this.groupStockUserService = groupStockUserService;
    }

    @GetMapping
    private ResponseEntity<List<PriceHistory>> groupStockUser(@ModelAttribute PriceHistoryRequest priceRe, HttpServletRequest request){
       List<PriceHistory> priceHistories = groupStockUserService.getListPriceByUser(request,priceRe);
       return new ResponseEntity<>(priceHistories, HttpStatus.OK);
    }
    @PutMapping("/addSymbol")
    private ResponseEntity<List<PriceHistory>> addToGroup(@RequestParam String stock,HttpServletRequest request){
        List<PriceHistory> priceHistories = groupStockUserService.addStock(stock, request);
        return new ResponseEntity<>(priceHistories, HttpStatus.OK);
    }
    @PutMapping("/removeSymbol")
    private ResponseEntity<List<PriceHistory>> removeToGroup(@RequestParam String stock,HttpServletRequest request){
        List<PriceHistory> priceHistories = groupStockUserService.addStock(stock, request);
        return new ResponseEntity<>(priceHistories, HttpStatus.OK);
    }
    @GetMapping("/focus")
    private ResponseEntity<List<String>> focusByUser(HttpServletRequest request){
        List<String> focus = groupStockUserService.focusByUser( request);
        return new ResponseEntity<>(focus, HttpStatus.OK);
    }
}
