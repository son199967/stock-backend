package vn.com.hust.stock.stockmodel.exception;

public class InvalidSortByParameter extends BusinessException {

    public InvalidSortByParameter(String message) {
        super(ErrorCode.INVALID_SORT_BY_PARAM, message);
    }

    public InvalidSortByParameter(String message, Throwable cause) {
        super(ErrorCode.INVALID_SORT_BY_PARAM, message, cause);
    }

    public InvalidSortByParameter(Throwable cause) {
        super(ErrorCode.INVALID_SORT_BY_PARAM, cause);
    }
}