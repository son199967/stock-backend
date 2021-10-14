package vn.com.hust.stock.stockapp.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import vn.com.hust.stock.stockapp.repository.StockPriceRepository;
import vn.com.hust.stock.stockapp.service.StockPriceService;
import vn.com.hust.stock.stockmodel.entity.StockPrice;

import javax.transaction.Transactional;

@Service
public class StockPriceServiceImpl implements StockPriceService {
    private  final StockPriceRepository stockPriceRepository;

    @Autowired
    public StockPriceServiceImpl(StockPriceRepository stockPriceRepository) {
        this.stockPriceRepository = stockPriceRepository;
    }

    @Override
    @Transactional
    public StockPrice createNewStockPrice(StockPrice stockPrice) {
        StockPrice stockPriceNew = new StockPrice();
        stockPriceNew.setCeilPrice(stockPrice.getCeilPrice());
        stockPriceNew.setColor(stockPrice.getColor());
        stockPriceNew.setCustomPrice(stockPrice.getCustomPrice());
        stockPriceNew.setFloorPrice(stockPrice.getFloorPrice());
        stockPriceNew.setHeightPrice(stockPrice.getHeightPrice());
        stockPriceNew.setLength(stockPrice.getLength());
        stockPriceNew.setLowPrice(stockPrice.getLowPrice());
        stockPriceNew.setOpenPrice(stockPrice.getOpenPrice());
        stockPriceNew.setPrice_fluctuate(stockPrice.getPrice_fluctuate());
        stockPriceNew.setPrice_fluctuate(stockPrice.getPrice_fluctuate());
        stockPriceNew.setTradePrice(stockPrice.getTradePrice());
        return stockPriceRepository.save(stockPriceNew);
    }
}
