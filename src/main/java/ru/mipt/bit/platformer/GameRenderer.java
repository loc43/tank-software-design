package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapRenderer;

import java.util.List;

import static ru.mipt.bit.platformer.util.GdxGameUtils.drawTextureRegionUnscaled;

public class GameRenderer implements GameLevelListener {
    private final Batch batch;
    private final MapRenderer levelRenderer;
    private List<GameObject> gameObjects;

    public GameRenderer(Batch batch, MapRenderer levelRenderer, List<GameObject> gameObjects) {
        this.batch = batch;
        this.levelRenderer = levelRenderer;
        this.gameObjects = gameObjects;
    }

    public void render() {
        levelRenderer.render();
        batch.begin();
        for (GameObject gameObject : gameObjects) {
            drawTextureRegionUnscaled(batch, gameObject.getGraphics(), gameObject.getRectangle(), gameObject.getRotation());
        }

        for (GameObject gameObject : gameObjects) {
            if (gameObject instanceof HealthBarDecorator) {
                ((HealthBarDecorator) gameObject).renderHealthBar(batch);
            }
        }
        batch.end();
    }

    @Override
    public void objectAdded(GameObject gameObject) {
        gameObjects.add(gameObject);
    }

    @Override
    public void objectRemoved(GameObject gameObject) {
        gameObjects.remove(gameObject);
    }
}
