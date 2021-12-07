package vn.com.hust.stock.stockapp.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.com.hust.stock.stockapp.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockapp.service.NormalService;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class NormalServiceImpl implements NormalService {

    private List<PriceHistory> lastDataStock = new ArrayList<>();

    private List<PriceHistory> dataGroupHistogram = new ArrayList<>();

    private Map<String,List<PriceHistory>> customIndex = new HashMap<>();

    private PriceHistoryRepository priceHistoryRepository;

    private PriceHistoryService priceHistoryService;

    @Autowired
    public NormalServiceImpl(PriceHistoryRepository priceHistoryRepository,PriceHistoryService priceHistoryService) {
        this.priceHistoryRepository = priceHistoryRepository;
        this.priceHistoryService = priceHistoryService;
    }

    @Override
    public List<PriceHistory> groupHistogram() {
      return dataGroupHistogram;
    }

    @Override
    public List<PriceHistory> priceLast(String field, String order, List<String> syms) {

        if (field.equals("percent") && order.equals("asc")) {
            return lastDataStock.stream().sorted(Comparator.comparing(PriceHistory::getPercent)).limit(100).collect(Collectors.toList());
        } else if (field.equals("percent") && order.equals("desc")) {
            return lastDataStock.stream().sorted(Comparator.comparing(PriceHistory::getPercent).reversed()).limit(100).collect(Collectors.toList());
        } else if (field.equals("simple") && order.equals("asc")) {
            return lastDataStock.stream().sorted(Comparator.comparing(PriceHistory::getSimpleReturn)).limit(100).collect(Collectors.toList());
        } else if (field.equals("simple") && order.equals("desc")) {
            return lastDataStock.stream().sorted(Comparator.comparing(PriceHistory::getSimpleReturn).reversed()).limit(100).collect(Collectors.toList());
        } else if (field.equals("logReturn") && order.equals("asc")) {
            return lastDataStock.stream().sorted(Comparator.comparing(PriceHistory::getLogReturn)).limit(100).collect(Collectors.toList());
        } else if (field.equals("logReturn") && order.equals("desc")) {
            return lastDataStock.stream().sorted(Comparator.comparing(PriceHistory::getLogReturn).reversed()).limit(100).collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public List<PriceHistory> customIndex(String sym) {
        return customIndex.get(sym).stream().sorted(Comparator.comparing(PriceHistory::getTime)
                .reversed()).limit(300).collect(Collectors.toList());
    }

    @Override
    public void resetCache() {
        lastDataStock = new ArrayList<>();
        customIndex = new HashMap<>();

        PriceHistoryRequest request = new PriceHistoryRequest();
        request.setFromTime(LocalDate.of(2000,01,01));
        request.setDay(20);
        request.setReDay(1);
        request.setRisk(0.12);
        request.setMoney(100000);
        request.setSymbol(List.of("VN30"));
        List<PriceHistory> vn30History =  priceHistoryService.calculateSimplePrice(request);
        request.setSymbol(List.of("VNINDEX"));
        List<PriceHistory> vnIndexHistory =  priceHistoryService.calculateSimplePrice(request);
        request.setSymbol(List.of("HNX30"));
        List<PriceHistory> hnx30 = priceHistoryService.calculateSimplePrice(request);
        request.setSymbol(List.of("HASTC"));
        List<PriceHistory> hasTc = priceHistoryService.calculateSimplePrice(request);

        customIndex.put("VN30",vn30History);
        customIndex.put("VNINDEX",vnIndexHistory);
        customIndex.put("HASTC",hasTc);
        customIndex.put("HNX30",hnx30);

        dataGroupHistogram = priceHistoryService.groupHistogram();
        lastDataStock = priceHistoryService.priceLast("percent","asc", new ArrayList<>());
    }
}
