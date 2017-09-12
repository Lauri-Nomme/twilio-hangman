package io.github.unapplicable.hangman.service;

import rx.Single;

public class GameServiceImpl implements GameService {
    private final PlayerRepository playerRepository;
    private WordList wordList;

    public GameServiceImpl(PlayerRepository playerRepository, WordList wordList) {
        this.playerRepository = playerRepository;
        this.wordList = wordList;
    }

    public Single<Game> start(String playerName) {
        return playerRepository
            .fetch(playerName)
            .flatMap(this::startWithPlayer);
    }

    private Single<Game> startWithPlayer(Player player) {
        String word = wordList.getRandom();
        Game game = new Game(player, word);
        return Single.just(game);
    }
}
