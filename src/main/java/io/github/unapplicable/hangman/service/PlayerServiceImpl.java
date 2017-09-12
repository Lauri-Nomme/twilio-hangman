package io.github.unapplicable.hangman.service;

import rx.Single;

import java.util.NoSuchElementException;

public class PlayerServiceImpl implements PlayerService {
    public Single<Player> fetch(String playerId) {
        return Single.error(new NoSuchElementException());
    }

    public Single<Player> create(Player player) {
        return Single.just(player);
    }
}
