package vn.com.hust.stock.stockapp.serviceImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import vn.com.hust.stock.stockapp.repository.CustomRepository;
import vn.com.hust.stock.stockapp.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;
import vn.com.hust.stock.stockmodel.specification.PriceHistorySpecifications;

import java.util.List;

@Service
public class PriceHistoryServiceImpl  implements PriceHistoryService {

    private final PriceHistoryRepository priceHistoryRepository;
    @Autowired
    public PriceHistoryServiceImpl(PriceHistoryRepository priceHistoryRepository) {
        this.priceHistoryRepository = priceHistoryRepository;
    }




    @Override
    public List<PriceHistory> getPriceHistory(PriceHistoryRequest priceHistoryRequest) {
        Specification<PriceHistory> specification =  PriceHistorySpecifications.builder()
                .build().eq(priceHistoryRequest);
        return priceHistoryRepository.findAll(specification);
    }
}
