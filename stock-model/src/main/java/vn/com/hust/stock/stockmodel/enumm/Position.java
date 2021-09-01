package vn.com.hust.stock.stockmodel.enumm;

public enum Position {
    TV_HDQT("Thành viên HĐQT"),HDQT("Chủ tịch HĐQT");
    private String position;
    Position(String position) {
        this.position = position;
    }

    public String getPosition() {
        return position;
    }
}
