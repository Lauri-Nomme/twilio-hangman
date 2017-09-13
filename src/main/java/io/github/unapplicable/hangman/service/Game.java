package io.github.unapplicable.hangman.service;

import io.github.unapplicable.hangman.service.error.BadRequest;
import rx.Single;

public class Game {
    private static final Integer INCORRECT_GUESS_LIMIT = 6;
    private static final char MASK_CHAR = '*';

    public enum Status {ongoing, won, lost}

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
        guessedWord = String.format("%" + this.word.length() + "s", "").replace(' ', MASK_CHAR);
        incorrectLetters = "";
    }

    protected Game(Game game, String letter) {
        player = game.player;
        guesses = game.guesses + 1;
        word = game.word;

        guessedWord = revealLetter(game.guessedWord, game.word, letter);
        Boolean correctGuess = !guessedWord.equals(game.guessedWord);
        if (!correctGuess) {
            incorrectLetters = game.incorrectLetters + letter;
            guessesLeft = game.guessesLeft - 1;
            gameStatus = 0 == guessesLeft ? Status.lost : game.gameStatus;
        } else {
            incorrectLetters = game.incorrectLetters;
            guessesLeft = game.guessesLeft;
            gameStatus = -1 == guessedWord.indexOf(MASK_CHAR) ? Status.won : game.gameStatus;
        }
    }

    private static String revealLetter(String gw, String w, String letter) {
        StringBuilder guessedWordBuilder = new StringBuilder(gw);
        Integer matchPos = -1;

        while (-1 != (matchPos = w.indexOf(letter, matchPos + 1))) {
            guessedWordBuilder.setCharAt(matchPos, w.charAt(matchPos));
        }

        return guessedWordBuilder.toString();
    }

    public Single<Game> guess(String letter) {
        if (gameStatus != Status.ongoing) {
            return Single.error(new BadRequest("Game status not ongoing"));
        }

        if (1 != letter.length()) {
            return Single.error(new BadRequest("Letter not single character"));
        }

        return Single.just(new Game(this, letter));
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
