package io.github.unapplicable.hangman.service;

import javafx.util.Pair;
import rx.Single;

public class GameServiceImpl implements GameService {
    private final PlayerRepository playerRepository;
    private final GameRepository gameRepository;
    private final WordList wordList;

    public GameServiceImpl(PlayerRepository playerRepository, GameRepository gameRepository, WordList wordList) {
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.wordList = wordList;
    }

    public Single<Pair<String, Game>> start(String playerName) {
        return playerRepository
            .fetch(playerName)
            .flatMap(this::startWithPlayer);
    }

    public Single<Game> guess(String gameId, String letter) {
        return gameRepository
            .fetch(gameId)
            .flatMap(game -> game.guess(letter))
            .flatMap(game -> gameRepository.update(gameId, game));
    }

    private Single<Pair<String, Game>> startWithPlayer(Player player) {
        String word = wordList.getRandom();
        Game game = new Game(player, word);
        return gameRepository.create(game);
    }
}
