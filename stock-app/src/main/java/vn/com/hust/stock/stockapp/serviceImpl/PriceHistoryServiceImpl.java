package vn.com.hust.stock.stockapp.serviceImpl;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import vn.com.hust.stock.stockapp.Job.ImportDataProcess;
import vn.com.hust.stock.stockapp.config.GroupsStock;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PriceHistoryServiceImpl implements PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private ScheduledExecutorService scheduledExecutor;
    private static final QPriceHistory Q_Price = QPriceHistory.priceHistory;




    private Map<String, List<String>> STOCK_MAPS;
    private List<String> STOCK_ARRAYS;

    private final int NORMAL_DAY = 30;
    private final int RE_DAY = 1;


    @PersistenceContext
    private EntityManager em;

    @Autowired
    public PriceHistoryServiceImpl(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
        scheduledExecutor = Executors.newScheduledThreadPool(10);
        STOCK_ARRAYS = priceHistoryRepository.findSymGroup();
        STOCK_MAPS = new GroupsStock().STOCK_MAPS();
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
        request.setAllData(true);
        List<PriceHistory> priceHistories = new ArrayList<>();
        LocalDate fromTime = request.getFromTime();
        if (request.getReDay() == 0) {
            request.setReDay(RE_DAY);
        }
        for (String symbol : request.getSymbol()) {
            List<PriceHistory> priceHistoryListRe = new ArrayList<>();
            List<PriceHistory> priceHistories1 = queryPolicyJoinProduct(conditionPriceRe(request, symbol));
            for (int i = 0; i < priceHistories1.size(); i++) {
                if (i % request.getReDay() == 0) {
                    priceHistoryListRe.add(priceHistories1.get(i));
                }
            }
            PriceHistory priceHistory = priceHistoryListRe.stream().filter(p -> p.getTime().isAfter(request.getFromTime())).collect(Collectors.toList()).get(0);
            int indexOf = priceHistoryListRe.indexOf(priceHistory);
            int startList = 0;
            if (indexOf>request.getDay()){
                startList = indexOf -request.getDay();
            }
            List<PriceHistory> priceHistoryList = priceHistoryListRe.subList(startList,priceHistoryListRe.size());
            List<PriceHistory> abc = priceSimplePriceSymbol(priceHistoryList, request.getMoney(), request.getRisk(), request.isAllData(), request.getDay());
            priceHistories.addAll(abc);
        }
        if (request.isAllData()){
            return priceHistories.stream().filter(s -> s.getSimpleReturn() != 0 && s.getVolatility() != 0)
                    .sorted(Comparator.comparing(PriceHistory::getTime)).collect(Collectors.toList());
        }
        return priceHistories.stream().filter(s -> s.getSimpleReturn() != 0 )
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
            priceRe.setDay(1000);
        }
        if (!StringUtils.isEmpty(priceRe.getSymbol())) {
            condition.and(Q_Price.sym.eq(sym));
        }
        if (priceRe.getFromTime() != null)
            condition.and(Q_Price.time.after(priceRe.getFromTime().minusDays(priceRe.getDay() )));
        if (priceRe.getToTime() != null)
            condition.and(Q_Price.time.before(priceRe.getToTime()));
        return condition;
    }

    @Override
    public List<VolatilityStrategyResponse> volatilityStrategy(PriceHistoryRequest request) {
        request.setReDay(request.getReDay()==0?RE_DAY:request.getReDay());
        Map<LocalDate,List<CorePrice>> map = new TreeMap<>();
        Map<String,List<PriceHistory>> mapStock = new HashMap<>();
        List<LocalDate> dateTrade = new ArrayList<>();
        List<CorePrice> priceHistories = new ArrayList<>();
        String stockMaxTime = null;
        int dateTradeMaxStock = 0;
        for (String symbol : request.getSymbol()) {
            List<PriceHistory> priceHistories1 = new ArrayList<>();

            List<PriceHistory> priceHistorieDatas =      priceHistoryRepository.findAllBySymOrderByTimeAsc(symbol);
            int indexOfLocalDate = priceHistorieDatas.indexOf(priceHistorieDatas.stream().filter(p->p.getTime().isAfter(request.getFromTime()))
                    .findFirst().orElseThrow(()->new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)));
            if (indexOfLocalDate-request.getDay()*request.getReDay()<0){
                int day = indexOfLocalDate/request.getDay();
                request.setDay(day);
            }
            priceHistories1 = priceHistorieDatas.subList(indexOfLocalDate-request.getDay()*request.getReDay(),priceHistorieDatas.size()-1);

            if (dateTradeMaxStock <priceHistories1.size()) {
                dateTradeMaxStock = priceHistories1.size();
                stockMaxTime  = symbol;
            }
            mapStock.put(symbol,priceHistories1);
        }
        List<PriceHistory> priceHistoriesMaxStock = mapStock.get(stockMaxTime);
        for (int j=0;j<dateTradeMaxStock;j++){
           if (j% request.getReDay()==0){
              dateTrade.add(priceHistoriesMaxStock.get(j).getTime());
           }
        }
       for (Map.Entry<String,List<PriceHistory>> price : mapStock.entrySet()){
           List<PriceHistory> priceHistoryList = price.getValue().stream()
                   .filter(p-> dateTrade.contains(p.getTime())).collect(Collectors.toList());
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
        List<VolatilityStrategyResponse> responses =  volatilityStrategy.volatilityStrategyResponse();
        return responses.stream().filter(s->s.getTime().isAfter(request.getFromTime())).collect(Collectors.toList());
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

        STOCK_ARRAYS.stream().sorted(Comparator.comparing(String::length).reversed()).forEach( symbol ->{
           startUpddateDataSymbol(symbol);
        });
        return null;
    }

    private void startUpddateDataSymbol(String symbol) {
        log.info("Start update sym stock {}", symbol);
        PriceHistoryRequest priceHistoryRequest = new PriceHistoryRequest();
        priceHistoryRequest.setSymbol(new ArrayList<>(Arrays.asList(symbol)));
        List<PriceHistory> priceHistories1 = priceHistoryRepository.findAllBySymOrderByTimeAsc(symbol);
        if (ObjectUtils.isEmpty(priceHistories1)) return;
        double cumulativeLog = 1;
        for (int i = 1; i < priceHistories1.size(); i++) {
            CorePrice calculatePrice = new CorePrice(priceHistories1.get(i - 1), priceHistories1.get(i), NORMAL_DAY);
            cumulativeLog = calculatePrice.getCumulativeLogReturn();
            priceHistoryRepository.save(calculatePrice.getPriceHistory());
        }
        return;
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
        return priceLast("simple", "asc", stocks);
    }

    @Override
    public List<PriceHistory> groupCommon() {
        List<String> stocks = STOCK_MAPS.get("COMMON");
        return priceLast("simple", "asc", stocks);
    }

    @Override
    public List<NormalStrategyResponse> normalStrategy(PriceHistoryRequest request) {
        double totalPercent = request.getPercent().stream().reduce(0D, Double::sum);
        if (request.getPercent().stream().reduce(0D, Double::sum) != 1D) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
        Map<String,Double> map = new HashMap<>();
        for (int k=0;k<request.getSymbol().size();k++){
            map.put(request.getSymbol().get(k),request.getPercent().get(k));
        }
        List<NormalStrategyResponse> responses = new ArrayList<>();
        Map<LocalDate, Long[]> money = new TreeMap<>();
        long moneyDevice = request.getMoney();
        for (int i = 0; i < request.getSymbol().size(); i++) {
            List<PriceHistory> priceHistories1 =
                    queryPolicyJoinProduct(conditionPriceRe(request, request.getSymbol().get(i)))
                    .stream().filter(s-> s.getTime().isAfter(request.getFromTime())).collect(Collectors.toList());
            if (ObjectUtils.isEmpty(priceHistories1)) {
                return null;
            }
            int numStocks = (int) ((request.getMoney() * request.getPercent().get(i)) / (priceHistories1.get(0).getClose() * 1000));
            moneyDevice -= request.getMoney()*request.getPercent().get(i);
            log.info("stock {}, percent {},start price 1 {} , to price {}",request.getSymbol().get(i),
                    request.getPercent().get(i),
                    priceHistories1.get(0).getClose(),
                    priceHistories1.get(priceHistories1.size()-1).getClose());
            for (PriceHistory priceHistory : priceHistories1) {
                 long nowMoney = (long) (numStocks * priceHistory.getClose() * 1000);
                if (money.containsKey(priceHistory.getTime())) {
                    long moneyTime = money.get(priceHistory.getTime())[0] + nowMoney;
                    long moneyNotUse = money.get(priceHistory.getTime())[1] - nowMoney;
                    money.put(priceHistory.getTime(), new Long[]{moneyTime, moneyNotUse});
                } else {
                    money.put(priceHistory.getTime(), new Long[]{nowMoney, moneyDevice});
                }
            }
        }
        for (Map.Entry<LocalDate, Long[]> data : money.entrySet()) {
            NormalStrategyResponse normal = new NormalStrategyResponse();
            normal.setSymbol("Money");
            normal.setTime(data.getKey());
            normal.setMoney(data.getValue()[0] + (data.getValue()[1]));
            responses.add(normal);
        }
        return responses.stream().filter(s -> s.getSymbol().equals("Money") ).collect(Collectors.toList());
    }

    @Override
    public List<Map<String, Double>> indexGroup(String sym) {
        Map<String,Double> map = new HashMap<>();
        Map<String,Double> mapRisk = new HashMap<>();
        int year = LocalDate.now().getYear();
        LocalDate returnYear = LocalDate.of(year,1,1);
        LocalDate returnBeforeYear= LocalDate.of(year-1,1,1);
        LocalDate return1BeforeYear = LocalDate.of(year,1,1);



        PriceHistoryRequest priceHistoryRequest = new PriceHistoryRequest();
        priceHistoryRequest.setSymbol(new ArrayList<>(Arrays.asList(sym)));
        priceHistoryRequest.setFromTime(return1BeforeYear);
        List<PriceHistory> priceHistories = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest, sym));
        LocalDate timeNow = priceHistories.get(priceHistories.size()-1).getTime();
        LocalDate return3BeforeMonth = timeNow.minusMonths(3);
        LocalDate return1BeforeMonth = timeNow.minusMonths(1);

        Double maxBeforeReturnYear = priceHistories.stream().max(Comparator.comparing(PriceHistory::getSimpleReturn))
                .orElseThrow(()->new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)).getSimpleReturn();
        Double minBeforeReturnYear = priceHistories.stream().min(Comparator.comparing(PriceHistory::getSimpleReturn))
                .orElseThrow(()->new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)).getSimpleReturn();
        Double avgBeforeReturnYear = priceHistories.stream().mapToDouble(PriceHistory::getSimpleReturn).average().getAsDouble();

        Double maxReturnYear = priceHistories.stream().filter(p->p.getTime().isAfter(returnYear)).max(Comparator.comparing(PriceHistory::getSimpleReturn))
                .orElseThrow(()->new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)).getSimpleReturn();
        Double minReturnYear = priceHistories.stream().filter(p->p.getTime().isAfter(returnYear)).min(Comparator.comparing(PriceHistory::getSimpleReturn))
                .orElseThrow(()->new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)).getSimpleReturn();
        Double avgReturnYear = priceHistories.stream().filter(p->p.getTime().isAfter(returnYear)).mapToDouble(PriceHistory::getSimpleReturn).average().getAsDouble();

        Double maxReturn3Month = priceHistories.stream().filter(p->p.getTime().isAfter(return3BeforeMonth)).max(Comparator.comparing(PriceHistory::getSimpleReturn))
                .orElseThrow(()->new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)).getSimpleReturn();
        Double minReturn3Month = priceHistories.stream().filter(p->p.getTime().isAfter(return3BeforeMonth)).min(Comparator.comparing(PriceHistory::getSimpleReturn))
                .orElseThrow(()->new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)).getSimpleReturn();
        Double avgReturn3Month = priceHistories.stream().filter(p->p.getTime().isAfter(return3BeforeMonth)).mapToDouble(PriceHistory::getSimpleReturn).average().getAsDouble();

        Double maxReturn1Month = priceHistories.stream().filter(p->p.getTime().isAfter(return1BeforeMonth)).max(Comparator.comparing(PriceHistory::getSimpleReturn))
                .orElseThrow(()->new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)).getSimpleReturn();
        Double minReturn1Month = priceHistories.stream().filter(p->p.getTime().isAfter(return1BeforeMonth)).min(Comparator.comparing(PriceHistory::getSimpleReturn))
                .orElseThrow(()->new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR)).getSimpleReturn();
        Double avgReturn1Month = priceHistories.stream().filter(p->p.getTime().isAfter(return1BeforeMonth)).mapToDouble(PriceHistory::getSimpleReturn).average().getAsDouble();

        List<PriceHistory> priceHistoryList =  priceSimplePriceSymbol(priceHistories,10000000,0.15,true,30);



        map.put("Avg "+year+ " Return (%)",avgReturnYear);
        map.put("Avg "+(year-1)+ " Return (%)",avgBeforeReturnYear);
        map.put("Avg Last 3 Months (%)",avgReturn3Month);
        map.put("Avg Last 1 Months (%)",avgReturnYear);
        map.put("Best Monthly Return (%)",maxReturn1Month);
        map.put("Worst Monthly Return (%)",minReturn1Month);
        map.put("Best "+year+" Return (%)",maxReturnYear);
        map.put("Worst "+year+" Return (%)",minReturnYear);
        map.put("Best "+(year-1)+" Return (%)",maxBeforeReturnYear);
        map.put("Worst "+(year-1)+" Year Return (%)",minBeforeReturnYear);
        PriceHistory last = priceHistoryList.get(priceHistoryList.size()-1);
        mapRisk.put("Annualised Standard Deviation (%)",last.getAnnualisedStandardDeviation());
        mapRisk.put("Volatility (%)",last.getVolatility());
        mapRisk.put("Sharpe Ratio",last.getSharpe());
        mapRisk.put("Simple Return",last.getSimpleReturn());
        mapRisk.put("Log Return",last.getLogReturn());
        mapRisk.put("Gross Return",last.getGrossReturn());
        mapRisk.put("Cumulative Log",last.getCumulativeLog());

        return new ArrayList<>(Arrays.asList(map,mapRisk));
    }
}
