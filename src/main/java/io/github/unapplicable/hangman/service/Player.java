package io.github.unapplicable.hangman.service;

public class Player {
    private final String name;
    private final Integer age;

    public Player(String name, Integer age) {
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public Integer getAge() {
        return age;
    }
}
