package ru.mipt.bit.platformer;

public interface Command {
    void execute();
    boolean canExecute();
}
