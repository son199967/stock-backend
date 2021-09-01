package vn.com.hust.stock.stockmodel.enumm;

public enum Group {
    STOCK("chứng khoán"),BANK("ngân hàng");
    private String group;
    Group(String group) {
        this.group = group;
    }

    public String getGroup() {
        return group;
    }
}
