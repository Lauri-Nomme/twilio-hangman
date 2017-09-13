package io.github.unapplicable.hangman.service;

import rx.Observable;
import rx.Single;

public class PlayerServiceImpl implements PlayerService {
    private final PlayerRepository playerRepository;

    public PlayerServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Single<Player> create(Player player) {
        return playerRepository.create(player);
    }

    public Single<Player> fetch(String playerId) {
        return playerRepository.fetch(playerId);
    }

    public Observable<Player> list() {
        return playerRepository.list();
    }
}
