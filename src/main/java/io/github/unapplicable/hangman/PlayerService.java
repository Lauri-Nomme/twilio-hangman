package io.github.unapplicable.hangman;

import rx.Single;

import java.util.NoSuchElementException;

class PlayerService {
    Single<Player> fetch(String playerId) {
        return Single.error(new NoSuchElementException());
    }
}
