package io.github.unapplicable.hangman.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WordList {
    private List<String> words;
    private PrimitiveIterator.OfInt randomIterator;

    public WordList(InputStream stream) throws IOException {
        Stream<String> stringStream = new BufferedReader(new InputStreamReader(stream)).lines();
        this.words = stringStream
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        randomIterator = new Random().ints(0, this.words.size()).iterator();
    }

    public String getRandom() {
        return words.get(randomIterator.nextInt());
    }
}
