package vn.com.hust.stock.stockapp.service;

import vn.com.hust.stock.stockmodel.request.GroupStockUserReqest;
import vn.com.hust.stock.stockmodel.response.GroupStockUserResponse;
import vn.com.hust.stock.stockmodel.user.GroupsStockHold;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface GroupStockUserService
{
    List<GroupStockUserResponse> getGroupPriceByUser(HttpServletRequest httpServletRequest);
    GroupsStockHold addStockToGroup(List<String> sym , Long idGroup, HttpServletRequest httpServletRequest);
    GroupsStockHold removeStockFromGroup(String sym, Long idGroup, HttpServletRequest httpServletRequest);

    List<GroupsStockHold> focusByUser(HttpServletRequest httpServletRequest);

    List<GroupsStockHold> addGroupUser(GroupStockUserReqest groupStockUserReqest, HttpServletRequest httpServletRequest);

    void removeGroupUser(Long idGroup, HttpServletRequest httpServletRequest);

}
