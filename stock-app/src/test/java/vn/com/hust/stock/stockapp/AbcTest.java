package vn.com.hust.stock.stockapp;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import vn.com.hust.stock.stockapp.repository.PriceHistoryRepository;
import vn.com.hust.stock.stockapp.service.PriceHistoryService;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;

@SpringBootTest
@Slf4j
public class AbcTest {
    @Autowired
    private PriceHistoryRepository priceHistoryRepository;
    @Autowired
    private PriceHistoryService priceHistoryService;
    @Test
    public void Test1(){
        log.info("=============> start:");

        PriceHistory priceHistoryRequest = new PriceHistory();
        priceHistoryRequest.setSym("TEST");
        priceHistoryRequest.setId(1l);
        priceHistoryRepository.save(priceHistoryRequest);

//        PriceHistoryRequest priceHistoryRequest1 = new PriceHistoryRequest();
//        priceHistoryRequest1.setSymbol("TEST");
//        List<PriceHistory> priceHistory1 = priceHistoryService.getPriceHistory(priceHistoryRequest1);
//        System.out.println(priceHistory1.size());

        log.info(priceHistoryRepository.findById(1l).get().toString());
        log.info("=========> End :");
    }
}
