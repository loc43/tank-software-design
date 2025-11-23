package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.GridPoint2;

import static ru.mipt.bit.platformer.util.GdxGameUtils.createBoundingRectangle;

public class Bullet implements GameObject, Health {
    private Texture texture;
    private TextureRegion graphics;
    private Rectangle rectangle;
    private GridPoint2 coordinates;
    private Direction direction;
    private int damage;
    private boolean alive;
    private GameLevel gameLevel;

    public Bullet(String texturePath, GridPoint2 startPosition, Direction direction, int damage, GameLevel gameLevel) {
        this.texture = new Texture(texturePath);
        this.graphics = new TextureRegion(texture);
        this.rectangle = createBoundingRectangle(graphics);
        this.coordinates = new GridPoint2(startPosition);
        this.direction = direction;
        this.damage = damage;
        this.alive = true;
        this.gameLevel = gameLevel;
    }

    @Override
    public TextureRegion getGraphics() { return graphics; }
    @Override
    public Rectangle getRectangle() { return rectangle; }
    @Override
    public GridPoint2 getCoordinates() { return coordinates; }
    @Override
    public float getRotation() { return direction.getRotation(); }
    
    @Override
    public void update(float deltaTime) {
        if (!alive) return;
        
        GridPoint2 offset = direction.getMovementOffset();
        GridPoint2 newPosition = new GridPoint2(
            coordinates.x + offset.x,
            coordinates.y + offset.y
        );
        
        if (newPosition.x < 0 || newPosition.x >= gameLevel.getGroundLayer().getWidth() ||
            newPosition.y < 0 || newPosition.y >= gameLevel.getGroundLayer().getHeight()) {
            alive = false;
            gameLevel.removeGameObject(this);
            return;
        }
        
        for (GameObject obj : gameLevel.getGameObjects()) {
            if (obj == this) continue;
            
            if (obj.getCoordinates().equals(newPosition)) {
                if (obj instanceof Health) {
                    Health healthObj = (Health) obj;
                    healthObj.setHealth(healthObj.getHealth() - damage);
                    if (!healthObj.isAlive()) {
                        gameLevel.removeGameObject((GameObject) healthObj);
                    }
                }
                alive = false;
                gameLevel.removeGameObject(this);
                return;
            }
        }
        
        coordinates.set(newPosition);
        gameLevel.moveRectangleAtTileCenter(rectangle, coordinates);
    }
    
    @Override
    public void dispose() { 
        if (texture != null) texture.dispose(); 
    }

    @Override
    public int getHealth() { return alive ? 1 : 0; }
    @Override
    public int getMaxHealth() { return 1; }
    @Override
    public void setHealth(int health) { 
        if (health <= 0) {
            alive = false;
            gameLevel.removeGameObject(this);
        }
    }
    @Override
    public boolean isAlive() { return alive; }
}
