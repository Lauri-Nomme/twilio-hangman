package io.github.unapplicable.hangman.service;

import rx.Single;

public class PlayerServiceImpl implements PlayerService {
    private final PlayerStorage playerStorage;

    public PlayerServiceImpl(PlayerStorage playerStorage) {
        this.playerStorage = playerStorage;
    }

    public Single<Player> fetch(String playerId) {
        return playerStorage.fetch(playerId);
    }

    public Single<Player> create(Player player) {
        return playerStorage.create(player);
    }
}
