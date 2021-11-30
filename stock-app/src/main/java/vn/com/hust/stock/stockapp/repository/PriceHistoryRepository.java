package vn.com.hust.stock.stockapp.repository;

import org.springframework.data.jpa.repository.Query;
import vn.com.hust.stock.stockmodel.entity.PriceHistory;

import java.util.List;


public interface  PriceHistoryRepository extends CustomRepository<PriceHistory,Long> {


    List<PriceHistory> findAllBySymOrderByTimeAsc(String sym);

    @Query("select p.sym from PriceHistory p group by p.sym")
    List<String> findSymGroup();
}
