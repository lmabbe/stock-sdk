package io.github.stock.sdk.exception;

/**
 * Stock SDK 全局统一运行时异常
 */
public class StockException extends RuntimeException {

    private final String code;

    public StockException(String message) {
        super(message);
        this.code = "INTERNAL_ERROR";
    }

    public StockException(String code, String message) {
        super("[" + code + "] " + message);
        this.code = code;
    }

    public StockException(String code, String message, Throwable cause) {
        super("[" + code + "] " + message, cause);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
