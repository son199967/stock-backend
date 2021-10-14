package vn.com.hust.stock.stockapp.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.com.hust.stock.stockapp.repository.IndicatorRepository;
import vn.com.hust.stock.stockapp.repository.StockInfoRepository;
import vn.com.hust.stock.stockapp.service.StockInfoService;
import vn.com.hust.stock.stockmodel.entity.Indicator;
import vn.com.hust.stock.stockmodel.entity.StockInfo;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Service
public class StockInfoServiceImpl implements StockInfoService {

    private final StockInfoRepository stockInfoRepository;
    private final IndicatorRepository indicatorRepository;

    @PersistenceContext
    private EntityManager em;


    @Autowired
    public StockInfoServiceImpl(StockInfoRepository stockInfoRepository,  IndicatorRepository indicatorRepository) {
        this.stockInfoRepository = stockInfoRepository;
        this.indicatorRepository = indicatorRepository;
    }




    @Override
    @Transactional
    public StockInfo createNewStockInFo(StockInfo stockInfo) {
        StockInfo stockInfoNew = new StockInfo();
        stockInfoNew.setBookValue(stockInfo.getBookValue());
        stockInfoNew.setCapitalNow(stockInfo.getCapitalNow());
        stockInfoNew.setCustomEps(stockInfo.getCustomEps());
        stockInfoNew.setDate_start(stockInfo.getDate_start());
        stockInfoNew.setDate_start_length(stockInfo.getDate_start_length());
        stockInfoNew.setDate_start_price(stockInfo.getDate_start_price());
        stockInfoNew.setLengthLh(stockInfo.getLengthLh());
        stockInfoNew.setLengthNy(stockInfo.getLengthNy());
        stockInfoNew.setPe(stockInfo.getPe());
        stockInfoNew.setUnitBookValue(stockInfo.getUnitBookValue());
        stockInfoNew.setUnit(stockInfo.getUnit());
        stockInfoNew.setWashyEps(stockInfo.getWashyEps());
        stockInfoNew.setIndicators(stockInfo.getIndicators());
        stockInfoRepository.save(stockInfoNew);
        for (Indicator indicator: stockInfo.getIndicators()){
            indicator.setStockInfo(stockInfoNew);
            indicatorRepository.save(indicator);
        }
        return stockInfoNew;
    }

}
