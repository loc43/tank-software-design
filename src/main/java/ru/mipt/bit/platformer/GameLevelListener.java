package ru.mipt.bit.platformer;

public interface GameLevelListener {
    void objectAdded(GameObject gameObject);
    void objectRemoved(GameObject gameObject);
}
