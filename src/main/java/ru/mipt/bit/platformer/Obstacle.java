package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.GridPoint2;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createBoundingRectangle;

public class Obstacle implements GameObject {
    private Texture texture;
    private TextureRegion graphics;
    private Rectangle rectangle;
    private GridPoint2 coordinates;

    public Obstacle(String texturePath, GridPoint2 position) {
        this.texture = new Texture(texturePath);
        this.graphics = new TextureRegion(texture);
        this.rectangle = createBoundingRectangle(graphics);
        this.coordinates = new GridPoint2(position);
    }

    @Override
    public TextureRegion getGraphics() { return graphics; }
    @Override
    public Rectangle getRectangle() { return rectangle; }
    @Override
    public GridPoint2 getCoordinates() { return coordinates; }
    @Override
    public float getRotation() { return 0f; }
    @Override
    public void update(float deltaTime) { }
    @Override
    public void dispose() { if (texture != null) texture.dispose(); }
}
