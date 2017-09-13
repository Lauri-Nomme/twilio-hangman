package io.github.unapplicable.hangman.service;

import javafx.util.Pair;
import rx.Single;

public interface GameService {
    Single<Pair<String, Game>> start(String playerName);

    Single<Game> guess(String gameId, String letter);
}
