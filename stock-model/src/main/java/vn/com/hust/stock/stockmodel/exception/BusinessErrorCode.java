package vn.com.hust.stock.stockmodel.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class BusinessErrorCode {
    private String code;
    private String description;
    private int httpStatus;
}
