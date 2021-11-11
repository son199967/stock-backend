package vn.com.hust.stock.stockapp.serviceImpl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Predicate;
import com.querydsl.jpa.impl.JPAQuery;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.com.hust.stock.stockapp.repository.StockRepository;
import vn.com.hust.stock.stockapp.service.StockPriceService;
import vn.com.hust.stock.stockapp.service.StockService;
import vn.com.hust.stock.stockmodel.entity.*;
import vn.com.hust.stock.stockmodel.exception.BusinessException;
import vn.com.hust.stock.stockmodel.exception.ErrorCode;
import vn.com.hust.stock.stockmodel.request.StockRequest;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class StockServiceImpl implements StockService {
    private final StockRepository stockRepository;
    private final StockPriceService stockPriceService;

    private List<String> vn100;

    private static final QStock Q_STOCK = QStock.stock;
    private static final QStockPrice Q_STOCK_PRICE = QStockPrice.stockPrice;
    private Map<String, List<String>> STOCK_MAP = new HashMap<>();
    private List<String> STOCK_ARRAYS = new ArrayList<>();
    @Autowired
    public StockServiceImpl(StockRepository stockRepository,
                            StockPriceService stockPriceService) {
        this.stockRepository = stockRepository;
        this.stockPriceService = stockPriceService;
        STOCK_MAP.put("BDS", Arrays.asList("VIC", "VHM", "VRE", "PRD", "KDH", "REE", "DXG", "HDG", "FLC", "ITA"));
        STOCK_MAP.put("CK", Arrays.asList("SSI", "VND", "VCI", "HCM", "MBS", "FTS", "SHS", "KLB", "AGR", "TVS"));
        STOCK_MAP.put("CONGNGHE", Arrays.asList("FPT", "FOX", "CMG", "SAM", "SGT", "ELC", "VEC", "ITD", "TTN", "CNC"));
        STOCK_MAP.put("DUOCPHAM", Arrays.asList("DGC", "DHG", "DVN", "IMP", "TRA", "DMC", "CSV", "DCL", "VFG", "OPC"));
        STOCK_MAP.put("HK", Arrays.asList("ACV", "VJC", "HVN", "SAS", "SGN", "NCT", "NCS", "MAS", "NAS", "ARM"));
        STOCK_MAP.put("NGANHANG", Arrays.asList("VCB", "TCB", "BID", "CTG", "MBB", "VPB", "ACB", "SHB", "STB", "TPB", "BVH", "VIB", "HDB", "EIB", "LPB", "BAB", "NVB", "ABB", "PVI", "VBB"));
        STOCK_MAP.put("XAYDUNG", Arrays.asList("VCG", "DIG", "DXG", "CTD", "HBC", "ROS", "VCP", "VLB", "TV2", "CC1"));
        STOCK_MAP.put("DAUKHI", Arrays.asList("GAS", "BSR", "PLX", "PVS", "PVD", "PVI", "PVT", "PLC", "PET", "PGS"));
        STOCK_MAP.put("NHUA", Arrays.asList("NTP", "BMP", "AAA", "DNP", "SVI", "INN", "RDP", "HII", "VNP", "MCP"));
        STOCK_MAP.put("COMMON", Arrays.asList("VNINDEX", "VN30", "VN30_HOSE", "HNX", "HNX30","UPCOM"));
        STOCK_MAP.put( "GROUPS", Arrays.asList("CONGNGHE", "DAUKHI", "DICHVU", "DUOCPHAM", "XAYDUNG",
                "NANGLUONG", "NGANHANG", "NHUA", "THEP", "THUCPHAM", "THUONGMAI", "THUYSAN", "VANTAI", "VLXD", "HK"));

        for (List<String> a : STOCK_MAP.values()) {
            STOCK_ARRAYS.addAll(a);
        }

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
        stockNew.setCode(stockRe.getCode());
        stockNew.setGroupCompany(stockRe.getGroupCompany());
        stockNew.setLogo(stockRe.getLogo());
        stockNew.setNameCompany(stockRe.getNameCompany());
        StockPrice stockPrice = stockPriceService.createNewStockPrice(stockRe.getStockPrice());
        Stock stockResult = stockRepository.save(stockNew);
        return stockResult;
    }

    @Override
    public Stock updateStock(Stock stockRe) {
        Stock stock = stockRepository.findById(stockRe.getId())
                .orElseThrow( ()->new  BusinessException(ErrorCode.STOCK_NOT_EXIST));
        stock.setLogo(stockRe.getLogo());
        stock.setNameCompany(stockRe.getNameCompany());
        stock.setLogo(stockRe.getLogo());
        Stock stockResult = stockRepository.save(stock);

        return stockResult;
    }

    @Override
    public Stock getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow( ()->new  BusinessException(ErrorCode.STOCK_NOT_EXIST));
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
    public List<String> search(String sym) {
          if (sym ==null)
              return STOCK_ARRAYS;
          return STOCK_ARRAYS.stream().filter(s -> s.startsWith(sym)).collect(Collectors.toList());
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
        if (stockRequest.getGroupCompany()!=null) {
            condition.and(Q_STOCK.groupCompany.eq(stockRequest.getGroupCompany()));
        }
        return condition;
    }
}
