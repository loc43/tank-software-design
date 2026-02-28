package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.maps.MapRenderer;

import java.util.List;

import static ru.mipt.bit.platformer.util.GdxGameUtils.drawTextureRegionUnscaled;

public class GameRenderer {
    private final Batch batch;
    private final MapRenderer levelRenderer;

    public GameRenderer(Batch batch, MapRenderer levelRenderer) {
        this.batch = batch;
        this.levelRenderer = levelRenderer;
    }

    public void render(List<GameObject> gameObjects) {
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
}
