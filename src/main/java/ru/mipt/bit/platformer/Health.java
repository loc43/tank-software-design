package ru.mipt.bit.platformer;

public interface Health {
    int getHealth();
    int getMaxHealth();
    void setHealth(int health);
    boolean isAlive();
}
