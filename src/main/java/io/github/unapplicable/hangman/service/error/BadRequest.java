package io.github.unapplicable.hangman.service.error;

public class BadRequest extends BaseError {
    public BadRequest(String message) {
        super(message, 400);
    }
}
