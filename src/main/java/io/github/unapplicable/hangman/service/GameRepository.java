package io.github.unapplicable.hangman.service;

import javafx.util.Pair;
import rx.Observable;
import rx.Single;

public interface GameRepository {
    Single<Pair<String,Game>> create(Game game);

    Single<Game> fetch(String gameId);

    Observable<Game> list();

    Single<Game> update(String gameId, Game game);
}
