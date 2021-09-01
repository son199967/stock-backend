package vn.com.hust.stock.stockmodel.login;

import java.util.Random;

public class ErrorResponse {
    private final String id;
    private final String error;
    private final String message;
    private final String description;
    private final int ID_LENGTH;
    private final int charA;
    private final int charZ;

    public ErrorResponse(String error, String message) {
        this(error, message, "");
    }

    public ErrorResponse(String code, String message, String description) {
        this.ID_LENGTH = 5;
        this.charA = 97;
        this.charZ = 122;
        this.id = this.generateId();
        this.error = code;
        this.message = message;
        this.description = description;
    }

    private String generateId() {
        StringBuilder sb = new StringBuilder();
        int randomRange = 25;
        Random random = new Random();

        for(int i = 0; i < 5; ++i) {
            int ascii = 97 + random.nextInt(randomRange);
            sb.append((char)ascii);
        }

        return sb.toString();
    }

    public String getMessage() {
        return this.message;
    }

    public String getError() {
        return this.error;
    }

    public String getDescription() {
        return this.description;
    }

    public String getId() {
        return this.id;
    }

    public String toString() {
        return "ErrorResponse [id=" + this.id + ", error=" + this.error + ", message=" + this.message + ", description=" + this.description + "]";
    }
}
