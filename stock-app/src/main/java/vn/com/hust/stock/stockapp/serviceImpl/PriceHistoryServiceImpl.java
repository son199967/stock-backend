package vn.com.hust.stock.stockapp.serviceImpl;


import com.mysql.cj.xdevapi.Collection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import javassist.compiler.ast.Symbol;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Collectors;

@Service
public class PriceHistoryServiceImpl implements PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private ScheduledExecutorService scheduledExecutor;
    private static final QPriceHistory Q_Price = QPriceHistory.priceHistory;
    private static int DAY = 30;
    private static int RE_DAY = 1;

    @PersistenceContext
    private EntityManager em;

    private List<String> STOCK_ARRAYS;
    @Autowired
    public PriceHistoryServiceImpl(PriceHistoryRepository priceHistoryRepository,
                                   @Value("${stock.vn100}") String vn100) {
        STOCK_ARRAYS = new ArrayList<>(Arrays.asList(vn100.split(",")));
        this.priceHistoryRepository = priceHistoryRepository;
        new Thread(() -> {
            scheduledExecutor = Executors.newScheduledThreadPool(10);
        }).start();
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
            if (fromTime == null){
                priceHistoryRequest.setFromTime(LocalDate.now().minusMonths(6));
                List<PriceHistory> priceHistories6month = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest, symbol));
                priceHistories.addAll(priceHistories6month);
                continue;
            }
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
        return priceHistories.stream().filter(p ->p.getSimpleReturn()!=0d & p.getVolatility()!=0d).collect(Collectors.toList());

    }

    private void caculatePrice(){
        //        System.out.println("time:" + (System.currentTimeMillis() - time));
//        long time1 = System.currentTimeMillis();
//        for (String symbol : priceHistoryRequest.getSymbol()) {
//            List<PriceHistory> priceHistoryList = new ArrayList<>();
//            List<PriceHistory> priceHistories1 = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest, symbol));
//            for (int i = 0; i < priceHistories1.size(); i++) {
//                if (i % priceHistoryRequest.getReDay() == 0) {
//                    priceHistoryList.add(priceHistories1.get(i));
//                }
//            }
//            List<PriceHistory> abc = new ArrayList<>();
//            try {
//                Future<List<PriceHistory>> future= scheduledExecutor.submit(new PriceCalculate(priceHistories, priceHistoryRequest.getDay(),
//                                priceHistoryRequest.getRisk(), priceHistoryRequest.getMoney()));
//                while (!future.isDone()){
//                    Thread.sleep(300);
//                }
//                abc= future.get();
//            } catch (RuntimeException | InterruptedException | ExecutionException exception) {
//                throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR);
//            }
//            priceHistories.addAll(abc);
//        }
//        System.out.println("time1:" + (System.currentTimeMillis() - time1));
    }

    public List<PriceHistory> priceSimplePriceSymbol(List<PriceHistory> priceHistories, int money, double risk) {

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
                money = (int) (numberStock * priceHistories.get(i + DAY - 1).getClose() + cash);
            }
            priceHistories.get(i + DAY - 1).setVolatility(setVolatility);
            priceHistories.get(i + DAY - 1).setAnnualisedStandardDeviation(setVolatility / 100);
            priceHistories.get(i + DAY - 1).setTargetWeights(risk / (setVolatility / 100));
            priceHistories.get(i + DAY - 1).setNumberOfSharesWithEquity(money * (risk / (setVolatility / 100)));
            cash = money - priceHistories.get(i + DAY - 1).getNumberOfSharesWithEquity();
            priceHistories.get(i + DAY - 1).setCash(cash);
            numberStock = (int) ((money * priceHistories.get(i + DAY - 1).getTargetWeights()) / priceHistories.get(i + DAY - 1).getClose());
            priceHistories.get(i + DAY - 1).setNumberStock(numberStock);
            priceHistories.get(i + DAY - 1).setPriceStock(numberStock * priceHistories.get(i + DAY - 1).getClose());
            priceHistories.get(i + DAY - 1).setMoney(money);
        }
        return priceHistories.stream().filter(p -> p.getSimpleReturn()!=0d& p.getVolatility()!=0d).collect(Collectors.toList());
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
            condition.and(Q_Price.time.after(priceRe.getFromTime().minusDays(DAY)));
        if (priceRe.getToTime() != null)
            condition.and(Q_Price.time.before(priceRe.getToTime()));
        return condition;
    }

    @Override
    public List<PriceHistory> loadtest(PriceHistoryRequest priceHistoryRequest) {
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
        return priceHistories;
    }

    @Override
    public List<PriceHistory> priceLast(String field, String order) {
        LocalDate localDate = new JPAQuery<>(em).select(Q_Price.time).from(Q_Price).orderBy(Q_Price.time.desc()).limit(1).fetch().get(0);
        return queryPolicyJoin(field,order,localDate);

    }

    @Override
    public List<PriceHistory> updateData() {
        List<PriceHistory> priceHistories = new ArrayList<>();
        for (String symbol : STOCK_ARRAYS) {
            PriceHistoryRequest priceHistoryRequest = new PriceHistoryRequest();
            priceHistoryRequest.setSymbol(new ArrayList<>(Arrays.asList(symbol)));
            priceHistoryRequest.setFromTime(LocalDate.now().minusMonths(6));
            priceHistoryRequest.setReDay(RE_DAY);
            List<PriceHistory> priceHistoryList = new ArrayList<>();
            List<PriceHistory> priceHistories1 = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest, symbol));
            if (ObjectUtils.isEmpty(priceHistories1)) continue;
            for (int i = 0; i < priceHistories1.size(); i++) {
                if (i % priceHistoryRequest.getReDay() == 0) {
                    priceHistoryList.add(priceHistories1.get(i));
                }
            }
            List<PriceHistory> simplePriceSymbol = priceSimplePriceSymbol(priceHistoryList, priceHistoryRequest.getMoney(), priceHistoryRequest.getRisk());
            priceHistoryRepository.saveAll(simplePriceSymbol);
            priceHistories.addAll(simplePriceSymbol);
        }
        return priceHistories;
    }

    public List<PriceHistory> queryPolicyJoin(String field, String order, LocalDate localDate) {
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
        } else if (field.equals("volume") && order.equals("asc")) {
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.volume.asc()).fetch();
        } else if (field.equals("volume") && order.equals("asc")) {
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.volume.desc()).fetch();
        }else if (field.equals("simple")&& order.equals("asc")){
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.simpleReturn.asc()).fetch();
        }else if (field.equals("simple") && order.equals("desc")){
            return new JPAQuery<>(em).select(Q_Price)
                    .from(Q_Price)
                    .where(Q_Price.time.eq(localDate))
                    .orderBy(Q_Price.simpleReturn.desc()).fetch();
        }
        return null;
    }
}
