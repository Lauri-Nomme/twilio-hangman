package io.github.unapplicable.hangman.service;

import rx.Single;

public interface PlayerService {
    Single<Player> fetch(String playerId);

    Single<Player> create(Player player);
}
