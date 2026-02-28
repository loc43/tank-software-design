package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.GridPoint2;

import java.util.Random;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createBoundingRectangle;

public class AITank implements GameObject, Health {
    private Texture texture;
    private TextureRegion graphics;
    private Rectangle rectangle;
    private GridPoint2 coordinates;
    private GridPoint2 destinationCoordinates;
    private float movementProgress = 1f;
    private Direction rotation;
    private Random random = new Random();
    private float timeSinceLastMove = 0f;
    private int health;
    private int maxHealth;

    public AITank(String texturePath, GridPoint2 startPosition) {
        this.texture = new Texture(texturePath);
        this.graphics = new TextureRegion(texture);
        this.rectangle = createBoundingRectangle(graphics);
        this.coordinates = new GridPoint2(startPosition);
        this.destinationCoordinates = new GridPoint2(startPosition);
        this.rotation = Direction.RIGHT;
        this.maxHealth = 100;
        this.health = 80 + random.nextInt(21);
    }

    @Override
    public TextureRegion getGraphics() { return graphics; }
    @Override
    public Rectangle getRectangle() { return rectangle; }
    @Override
    public GridPoint2 getCoordinates() { return coordinates; }
    @Override
    public float getRotation() { return rotation.getRotation(); }
    @Override
    public void update(float deltaTime) {
        timeSinceLastMove += deltaTime;
    }
    @Override
    public void dispose() { if (texture != null) texture.dispose(); }

    public GridPoint2 getDestinationCoordinates() { return destinationCoordinates; }
    public void setDestinationCoordinates(GridPoint2 destinationCoordinates) { this.destinationCoordinates = destinationCoordinates; }
    public float getMovementProgress() { return movementProgress; }
    public void setMovementProgress(float movementProgress) { this.movementProgress = movementProgress; }
    public Direction getRotationDirection() { return rotation; }
    public void setRotation(Direction rotation) { this.rotation = rotation; }
    
    public boolean shouldMakeDecision() {
        return timeSinceLastMove > 1f;
    }
    
    public void resetDecisionTimer() {
        timeSinceLastMove = 0f;
    }
    
    public Direction getRandomDirection() {
        Direction[] directions = Direction.values();
        return directions[random.nextInt(directions.length)];
    }

    @Override
    public int getHealth() { return health; }
    @Override
    public int getMaxHealth() { return maxHealth; }
    @Override
    public void setHealth(int health) { this.health = health; }
    @Override
    public boolean isAlive() { return health > 0; }
}
