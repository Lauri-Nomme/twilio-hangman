package io.github.unapplicable.hangman.service;

import rx.Single;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

public class MemoryPlayerStorage implements PlayerStorage {
    private Map<String, Player> players = new HashMap<>();

    public Single<Player> fetch(String playerId) {
        Player player = players.get(playerId);
        if (null == player) {
            return Single.error(new NoSuchElementException());
        }

        return Single.just(player);
    }

    public Single<Player> create(Player player) {
        Boolean duplicateName = players.values().stream().anyMatch(p -> player.getName().equals(p.getName()));
        if (duplicateName) {
            return Single.error(new RuntimeException("Player name taken"));
        }

        players.put(String.valueOf(players.size() + 1), player);

        return Single.just(player);
    }
}
