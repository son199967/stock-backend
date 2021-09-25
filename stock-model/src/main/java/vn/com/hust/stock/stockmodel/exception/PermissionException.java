package vn.com.hust.stock.stockmodel.exception;

import lombok.Data;
import lombok.Getter;

@Data
public class PermissionException extends RuntimeException{


    private BusinessErrorCode errorCode;

    public PermissionException( String message,BusinessErrorCode errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
}
