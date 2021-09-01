package vn.com.hust.stock.stockmodel.exception;

public interface ErrorCode {
    BusinessErrorCode JSON_PROCESSING_ERROR = new BusinessErrorCode("ABC-1051", "json encoding/decoding error", 500);
    BusinessErrorCode INTERNAL_SERVER_ERROR = new BusinessErrorCode("ABC-1000", "Internal Server Error", 500);
    BusinessErrorCode INVALID_PASSWORD = new BusinessErrorCode("ABC-1", "invalid password", 400);
    BusinessErrorCode INVALID_USER_NAME = new BusinessErrorCode("ABC-1", "username has been exist", 400);
    BusinessErrorCode INVALID_SORT_BY_PARAM = new BusinessErrorCode("ABC-12", "Invalid sortBy parameter Error", 400);
    BusinessErrorCode NOT_FOUND = new BusinessErrorCode("ABC-12", "Not Found", 400);

}
