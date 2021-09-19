package vn.com.hust.stock.stockmodel.login;

import lombok.Data;

public class LoginRequest {
    public LoginRequest(String username,String password,String udid) {
        this.username = username;
        this.password = password;
        this.udid = udid;
    }
    public String username;
    public String password;
    public String udid;
}