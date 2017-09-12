package io.github.unapplicable.hangman.service;

import io.github.unapplicable.hangman.service.error.BadRequest;
import io.github.unapplicable.hangman.service.error.NotFound;
import rx.Observable;
import rx.Single;

import java.util.HashMap;
import java.util.Map;

public class MemoryPlayerRepository implements PlayerRepository {
    private final Map<String, Player> players = new HashMap<>();

    public Single<Player> fetch(String playerId) {
        Player player = players.get(playerId);
        if (null == player) {
            return Single.error(new NotFound("Player not found, id " + playerId));
        }

        return Single.just(player);
    }

    public Single<Player> create(Player player) {
        Boolean duplicateName = players.containsKey(player.getName());
        if (duplicateName) {
            return Single.error(new BadRequest("Player name taken"));
        }

        players.put(player.getName(), player);

        return Single.just(player);
    }

    public Observable<Player> list() {
        return Observable.from(players.values());
    }
}
