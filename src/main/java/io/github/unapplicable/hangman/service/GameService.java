package io.github.unapplicable.hangman.service;

import rx.Single;

public interface GameService {
    Single<Game> start(String playerName);
}
