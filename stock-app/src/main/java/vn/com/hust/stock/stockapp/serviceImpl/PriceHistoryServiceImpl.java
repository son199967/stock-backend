package vn.com.hust.stock.stockapp.serviceImpl;


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

@Service
public class PriceHistoryServiceImpl  implements PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    private ScheduledExecutorService scheduledExecutor;
    private static final   QPriceHistory Q_Price = QPriceHistory.priceHistory;


    @PersistenceContext
    private EntityManager em;

        private List<String> STOCK_ARRAYS = new ArrayList<>(Arrays.asList("ACB","BID",
            "BVH","CTG","FPT","GAS","GVR","HDB","HPG","KDH","MBB","MSN","MWG","NVL","PDR","PLX"
            ,"PNJ","POW","SAB","SSI","STB","TCB","TPB","VCB","VHM","VIC","VJC","VNM","VPB","VRE","REE"));

    @Autowired
    public PriceHistoryServiceImpl(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
        new Thread(() -> {
            scheduledExecutor = Executors.newScheduledThreadPool(1);
        }).start();
    }

    @Override
    public List<PriceHistory> getPriceHistory(PriceHistoryRequest priceHistoryRequest) {
//        Specification<PriceHistory> specification =  PriceHistorySpecifications.builder()
//                .build().eq(priceHistoryRequest);
//        return priceHistoryRepository.findAll(specification);
//        return null;
        return null;
    }

    @Override
    public void calculateSimplePrice() {
        PriceHistoryRequest priceHistoryRequest = new PriceHistoryRequest();
        for (String stock :STOCK_ARRAYS){
            priceHistoryRequest.setSymbol(stock);
            priceHistoryRequest.setFromTime(LocalDate.of(2021,1,11));
            List<PriceHistory> priceHistoryList = queryPolicyJoinProduct(conditionPriceRe(priceHistoryRequest));

            scheduledExecutor.execute(()->priceSimplePriceSymbol(priceHistoryList));
        }

    }

//    private List<PriceHistory> createQueryPriceHistory(PriceHistoryRequest priceRe) {
//        CriteriaBuilder criteriaBuilder = em.getCriteriaBuilder();
//        CriteriaQuery<PriceHistory> query = criteriaBuilder.createQuery(PriceHistory.class);
//        Root<PriceHistory> root = query.from(PriceHistory.class);
//
//        List<Order> orderList = new ArrayList();
//        ///condition
//        Predicate hasSym = criteriaBuilder.equal(root.get("sym"), priceRe.getSymbol());
//        Predicate fromTime = null;
//        Predicate toTime = null;
//        if (priceRe.getFromTime() != null)
//
//            fromTime = criteriaBuilder.greaterThanOrEqualTo(root.get("time"), priceRe.getFromTime());
//
//        if (priceRe.getToTime() != null)
//            toTime = criteriaBuilder.lessThanOrEqualTo(root.get("time"), priceRe.getToTime());
//
//        Predicate finalPredicate = criteriaBuilder.and(hasSym,fromTime,toTime);
//        orderList.add(criteriaBuilder.asc( root.get("time")));
//        List<PriceHistory> priceHistoryList =em.createQuery(
//                 query
//                         .select(root)
//                         .where(hasSym)
//                         .orderBy(orderList))
//                .getResultList();
//        return priceHistoryList;
//    }

    public void priceSimplePriceSymbol(List<PriceHistory> priceHistories){
        double cumulativeLog = 1;
        for (int i =1 ;i<priceHistories.size();i++){
            cumulativeLog = coreCacular(priceHistories, cumulativeLog, i);
        }
        priceHistoryRepository.saveAll(priceHistories);

    }

    private double coreCacular(List<PriceHistory> priceHistories, double cumulativeLog, int i) {
        PriceHistory priceHistory = priceHistories.get(i);
        double priceT = priceHistory.getClose();
        double priceT_1 = priceHistories.get(i -1).getClose();
        double simpleReturn = (priceT-priceT_1)/priceT_1 *100;
        double grossReturn = 100+ simpleReturn;
        cumulativeLog = cumulativeLog *(1+grossReturn)/100;
        priceHistory.setSimpleReturn(simpleReturn);
        priceHistory.setGrossReturn(grossReturn);
        priceHistory.setLogReturn(Math.log(grossReturn));
        priceHistory.setCumulativeLog(cumulativeLog);
        return cumulativeLog;
    }


//    @Override
//    public void calculateGrossPrice() {
//
//    }

    public List<PriceHistory> queryPolicyJoinProduct(Predicate condition) {

           return  new JPAQuery<>(em).select(Q_Price)
                .from(Q_Price)
                .where(condition)
                .orderBy(Q_Price.time.asc()).fetch();
    }
    private Predicate conditionPriceRe(PriceHistoryRequest priceRe){
        BooleanBuilder condition = new BooleanBuilder();

        if (!StringUtils.isEmpty(priceRe.getSymbol())) {
            condition.and(Q_Price.sym.eq(priceRe.getSymbol()));
        }
        if (priceRe.getFromTime()!=null)
            condition.and(Q_Price.time.after(priceRe.getFromTime()));
        if (priceRe.getToTime() !=null)
            condition.and(Q_Price.time.before(priceRe.getToTime()));
        return condition;
    }

}
