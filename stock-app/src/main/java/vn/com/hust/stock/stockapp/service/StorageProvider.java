package vn.com.hust.stock.stockapp.service;

public interface StorageProvider {
    void store(String fileName, byte[] content);

    String getLocation();
}
NormalServiceImpl