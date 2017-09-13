package io.github.unapplicable.hangman.service;

import rx.Observable;
import rx.Single;

public interface PlayerService {
    Single<Player> create(Player player);

    Single<Player> fetch(String playerId);

    Observable<Player> list();
}
