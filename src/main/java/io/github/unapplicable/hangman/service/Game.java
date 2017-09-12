package io.github.unapplicable.hangman.service;

public class Game {
    private static final Integer INCORRECT_GUESS_LIMIT = 6;

    public enum Status {ongoing, won, lost};

    private final Player player;
    private final Integer guesses;
    private final Integer guessesLeft;
    private final String guessedWord;
    private final String incorrectLetters;
    private final Status gameStatus;
    private final String word;

    public Game(Player player, String word) {
        this.player = player;
        this.word = word;
        gameStatus = Status.ongoing;
        guesses = 0;
        guessesLeft = INCORRECT_GUESS_LIMIT;
        guessedWord = String.format("%" + this.word.length() + "s", "").replace(' ', '*');
        incorrectLetters = "";
    }

    public Integer getGuesses() {
        return guesses;
    }

    public Integer getGuessesLeft() {
        return guessesLeft;
    }

    public String getIncorrectLetters() {
        return incorrectLetters;
    }

    public String getGuessedWord() {
        return guessedWord;
    }

    public Player getPlayer() {
        return player;
    }

    public Status getGameStatus() {
        return gameStatus;
    }
}
