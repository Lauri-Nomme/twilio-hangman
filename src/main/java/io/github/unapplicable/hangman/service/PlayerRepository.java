package io.github.unapplicable.hangman.service;

import rx.Observable;
import rx.Single;

public interface PlayerRepository {
    Single<Player> create(Player player);

    Single<Player> fetch(String playerId);

    Observable<Player> list();
}
