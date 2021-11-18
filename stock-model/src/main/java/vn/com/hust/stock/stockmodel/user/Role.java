package vn.com.hust.stock.stockmodel.user;


import org.springframework.security.core.GrantedAuthority;

public enum Role implements GrantedAuthority {
    ROLE_ADMIN, ROLE_CLIENT,ROLE_MEMBER;

    public String getAuthority() {
        return name();
    }

}

