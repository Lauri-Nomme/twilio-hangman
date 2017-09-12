package io.github.unapplicable.hangman.service.error;

public class NotFound extends BaseError {
    public NotFound(String message) {
        super(message, 404);
    }
}
