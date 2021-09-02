package vn.com.hust.stock.stockmodel.exception;
public class DataAlreadyExistsException extends RuntimeException {
    public DataAlreadyExistsException(String message) {
        super(message);
    }
}
