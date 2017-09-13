package io.github.unapplicable.hangman.service;

import javafx.util.Pair;
import rx.Observable;
import rx.Single;

public interface GameService {
    Single<Pair<String, Game>> start(String playerName);

    Single<Game> guess(String gameId, String letter);

    Single<Game> giveUp(String gameId);

    Single<Game> fetch(String gameId);

    Observable<Game> list();
}
