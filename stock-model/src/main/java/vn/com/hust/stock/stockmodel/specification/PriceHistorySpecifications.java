package vn.com.hust.stock.stockmodel.specification;


import lombok.Builder;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;

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
