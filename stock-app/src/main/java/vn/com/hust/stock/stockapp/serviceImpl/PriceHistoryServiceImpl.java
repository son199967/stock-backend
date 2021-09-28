package vn.com.hust.stock.stockapp.serviceImpl;


import com.mysql.cj.xdevapi.Collection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import org.springframework.util.StringUtils;
import vn.com.hust.stock.stockapp.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.entity.QPriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;
import vn.com.hust.stock.stockmodel.specification.PriceHistorySpecifications;

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
    private static int DAY= 59;


    @PersistenceContext
    private EntityManager em;

    private List<String> STOCK_ARRAYS = new ArrayList<>(Arrays.asList("ACB", "BID",
            "BVH", "CTG", "FPT", "GAS", "GVR", "HDB", "HPG", "KDH", "MBB", "MSN", "MWG", "NVL", "PDR", "PLX"
            , "PNJ", "POW", "SAB", "SSI", "STB", "TCB", "TPB", "VCB", "VHM", "VIC", "VJC", "VNM", "VPB", "VRE", "REE"));

    @Autowired
    public PriceHistoryServiceImpl(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
        new Thread(() -> {
            scheduledExecutor = Executors.newScheduledThreadPool(1);
        }).start();
    }

    @Override
    public List<PriceHistory> getPriceHistory(PriceHistoryRequest priceHistoryRequest) {
        return queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest));
    }

    @Override
    public void calculateListSimplePrice() {
        PriceHistoryRequest priceHistoryRequest = new PriceHistoryRequest();
        for (String stock : STOCK_ARRAYS) {
            priceHistoryRequest.setSymbol(stock);
            List<PriceHistory> priceHistoryList = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest));
            scheduledExecutor.execute(() -> priceSimplePriceSymbol(priceHistoryList));
        }

    }
    @Override
    public List<PriceHistory> calculateSimplePrice(PriceHistoryRequest priceHistoryRequest) {
            List<PriceHistory> priceHistoryList = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest));
            List<PriceHistory> priceHistories = priceSimplePriceSymbol(priceHistoryList);
        return priceHistories;

    }

    public List<PriceHistory> priceSimplePriceSymbol(List<PriceHistory> priceHistories) {
        double cumulativeLog = 1;
        for (int i = 1; i < priceHistories.size(); i++) {
            cumulativeLog = coreCalculatePrice(priceHistories, cumulativeLog, i);
        }
        List<Double> simpleReturn = priceHistories.stream().map(p -> p.getSimpleReturn()).collect(Collectors.toList());
        for (int k=0;k<DAY;k++){
           simpleReturn.add(0D);
        }
        for (int i = 1; i < priceHistories.size(); i++) {
           double setVolatility = calculateSD(simpleReturn, i)* Math.sqrt(252);
            priceHistories.get(i).setVolatility(setVolatility);
        }
       priceHistoryRepository.saveAll(priceHistories);
        return priceHistories;
    }

    private double coreCalculatePrice(List<PriceHistory> priceHistories, double cumulativeLog, int i) {
        PriceHistory priceHistory = priceHistories.get(i);
        double priceT = priceHistory.getClose();
        double priceT_1 = priceHistories.get(i - 1).getClose();
        double simpleReturn = (priceT - priceT_1) / priceT_1 ;
        double grossReturn = 1 + simpleReturn;
        double logReturn = Math.log(grossReturn);
        cumulativeLog = cumulativeLog * (1 + logReturn);
        priceHistory.setSimpleReturn(simpleReturn*100);
        priceHistory.setGrossReturn(grossReturn*100);
        priceHistory.setLogReturn(logReturn*100);
        priceHistory.setCumulativeLog(cumulativeLog);
        return cumulativeLog;
    }
    public static double calculateSD(List<Double> numArray,int i)
    {
        double sum = 0.0, standardDeviation = 0.0;
        int length = DAY;

        for(int j=0;j<DAY;j++) {
            sum += numArray.get(j+i);
        }

        double mean = sum/length;
        for(int j=0;j<DAY;j++) {
            standardDeviation += Math.pow(numArray.get(j+i) - mean, 2);
        }
        return Math.sqrt(standardDeviation/length);
    }


    public List<PriceHistory> queryPolicyJoinProduct(Predicate condition) {

        return new JPAQuery<>(em).select(Q_Price)
                .from(Q_Price)
                .where(condition)
                .orderBy(Q_Price.time.asc()).fetch();
    }

    private Predicate conditionPriceRe(PriceHistoryRequest priceRe) {
        BooleanBuilder condition = new BooleanBuilder();

        if (!StringUtils.isEmpty(priceRe.getSymbol())) {
            condition.and(Q_Price.sym.eq(priceRe.getSymbol()));
        }
        if (priceRe.getFromTime() != null)
            condition.and(Q_Price.time.after(priceRe.getFromTime()));
        if (priceRe.getToTime() != null)
            condition.and(Q_Price.time.before(priceRe.getToTime()));
        return condition;
    }

}
