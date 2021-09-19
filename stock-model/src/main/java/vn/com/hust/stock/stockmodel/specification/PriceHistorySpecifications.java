package vn.com.hust.stock.stockmodel.specification;


import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;
import vn.com.hust.stock.stockmodel.request.PriceHistoryRequest;

import javax.inject.Singleton;

@Builder
public class PriceHistorySpecifications extends CustomSpecifications<PriceHistory> {
//    public   Specification<PriceHistory> eq(PriceHistoryRequest priceHistoryRequest) {
//        Specification<PriceHistory> specification = Specification
//                .where(eq("sym",priceHistoryRequest.getSymbol())
//                        .and(formDate("time",priceHistoryRequest.getFromTime()))
//                        .and(toDate("time",priceHistoryRequest.getToTime())));
//        return specification;
//    }
}
