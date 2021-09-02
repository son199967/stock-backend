package vn.com.hust.stock.stockmodel.exception;

public class DataCorruptedException extends RuntimeException {
    public DataCorruptedException(Throwable thr) {
        super(thr);
    }
}
