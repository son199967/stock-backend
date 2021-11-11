package vn.com.hust.stock.stockapp.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.com.hust.stock.stockapp.repository.GroupStockUserRepository;
import vn.com.hust.stock.stockapp.service.GroupStockUserService;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockapp.service.UserService;
import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;
import vn.com.hust.stock.stockmodel.request.GroupStockUserReqest;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;
import vn.com.hust.stock.stockmodel.response.GroupStockUserResponse;
import vn.com.hust.stock.stockmodel.user.GroupsStockHold;
import vn.com.hust.stock.stockmodel.user.User;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

@Service
public class GroupStockUserServiceImpl implements GroupStockUserService {
    private final UserService userService;
    private final PriceHistoryService priceHistoryService;
    private final GroupStockUserRepository groupStockUserRepository;

    @Autowired
    public GroupStockUserServiceImpl(UserService userService, PriceHistoryService priceHistoryService,GroupStockUserRepository groupStockUserRepository) {
        this.userService = userService;
        this.priceHistoryService = priceHistoryService;
        this.groupStockUserRepository = groupStockUserRepository;
    }

    @Override
    public List<GroupStockUserResponse> getGroupPriceByUser(HttpServletRequest request) {
        User user = userService.whoami(request);
        List<GroupsStockHold> holdStock = user.getGroupsStockHolds();
        if (holdStock == null)
            return  null;
        List<GroupStockUserResponse> groupStockUserResponses = new ArrayList<>();
        for (GroupsStockHold holds: holdStock){
            GroupStockUserResponse groupStock = new GroupStockUserResponse();
            groupStock.setGroupName(holds.getNameGroups());

            PriceHistoryRequest priceHistoryRequest1 = new PriceHistoryRequest();
            priceHistoryRequest1.setSymbol(holds.getSymbols());
            groupStock.setPriceHistoryList(priceHistoryService.calculateSimplePrice(priceHistoryRequest1));
            groupStockUserResponses.add(groupStock);
        }

        return groupStockUserResponses;
    }

    @Override
    public GroupsStockHold addStockToGroup(List<String> syms, Long idGroup, HttpServletRequest httpServletRequest) {
        User user = userService.whoami(httpServletRequest);

        GroupsStockHold groupsStockHold = groupStockUserRepository.findById(idGroup)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_NOT_EXIST));
        if (groupsStockHold.getUser().getId()!=user.getId())
            throw  new BusinessException(ErrorCode.STOCK_EXIST);
        for (String sym: syms){
            if (groupsStockHold.getSymbols().contains(sym))
                continue;
            groupsStockHold.getSymbols().add(sym);
        }
        groupStockUserRepository.save(groupsStockHold);
        return groupsStockHold;
    }

    @Override
    public GroupsStockHold removeStockFromGroup(String sym, Long idGroup, HttpServletRequest httpServletRequest) {
        User user = userService.whoami(httpServletRequest);
        GroupsStockHold groupsStockHold = groupStockUserRepository.findById(idGroup)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_EXIST));
        if (groupsStockHold.getUser().getId()!=user.getId())
            throw  new BusinessException(ErrorCode.STOCK_EXIST);
        if (!groupsStockHold.getSymbols().contains(sym))
            throw  new BusinessException(ErrorCode.STOCK_EXIST);
        groupsStockHold.getSymbols().remove(sym);

       return   groupStockUserRepository.save(groupsStockHold);


    }

    @Override
    public List<GroupsStockHold> focusByUser(HttpServletRequest httpServletRequest) {
        User user = userService.whoami(httpServletRequest);
        List<GroupsStockHold> holdStock = user.getGroupsStockHolds();
        return holdStock;
    }

    @Override
    public List<GroupsStockHold> addGroupUser(GroupStockUserReqest groupStockUserReqest, HttpServletRequest httpServletRequest) {

        User user = userService.whoami(httpServletRequest);

        GroupsStockHold groupsStockHold1 = new GroupsStockHold();
        groupsStockHold1.setSymbols(groupStockUserReqest.getSyms());
        groupsStockHold1.setNameGroups(groupStockUserReqest.getNameGroup());
        groupsStockHold1.setUser(user);
        groupStockUserRepository.save(groupsStockHold1);

        return userService.whoami(httpServletRequest).getGroupsStockHolds();

    }


    @Override
    public void removeGroupUser(Long idGroup, HttpServletRequest httpServletRequest) {
        User user = userService.whoami(httpServletRequest);
        GroupsStockHold groupsStockHold = groupStockUserRepository.findById(idGroup)
                .orElseThrow(() -> new BusinessException(ErrorCode.STOCK_EXIST));
        if (groupsStockHold.getUser().getId()!=user.getId())
            throw  new BusinessException(ErrorCode.STOCK_EXIST);
        groupStockUserRepository.deleteById(idGroup);
    }
}
