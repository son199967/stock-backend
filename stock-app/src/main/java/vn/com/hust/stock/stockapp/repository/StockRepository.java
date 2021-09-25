package vn.com.hust.stock.stockapp.repository;

import vn.com.hust.stock.stockmodel.entity.Stock;

import java.util.Optional;

public interface StockRepository extends CustomRepository<Stock, Long> {

    Optional<Stock> findByCode(String code);
}
