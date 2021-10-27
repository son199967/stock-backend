package vn.com.hust.stock.stockapp.serviceImpl;


import com.mysql.cj.xdevapi.Collection;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import javassist.compiler.ast.Symbol;
import org.apache.kafka.common.protocol.types.Field;
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
import vn.com.hust.stock.stockmodel.response.PriceHistoryRes;

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
    private static int RE_DAY = 15;
    private Map<String, List<String>> STOCK_MAP = new HashMap<>();
    private List<String> STOCK_ARRAYS = new ArrayList<>();

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public PriceHistoryServiceImpl(PriceHistoryRepository priceHistoryRepository) {
        STOCK_MAP.put("BDS", Arrays.asList("VIC", "VHM", "VRE", "PRD", "KDH", "REE", "DXG", "HDG", "FLC", "ITA"));
        STOCK_MAP.put("CK", Arrays.asList("SSI", "VND", "VCI", "HCM", "MBS", "FTS", "SHS", "KLB", "AGR", "TVS"));
        STOCK_MAP.put("CONGNGHE", Arrays.asList("FPT", "FOX", "CMG", "SAM", "SGT", "ELC", "VEC", "ITD", "TTN", "CNC"));
        STOCK_MAP.put("DUOCPHAM", Arrays.asList("DGC", "DHG", "DVN", "IMP", "TRA", "DMC", "CSV", "DCL", "VFG", "OPC"));
        STOCK_MAP.put("HK", Arrays.asList("ACV", "VJC", "HVN", "SAS", "SGN", "NCT", "NCS", "MAS", "NAS", "ARM"));
        STOCK_MAP.put("NGANHANG", Arrays.asList("VCB", "TCB", "BID", "CTG", "MBB", "VPB", "ACB", "SHB", "STB", "TPB", "BVH", "VIB", "HDB", "EIB", "LPB", "BAB", "NVB", "ABB", "PVI", "VBB"));
        STOCK_MAP.put("XAYDUNG", Arrays.asList("VCG", "DIG", "DXG", "CTD", "HBC", "ROS", "VCP", "VLB", "TV2", "CC1"));
        STOCK_MAP.put("DAUKHI", Arrays.asList("GAS", "BSR", "PLX", "PVS", "PVD", "PVI", "PVT", "PLC", "PET", "PGS"));
        STOCK_MAP.put("NHUA", Arrays.asList("NTP", "BMP", "AAA", "DNP", "SVI", "INN", "RDP", "HII", "VNP", "MCP"));
        STOCK_MAP.put("COMMOM", Arrays.asList("VNINDEX", "VN30", "VN30_HOSE", "HNX", "HNX30", "CONGNGHE", "DAUKHI", "DICHVU", "DUOCPHAM", "XAYDUNG",
                "NANGLUONG", "NGANHANG", "NHUA", "THEP", "THUCPHAM", "THUONGMAI", "THUYSAN", "UPCOM", "VANTAI", "VLXD", "HK"));

        for (List<String> a : STOCK_MAP.values()) {
            STOCK_ARRAYS.addAll(a);
        }
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
        return priceHistories;
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
        if (!symbols.isEmpty()) {
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
        List<String> stocks = STOCK_MAP.get("COMMOM");
        return priceLast(null, null, stocks);
    }
}
