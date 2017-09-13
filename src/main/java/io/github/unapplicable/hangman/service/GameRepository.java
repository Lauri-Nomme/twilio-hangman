package io.github.unapplicable.hangman.service;

import javafx.util.Pair;
import rx.Single;

public interface GameRepository {
    Single<Game> fetch(String gameId);

    Single<Game> update(String gameId, Game game);

    Single<Pair<String,Game>> create(Game game);
}
