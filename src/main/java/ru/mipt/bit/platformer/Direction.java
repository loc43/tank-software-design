package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;

public enum Direction {
    UP(90f),
    RIGHT(0f),
    DOWN(-90f),
    LEFT(-180f);

    private final float rotation;

    Direction(float rotation) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }

    public GridPoint2 getMovementOffset() {
        switch (this) {
            case UP: return new GridPoint2(0, 1);
            case RIGHT: return new GridPoint2(1, 0);
            case DOWN: return new GridPoint2(0, -1);
            case LEFT: return new GridPoint2(-1, 0);
            default: return new GridPoint2(0, 0);
        }
    }
}
