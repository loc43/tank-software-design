package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.GridPoint2;

public interface GameObject {
    TextureRegion getGraphics();
    Rectangle getRectangle();
    GridPoint2 getCoordinates();
    float getRotation();
    void update(float deltaTime);
    void dispose();
}
