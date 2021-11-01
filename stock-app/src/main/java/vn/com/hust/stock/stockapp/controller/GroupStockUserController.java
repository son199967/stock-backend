package vn.com.hust.stock.stockapp.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.hust.stock.stockapp.service.GroupStockUserService;

import vn.com.hust.stock.stockmodel.request.GroupStockUserReqest;

import vn.com.hust.stock.stockmodel.request.SymbolsAddRequest;
import vn.com.hust.stock.stockmodel.response.GroupStockUserResponse;
import vn.com.hust.stock.stockmodel.user.GroupsStockHold;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/group")
public class GroupStockUserController {
    private final GroupStockUserService groupStockUserService;

    @Autowired
    public GroupStockUserController(GroupStockUserService groupStockUserService) {
        this.groupStockUserService = groupStockUserService;
    }

    @GetMapping
    private ResponseEntity<List<GroupStockUserResponse>> groupStockUser(HttpServletRequest request){
       List<GroupStockUserResponse> priceHistories = groupStockUserService.getGroupPriceByUser(request);
       return new ResponseEntity<>(priceHistories, HttpStatus.OK);
    }

    @PutMapping("/addSymbol")
    private ResponseEntity addSymToGroup(@RequestBody SymbolsAddRequest symbols, HttpServletRequest request){
        GroupsStockHold groupsStockHold = groupStockUserService.addStockToGroup(symbols.getSymbols(),symbols.getIdGroup(), request);
        return new ResponseEntity(groupsStockHold, HttpStatus.OK);
    }
    @DeleteMapping("/removeSymbol")
    private ResponseEntity removeSymFromGroup(
            @RequestParam String stock,
            @RequestParam Long idGroup,
            HttpServletRequest request){
        GroupsStockHold groupsStockHold = groupStockUserService.removeStockFromGroup(stock,idGroup, request);
        return new ResponseEntity(groupsStockHold, HttpStatus.OK);
    }

    @PostMapping("/addGroupUser")
    private ResponseEntity addGroupUser(
            @RequestBody GroupStockUserReqest reqest,
            HttpServletRequest request){
        List<GroupsStockHold> groupsStockHolds = groupStockUserService.addGroupUser(reqest, request);
        return new ResponseEntity(groupsStockHolds, HttpStatus.OK);
    }

    @DeleteMapping("/removeGroupUser")
    private ResponseEntity removeGroupUser(
            @RequestParam Long idGroup,
            HttpServletRequest request){
         groupStockUserService.removeGroupUser(idGroup, request);
        return new ResponseEntity( HttpStatus.OK);
    }
    @GetMapping("/focus")
    private ResponseEntity<List<GroupsStockHold>> focusByUser(HttpServletRequest request){
        List<GroupsStockHold> focus = groupStockUserService.focusByUser(request);
        return new ResponseEntity(focus, HttpStatus.OK);
    }
}
