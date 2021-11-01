package vn.com.hust.stock.stockapp.serviceImpl;


import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import vn.com.hust.stock.stockapp.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.entity.QPriceHistory;

import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;

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
    private static int DAY = 30;
    private static int RE_DAY = 15;

     @Autowired
     private Map<String, List<String>> STOCK_MAPS;

     @Autowired
     private List<String> STOCK_ARRAYS;


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


            scheduledExecutor.execute(() -> priceSimplePriceSymbol(priceHistoryList, 1000000, 0.07));
        }

    }

    @Override
    public List<PriceHistory> calculateSimplePrice(PriceHistoryRequest priceHistoryRequest) {
        List<PriceHistory> priceHistories = new ArrayList<>();
        LocalDate fromTime = priceHistoryRequest.getFromTime();
        for (String symbol : priceHistoryRequest.getSymbol()) {
            List<PriceHistory> priceHistoryList = new ArrayList<>();
            List<PriceHistory> priceHistories1 = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest, symbol));
            for (int i = 0; i < priceHistories1.size(); i++) {
                if (i % priceHistoryRequest.getReDay() == 0) {
                    priceHistoryList.add(priceHistories1.get(i));
                }
            }
            List<PriceHistory> abc = priceSimplePriceSymbol(priceHistoryList, priceHistoryRequest.getMoney(), priceHistoryRequest.getRisk());
            priceHistories.addAll(abc);
        }
        return priceHistories.stream().filter(s -> s.getSimpleReturn() != 0 && s.getVolatility() != 0)
                .sorted(Comparator.comparing(PriceHistory::getTime)).collect(Collectors.toList());

    }


    public List<PriceHistory> priceSimplePriceSymbol(List<PriceHistory> priceHistories, long money, double risk) {

        double cumulativeLog = 1;
        for (int i = 1; i < priceHistories.size(); i++) {
            cumulativeLog = coreCalculatePrice(priceHistories, cumulativeLog, i);
        }
        int numberStock = 0;
        double cash = 0.0d;
        List<Double> simpleReturn = priceHistories.stream().map(p -> p.getSimpleReturn()).collect(Collectors.toList());

        for (int k = 0; k < DAY; k++) {
            simpleReturn.add(0D);
        }
        for (int i = 1; i < priceHistories.size() - DAY; i++) {
            double setVolatility = calculateSD(simpleReturn, i) * Math.sqrt(252);
            if (i > 1) {
                money = (int) (numberStock * priceHistories.get(i + DAY - 1).getClose()*1000 + cash);
            }
            priceHistories.get(i + DAY - 1).setVolatility(setVolatility);
            priceHistories.get(i + DAY - 1).setAnnualisedStandardDeviation(setVolatility / 100);
            priceHistories.get(i + DAY - 1).setTargetWeights(risk / (setVolatility / 100));
            System.out.println(risk *100 / (setVolatility));
            priceHistories.get(i + DAY - 1).setNumberOfSharesWithEquity(money * (risk * 100 / (setVolatility)));
            System.out.println("getNumberOfSharesWithEquity : "+priceHistories.get(i + DAY - 1).getNumberOfSharesWithEquity());
            cash = money - priceHistories.get(i + DAY - 1).getNumberOfSharesWithEquity();
            priceHistories.get(i + DAY - 1).setCash(cash);
            numberStock = (int) ((money * priceHistories.get(i + DAY - 1).getTargetWeights()) /( priceHistories.get(i + DAY - 1).getClose()*1000));
            priceHistories.get(i + DAY - 1).setNumberStock(numberStock);
            priceHistories.get(i + DAY - 1).setPriceStock(numberStock * priceHistories.get(i + DAY - 1).getClose()*1000);
            priceHistories.get(i + DAY - 1).setMoney(money);
        }
        return priceHistories;
    }

    private double coreCalculatePrice(List<PriceHistory> priceHistories, double cumulativeLog, int i) {
        PriceHistory priceHistory = priceHistories.get(i);
        double priceT = priceHistory.getClose();
        double priceT_1 = priceHistories.get(i - 1).getClose();
        double simpleReturn = (priceT - priceT_1) / priceT_1;
        double grossReturn = 1 + simpleReturn;
        double logReturn = Math.log(grossReturn);
        cumulativeLog = cumulativeLog * (1 + logReturn);
        priceHistory.setSimpleReturn(simpleReturn * 100);
        priceHistory.setGrossReturn(grossReturn * 100);
        priceHistory.setLogReturn(logReturn * 100);
        priceHistory.setCumulativeLog(cumulativeLog);
        return cumulativeLog;
    }

    public static double calculateSD(List<Double> numArray, int i) {
        double sum = 0.0, standardDeviation = 0.0;
        int length = DAY;
        for (int j = 0; j < DAY; j++) {
            sum += numArray.get(j + i);
        }
        double mean = sum / length;
        for (int j = 0; j < DAY; j++) {
            standardDeviation += Math.pow(numArray.get(j + i) - mean, 2);
        }
        return Math.sqrt(standardDeviation / length);
    }


    public List<PriceHistory> queryPolicyJoinProduct(Predicate condition) {
        return new JPAQuery<>(em).select(Q_Price)
                .from(Q_Price)
                .where(condition)
                .orderBy(Q_Price.time.asc()).fetch();
    }

    private Predicate conditionPriceRe(PriceHistoryRequest priceRe, String sym) {
        BooleanBuilder condition = new BooleanBuilder();

        if (!StringUtils.isEmpty(priceRe.getSymbol())) {
            condition.and(Q_Price.sym.eq(sym));
        }
        if (priceRe.getFromTime() != null)
            condition.and(Q_Price.time.after(priceRe.getFromTime()));
        if (priceRe.getToTime() != null)
            condition.and(Q_Price.time.before(priceRe.getToTime()));
        return condition;
    }

    @Override
    public List<PriceHistory> loadTest(PriceHistoryRequest priceHistoryRequest) {
        DAY = priceHistoryRequest.getDay();
        List<PriceHistory> priceHistories = new ArrayList<>();
        for (String symbol : priceHistoryRequest.getSymbol()) {

            List<PriceHistory> priceHistoryList = new ArrayList<>();
            List<PriceHistory> priceHistories1 = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest, symbol));
            for (int i = 0; i < priceHistories1.size(); i++) {
                if (i % priceHistoryRequest.getReDay() == 0) {
                    priceHistoryList.add(priceHistories1.get(i));
                }
            }

            List<PriceHistory> abc = priceSimplePriceSymbol(priceHistoryList, priceHistoryRequest.getMoney(), priceHistoryRequest.getRisk());
            priceHistories.addAll(abc);
            for (PriceHistory priceHistory1 : abc) {
                PriceHistory priceHistory = new PriceHistory();
                priceHistory.setSym("CASH");
                priceHistory.setPriceStock(0d);
                priceHistory.setTime(priceHistory1.getTime());
                priceHistory.setPriceStock(priceHistory1.getCash());
                priceHistories.add(priceHistory);
            }
        }
        return priceHistories.stream().filter(p ->p.getMoney() !=0d).collect(Collectors.toList());
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

            double cumulativeLog = 1;
            for (int i = 1; i < priceHistories1.size(); i++) {
                cumulativeLog = coreCalculatePrice(priceHistories1, cumulativeLog, i);
            }
            priceHistoryRepository.saveAll(priceHistories1);
            priceHistories.addAll(priceHistories1);
        }
        return priceHistories;
    }

    @Override
    public List<PriceHistory> histogram(String field, String order) {
        return priceLast(field, order,null);
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
}
