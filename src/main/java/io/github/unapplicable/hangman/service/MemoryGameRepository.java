package io.github.unapplicable.hangman.service;

import io.github.unapplicable.hangman.service.error.NotFound;
import javafx.util.Pair;
import rx.Single;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MemoryGameRepository implements GameRepository {
    private final Map<String, Game> games = new HashMap<>();

    public Single<Game> fetch(String gameId) {
        Game game = games.get(gameId);
        if (null == game) {
            return Single.error(new NotFound("Game not found, id " + gameId));
        }

        return Single.just(game);
    }

    public Single<Game> update(String gameId, Game game) {
        games.replace(gameId, game);

        return Single.just(game);
    }

    public Single<Pair<String, Game>> create(Game game) {
        String id = UUID.randomUUID().toString();
        games.put(id, game);

        return Single.just(new Pair<>(id, game));
    }
}
