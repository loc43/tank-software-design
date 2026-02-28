package ru.mipt.bit.platformer;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.GridPoint2;

public class HealthBarDecorator implements GameObject {
    private GameObject decorated;
    private Health healthObject;
    private static boolean showHealthBars = false;
    
    public HealthBarDecorator(GameObject decorated, Health healthObject) {
        this.decorated = decorated;
        this.healthObject = healthObject;
    }
    
    public static void toggleHealthBars() {
        showHealthBars = !showHealthBars;
    }
    
    @Override
    public TextureRegion getGraphics() {
        return decorated.getGraphics();
    }
    
    @Override
    public Rectangle getRectangle() {
        return decorated.getRectangle();
    }
    
    @Override
    public GridPoint2 getCoordinates() {
        return decorated.getCoordinates();
    }
    
    @Override
    public float getRotation() {
        return decorated.getRotation();
    }
    
    @Override
    public void update(float deltaTime) {
        decorated.update(deltaTime);
    }
    
    @Override
    public void dispose() {
        decorated.dispose();
    }
    
    public void renderHealthBar(Batch batch) {
        if (!showHealthBars || !healthObject.isAlive()) return;
        
        Rectangle rect = getRectangle();
        float barWidth = rect.width;
        float barHeight = 5f;
        float barX = rect.x;
        float barY = rect.y + rect.height + 2f;
        batch.setColor(1, 0, 0, 1);
        batch.draw(getGraphics().getTexture(), barX, barY, barWidth, barHeight);
        
        float healthPercent = (float) healthObject.getHealth() / healthObject.getMaxHealth();
        batch.setColor(0, 1, 0, 1);
        batch.draw(getGraphics().getTexture(), barX, barY, barWidth * healthPercent, barHeight);
        
        batch.setColor(1, 1, 1, 1);
    }
}
