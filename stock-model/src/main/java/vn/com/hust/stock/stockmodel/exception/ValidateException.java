package vn.com.hust.stock.stockmodel.exception;

public class ValidateException extends BusinessException{
    public ValidateException(BusinessErrorCode errorCode) {
        super(errorCode);
    }

    public ValidateException(BusinessErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public ValidateException(BusinessErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }

    public ValidateException(BusinessErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}
