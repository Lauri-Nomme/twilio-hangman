package io.github.unapplicable.hangman.service;

import rx.Single;

public class GameServiceImpl implements GameService {
    private final PlayerRepository playerRepository;

    public GameServiceImpl(PlayerRepository playerRepository) {
        this.playerRepository = playerRepository;
    }

    public Single<Game> start(String playerName) {
        return playerRepository
            .fetch(playerName)
            .flatMap(this::startWithPlayer);
    }

    private Single<Game> startWithPlayer(Player player) {
        Game game = new Game(player);
        return Single.just(game);
    }
}
