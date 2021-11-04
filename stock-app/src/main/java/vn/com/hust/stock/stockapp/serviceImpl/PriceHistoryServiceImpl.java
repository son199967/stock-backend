package vn.com.hust.stock.stockapp.serviceImpl;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import vn.com.hust.stock.stockapp.core.CorePrice;
import vn.com.hust.stock.stockapp.core.VolatilityStrategy;
import vn.com.hust.stock.stockapp.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.entity.QPriceHistory;

import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;
import vn.com.hust.stock.stockmodel.response.NormalStrategyResponse;
import vn.com.hust.stock.stockmodel.response.VolatilityStrategyResponse;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PriceHistoryServiceImpl implements PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private ScheduledExecutorService scheduledExecutor;
    private static final QPriceHistory Q_Price = QPriceHistory.priceHistory;


    @Autowired
    private Map<String, List<String>> STOCK_MAPS;

    @Autowired
    private List<String> STOCK_ARRAYS;

    private final int NORMAL_DAY = 30;
    private final int RE_DAY = 1;


    @PersistenceContext
    private EntityManager em;

    @Autowired
    public PriceHistoryServiceImpl(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
    }

    @Override
    public List<PriceHistory> getPriceHistory(PriceHistoryRequest priceHistoryRequest) {
        return queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest, priceHistoryRequest.getSymbol().get(0)));
    }

    @Override
    public void calculateListSimplePrice() {
        PriceHistoryRequest priceHistoryRequest = new PriceHistoryRequest();


        for (String stock : STOCK_ARRAYS) {
            priceHistoryRequest.setSymbol(new ArrayList<>(Arrays.asList(stock)));
            List<PriceHistory> priceHistoryList = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest, stock));
            scheduledExecutor.execute(() -> priceSimplePriceSymbol(priceHistoryList, 1000000, 0.07, true, NORMAL_DAY));
        }

    }

    @Override
    public List<PriceHistory> calculateSimplePrice(PriceHistoryRequest request) {
        List<PriceHistory> priceHistories = new ArrayList<>();
        LocalDate fromTime = request.getFromTime();
        if (request.getReDay() == 0) {
            request.setReDay(RE_DAY);
        }
        for (String symbol : request.getSymbol()) {
            List<PriceHistory> priceHistoryList = new ArrayList<>();
            List<PriceHistory> priceHistories1 = queryPolicyJoinProduct(conditionPriceRe(request, symbol));
            for (int i = 0; i < priceHistories1.size(); i++) {
                if (i % request.getReDay() == 0) {
                    priceHistoryList.add(priceHistories1.get(i));
                }
            }
            List<PriceHistory> abc = priceSimplePriceSymbol(priceHistoryList, request.getMoney(), request.getRisk(), request.isAllData(), request.getDay());
            priceHistories.addAll(abc);
        }
        return priceHistories.stream().filter(s -> s.getSimpleReturn() != 0 && s.getVolatility() != 0)
                .sorted(Comparator.comparing(PriceHistory::getTime)).collect(Collectors.toList());

    }



    public List<PriceHistory> priceSimplePriceSymbol(List<PriceHistory> priceHistories, long money, double risk, boolean isAllData, int Day) {
        int DAY = Day == 0 ? NORMAL_DAY : Day;
        double cumulativeLog = 1;
        List<CorePrice> calculatePriceList = new ArrayList<>();
        for (int i = 1; i < priceHistories.size(); i++) {
            CorePrice calculatePrice = new CorePrice(priceHistories.get(i - 1), priceHistories.get(i), cumulativeLog);
            calculatePriceList.add(calculatePrice);
            cumulativeLog = calculatePrice.getCumulativeLogReturn();
        }
        if (isAllData) {
            List<Double> simpleReturn = priceHistories.stream().map(p -> p.getSimpleReturn()).collect(Collectors.toList());

            for (int i = DAY + 1; i < calculatePriceList.size(); i++) {
                if (i == DAY + 1) {
                    calculatePriceList.get(i).calculateHigh(simpleReturn.subList(i - DAY, i), DAY, risk, money, true);
                } else {
                    calculatePriceList.get(i).calculateHigh(simpleReturn.subList(i - DAY, i), DAY, risk, money, false);
                }
            }
        }
        List<PriceHistory> returnPrices = calculatePriceList.stream().map(c -> c.getPriceHistory()).collect(Collectors.toList());
        if (returnPrices.size() < DAY) {
            return returnPrices;
        }
        return returnPrices.subList(DAY, returnPrices.size() - 1);
    }


    public List<PriceHistory> queryPolicyJoinProduct(Predicate condition) {
        return new JPAQuery<>(em).select(Q_Price)
                .from(Q_Price)
                .where(condition)
                .orderBy(Q_Price.time.asc()).fetch();
    }

    private Predicate conditionPriceRe(PriceHistoryRequest priceRe, String sym) {
        BooleanBuilder condition = new BooleanBuilder();
        if (priceRe.getDay() == 0) {
            priceRe.setDay(30);
        }
        if (!StringUtils.isEmpty(priceRe.getSymbol())) {
            condition.and(Q_Price.sym.eq(sym));
        }
        if (priceRe.getFromTime() != null)
            condition.and(Q_Price.time.after(priceRe.getFromTime().minusDays(priceRe.getDay() + 10)));
        if (priceRe.getToTime() != null)
            condition.and(Q_Price.time.before(priceRe.getToTime()));
        return condition;
    }

    @Override
    public List<VolatilityStrategyResponse> volatilityStrategy(PriceHistoryRequest request) {
        request.setReDay(request.getReDay()==0?RE_DAY:request.getReDay());
        Map<LocalDate,List<CorePrice>> map = new TreeMap<>();
        List<CorePrice> priceHistories = new ArrayList<>();
        for (String symbol : request.getSymbol()) {
            List<PriceHistory> priceHistoryList = new ArrayList<>();
            List<PriceHistory> priceHistories1 = queryPolicyJoinProduct(conditionPriceRe(request, symbol));
            for (int i = 0; i < priceHistories1.size(); i++) {
                if (i % request.getReDay() == 0) {
                    priceHistoryList.add(priceHistories1.get(i));
                }
            }
            List<CorePrice> data = priceVolatility(priceHistoryList, request.getMoney(), request.getRisk(), request.getDay());
            priceHistories.addAll(data);
        }
        for (CorePrice c : priceHistories){
            if (map.containsKey(c.getPriceHistory().getTime())) {
                map.get(c.getPriceHistory().getTime()).add(c);
            } else {
                List<CorePrice> corePrices = new ArrayList<>();
                corePrices.add(c);
                map.put(c.getPriceHistory().getTime(), corePrices);
            }
        }
        VolatilityStrategy volatilityStrategy = new VolatilityStrategy(map,request.getMoney());
        return volatilityStrategy.volatilityStrategyResponse();

    }
    public  List<CorePrice>  priceVolatility(List<PriceHistory> priceHistories, long money, double risk, int Day) {
        int DAY = Day == 0 ? NORMAL_DAY : Day;
        double cumulativeLog = 1;
        List<CorePrice> calculatePriceList = new ArrayList<>();
        for (int i = 1; i < priceHistories.size(); i++) {
            CorePrice calculatePrice = new CorePrice(priceHistories.get(i - 1), priceHistories.get(i),cumulativeLog);
            calculatePriceList.add(calculatePrice);
            cumulativeLog = calculatePrice.getCumulativeLogReturn();
        }
        List<Double> simpleReturn = priceHistories.stream().map(p -> p.getSimpleReturn()).collect(Collectors.toList());
        for (int i = DAY + 1; i < calculatePriceList.size(); i++) {
            if (i == DAY + 1) {
                calculatePriceList.get(i).volatilityStrategy(simpleReturn.subList(i - DAY, i), DAY, risk);
            } else {
                calculatePriceList.get(i).volatilityStrategy(simpleReturn.subList(i - DAY, i), DAY, risk);
            }
        }
        return calculatePriceList;
    }

    @Override
    public List<PriceHistory> priceLast(String field, String order, List<String> syms) {
        LocalDate localDate = new JPAQuery<>(em).select(Q_Price.time).from(Q_Price).orderBy(Q_Price.time.desc()).limit(1).fetch().get(0);
        return queryPolicyJoin(field, order, localDate, syms);
    }

    @Override
    public List<PriceHistory> updateData() {
        List<PriceHistory> priceHistories = new ArrayList<>();

        for (String symbol : STOCK_ARRAYS) {

            PriceHistoryRequest priceHistoryRequest = new PriceHistoryRequest();
            priceHistoryRequest.setSymbol(new ArrayList<>(Arrays.asList(symbol)));

            List<PriceHistory> priceHistories1 = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest, symbol));
            if (ObjectUtils.isEmpty(priceHistories1)) continue;
            List<PriceHistory> dataSave = new ArrayList<>();
            double cumulativeLog = 1;
            for (int i = 1; i < priceHistories1.size(); i++) {
                CorePrice calculatePrice = new CorePrice(priceHistories.get(i - 1), priceHistories.get(i), NORMAL_DAY);
                cumulativeLog = calculatePrice.getCumulativeLogReturn();
                dataSave.add(calculatePrice.getPriceHistory());
            }
            priceHistoryRepository.saveAll(dataSave);
            priceHistories.addAll(dataSave);
        }
        return priceHistories;
    }

    @Override
    public List<PriceHistory> histogram(String field, String order) {
        return priceLast(field, order, null);
    }

    public List<PriceHistory> queryPolicyJoin(String field, String order, LocalDate localDate, List<String> symbols) {
        if (!ObjectUtils.isEmpty(symbols)) {
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate).and(Q_Price.sym.in(symbols)))
                    .orderBy(Q_Price.percent.asc()).fetch();
        }
        if (field.equals("percent") && order.equals("asc")) {
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.percent.asc()).fetch();
        } else if (field.equals("percent") && order.equals("desc")) {
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.percent.desc()).fetch();
        } else if (field.equals("simple") && order.equals("asc")) {
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.simpleReturn.asc()).fetch();
        } else if (field.equals("simple") && order.equals("desc")) {
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.simpleReturn.desc()).fetch();
        } else if (field.equals("logReturn") && order.equals("asc")) {
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.logReturn.asc()).fetch();
        } else if (field.equals("logReturn") && order.equals("desc")) {
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.logReturn.desc()).fetch();
        }
        return null;
    }


    @Override
    public List<PriceHistory> groupHistogram() {
        List<String> stocks = STOCK_MAPS.get("GROUPS");
        return priceLast(null, null, stocks);
    }

    @Override
    public List<PriceHistory> groupCommon() {
        List<String> stocks = STOCK_MAPS.get("COMMON");
        return priceLast(null, null, stocks);
    }

    @Override
    public List<NormalStrategyResponse> normalStrategy(PriceHistoryRequest request) {
        double totalPercent = request.getPercent().stream().reduce(0D, Double::sum);
        if (request.getPercent().stream().reduce(0D, Double::sum) != 1D) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        List<NormalStrategyResponse> responses = new ArrayList<>();
        Map<LocalDate, Long[]> money = new TreeMap<>();
        long hasUse = request.getMoney();
        for (int i = 0; i < request.getSymbol().size(); i++) {
            List<PriceHistory> priceHistories1 = queryPolicyJoinProduct(conditionPriceRe(request, request.getSymbol().get(i)));
            if (ObjectUtils.isEmpty(priceHistories1)) {
                return null;
            }
            int numStocks = (int) ((request.getMoney() * request.getPercent().get(i)) / (priceHistories1.get(0).getClose() * 1000));
            hasUse -= (long) (numStocks * priceHistories1.get(0).getClose() * 1000);
            for (PriceHistory priceHistory : priceHistories1) {
                NormalStrategyResponse normal = new NormalStrategyResponse();
                normal.setTime(priceHistory.getTime());
                normal.setSymbol(request.getSymbol().get(i));
                normal.setMoney((long) (numStocks * priceHistory.getClose() * 1000));
                responses.add(normal);
                if (money.containsKey(normal.getTime())) {
                    long moneyTime = money.get(normal.getTime())[0] + normal.getMoney();
                    long moneyNotUse = money.get(normal.getTime())[1] - normal.getMoney();
                    money.put(normal.getTime(), new Long[]{moneyTime, moneyNotUse});
                } else {
                    long moneyNotUse = request.getMoney() - normal.getMoney();
                    money.put(normal.getTime(), new Long[]{normal.getMoney(), moneyNotUse});
                }
            }
        }
        for (Map.Entry<LocalDate, Long[]> data : money.entrySet()) {
            NormalStrategyResponse normal = new NormalStrategyResponse();
            normal.setSymbol("Money");
            normal.setTime(data.getKey());
            normal.setMoney(data.getValue()[0] + (hasUse));
            responses.add(normal);
        }
        return responses.stream().filter(s -> s.getSymbol().equals("Money")).collect(Collectors.toList());
    }
}
