package io.github.unapplicable.hangman.service.error;

public class BaseError extends RuntimeException {
    private final int code;

    public BaseError(String message, int code) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
