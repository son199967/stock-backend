package vn.com.hust.stock.stockmodel.exception;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BusinessErrorCode {
    private String code;
    private String description;
    private int httpStatus;
}
