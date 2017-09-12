package io.github.unapplicable.hangman.service;

public class Game {
    public enum Status {ongoing, won, lost};
    private final Player player;
    private final Status gameStatus;

    public Game(Player player) {
        this.player = player;
        gameStatus = Status.ongoing;
    }

    public Player getPlayer() {
        return player;
    }

    public Status getGameStatus() {
        return gameStatus;
    }
}
