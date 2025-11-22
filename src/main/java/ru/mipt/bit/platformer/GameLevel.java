package ru.mipt.bit.platformer;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.GridPoint2;

import java.util.ArrayList;
import java.util.List;

public class GameLevel {
    private List<GameObject> gameObjects;
    private List<GameLevelListener> listeners;
    private TiledMapTileLayer groundLayer;

    public GameLevel(TiledMapTileLayer groundLayer) {
        this.gameObjects = new ArrayList<>();
        this.listeners = new ArrayList<>();
        this.groundLayer = groundLayer;
    }

    public void addGameObject(GameObject gameObject) {
        gameObjects.add(gameObject);
        moveRectangleAtTileCenter(groundLayer, gameObject.getRectangle(), gameObject.getCoordinates());
        for (GameLevelListener listener : listeners) {
            listener.objectAdded(gameObject);
        }
    }

    public void removeGameObject(GameObject gameObject) {
        gameObjects.remove(gameObject);
        for (GameLevelListener listener : listeners) {
            listener.objectRemoved(gameObject);
        }
    }

    public List<GameObject> getGameObjects() {
        return new ArrayList<>(gameObjects);
    }

    public TiledMapTileLayer getGroundLayer() {
        return groundLayer;
    }

    public void addListener(GameLevelListener listener) {
        listeners.add(listener);
    }

    public void removeListener(GameLevelListener listener) {
        listeners.remove(listener);
    }

    public void update(float deltaTime) {
        for (GameObject gameObject : new ArrayList<>(gameObjects)) {
            gameObject.update(deltaTime);
        }
    }

    private void moveRectangleAtTileCenter(TiledMapTileLayer groundLayer, Rectangle rectangle, GridPoint2 coordinates) {
        float tileWidth = groundLayer.getTileWidth();
        float tileHeight = groundLayer.getTileHeight();
        rectangle.setX(coordinates.x * tileWidth);
        rectangle.setY(coordinates.y * tileHeight);
    }
}
