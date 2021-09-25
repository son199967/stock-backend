package vn.com.hust.stock.stockmodel.enumm;

public enum GroupCompany {
    STOCK("chứng khoán"),BANK("ngân hàng"),TECH("công nghệ ");
    private String group;
    GroupCompany(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
