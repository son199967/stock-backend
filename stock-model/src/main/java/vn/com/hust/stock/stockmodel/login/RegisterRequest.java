package vn.com.hust.stock.stockmodel.login;

import lombok.Data;

@Data
public class RegisterRequest {
    private String userName;
    private String passWord;
    private String repassWord;

}
