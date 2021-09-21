package vn.com.hust.stock.stockapp.repository;


import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;

import java.util.List;

public interface   PriceHistoryRepository extends CustomRepository<PriceHistory,Long>{


}
