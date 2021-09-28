package vn.com.hust.stock.stockapp.serviceImpl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.com.hust.stock.stockapp.repository.StockRepository;
import vn.com.hust.stock.stockapp.service.StockInfoService;
import vn.com.hust.stock.stockapp.service.StockPriceService;
import vn.com.hust.stock.stockapp.service.StockService;
import vn.com.hust.stock.stockmodel.entity.*;
import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;
import vn.com.hust.stock.stockmodel.request.StockRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {
    private final StockRepository stockRepository;
    private final StockPriceService stockPriceService;
    private final StockInfoService stockInfoService;

    private static final QStock Q_STOCK = QStock.stock;
    private static final QStockInfo Q_STOCK_INFO = QStockInfo.stockInfo;
    private static final QStockPrice Q_STOCK_PRICE = QStockPrice.stockPrice;

    @Autowired
    public StockServiceImpl(StockRepository stockRepository, StockPriceService stockPriceService, StockInfoService stockInfoService) {
        this.stockRepository = stockRepository;
        this.stockPriceService = stockPriceService;
        this.stockInfoService = stockInfoService;
    }



    @PersistenceContext
    private EntityManager em;



    @Override
    @Transactional
    public Stock createNewStock(Stock stockRe) {
        Optional<Stock> stock = stockRepository.findByCode(stockRe.getCode());
        if (stock.isPresent()){
            throw new BusinessException(ErrorCode.STOCK_EXIST);
        }
        Stock stockNew = new Stock();
        stockNew.setAddress(stockRe.getAddress());
        stockNew.setCode(stockRe.getCode());
        stockNew.setDescription(stockRe.getDescription());
        stockNew.setEmail(stockRe.getEmail());
        stockNew.setGroupCompany(stockRe.getGroupCompany());
        stockNew.setLogo(stockRe.getLogo());
        stockNew.setNameCompany(stockRe.getNameCompany());
        stockNew.setPhone(stockRe.getPhone());
        stockNew.setPrice(stockRe.getPrice());
        stockNew.setWebsite(stockRe.getWebsite());
        StockPrice stockPrice = stockPriceService.createNewStockPrice(stockRe.getStockPrice());
        StockInfo stockInfo = stockInfoService.createNewStockInFo(stockRe.getStockInfo());
        stockNew.setStockPrice(stockPrice);
        stockNew.setStockInfo(stockInfo);
        Stock stockResult = stockRepository.save(stockNew);
        return stockResult;
    }

    @Override
    public Stock updateStock(Stock stockRe) {
        Stock stock = stockRepository.findById(stockRe.getId())
                .orElseThrow( ()->new  BusinessException(ErrorCode.STOCK_NOT_EXIST));
        stock.setLogo(stockRe.getLogo());
        stock.setPhone(stockRe.getPhone());
        stock.setNameCompany(stockRe.getNameCompany());
        stock.setDescription(stockRe.getDescription());
        stock.setLogo(stockRe.getLogo());
        Stock stockResult = stockRepository.save(stock);

        return stockResult;
    }

    @Override
    public Stock getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow( ()->new  BusinessException(ErrorCode.STOCK_NOT_EXIST));


        List<StockReport> reportList = stock.getStockInfo().getStockReports().stream().sorted(Comparator.comparingInt(StockReport::getYear).thenComparing(StockReport::getPrecious)).collect(Collectors.toList());
        Collections.reverse(reportList);
        stock.getStockInfo().setStockReports(reportList);
        return stock;
    }

    @Override
    public Stock getStockByCode(String code) {
       Stock stock = stockRepository.findByCode(code)
               .orElseThrow(()->new BusinessException(ErrorCode.STOCK_NOT_EXIST,"Not found stock by code "+code));
       return stock;
    }

    @Override
    public List<Stock> filterStockBy(StockRequest stockRequest) {
        List<Stock> stocks = searchStockBy(stockRequest);

        return stocks;
    }

    @Override
    public void deleteById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(()->new BusinessException(ErrorCode.STOCK_NOT_EXIST,"Not found stock by id "+id));
        stockRepository.deleteById(id);
    }

    public List<Stock> searchStockBy(StockRequest stockRequest) {
        Predicate condition = conditionStockRe(stockRequest);
        return new JPAQuery<>(em).select(Q_STOCK)
                .from(Q_STOCK)
                .where(condition)
                .fetch();
    }

    private Predicate conditionStockRe(StockRequest stockRequest) {
        BooleanBuilder condition = new BooleanBuilder();

        if (!StringUtils.isEmpty(stockRequest.getCode())) {
            condition.and(Q_STOCK.code.like("%"+stockRequest.getCode()+"%"));
        }
        if (!StringUtils.isEmpty(stockRequest.getName())) {
            condition.and(Q_STOCK.nameCompany.like("%"+stockRequest.getName()+"%"));
        }
        if (!StringUtils.isEmpty(stockRequest.getGroupCompany())) {
            condition.and(Q_STOCK.groupCompany.eq(stockRequest.getGroupCompany()));
        }
        if (stockRequest.getPriceFrom() != 0)
            condition.and(Q_STOCK.price.lt(stockRequest.getPriceFrom()));
        if (stockRequest.getPriceFrom() != 0)
            condition.and(Q_STOCK.price.gt(stockRequest.getPriceTo()));
        return condition;
    }
}
