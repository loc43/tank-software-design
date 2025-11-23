package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.GridPoint2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.util.List;

import static com.badlogic.gdx.Input.Keys.L;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public class GameDesktopLauncher implements ApplicationListener {

    private ApplicationContext context;
    private Batch batch;
    private TiledMap level;
    private MapRenderer levelRenderer;
    private TiledMapTileLayer groundLayer;

    private Player player;
    private List<AITank> aiTanks;
    private KeyboardInputHandler inputHandler;
    private PlayerController playerController;
    private GameRenderer gameRenderer;
    private List<AIController> aiControllers;
    private ToggleHealthBarsCommand toggleHealthBarsCommand;
    private GameLevel gameLevel;
    private boolean lKeyPressed = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        initializeLevel();
        
        context = new ClassPathXmlApplicationContext("application-context.xml");
        
        initializeBeans();
        initializeGameObjects();
    }

    private void initializeLevel() {
        level = new TmxMapLoader().load("level.tmx");
        levelRenderer = createSingleLayerMapRenderer(level, batch);
        groundLayer = getSingleLayer(level);
    }

    private void initializeBeans() {
        player = context.getBean("player", Player.class);
        inputHandler = context.getBean("keyboardInputHandler", KeyboardInputHandler.class);
        playerController = context.getBean("playerController", PlayerController.class);
        gameRenderer = context.getBean("gameRenderer", GameRenderer.class);
        toggleHealthBarsCommand = context.getBean("toggleHealthBarsCommand", ToggleHealthBarsCommand.class);
        aiTanks = context.getBean("aiTanks", List.class);
        aiControllers = context.getBean("aiControllers", List.class);
        gameLevel = context.getBean("gameLevel", GameLevel.class);
        
        gameLevel.addListener(gameRenderer);
    }

    private void initializeGameObjects() {
        GameObject playerWithHealthBar = context.getBean("playerWithHealthBar", HealthBarDecorator.class);
        gameLevel.addGameObject(playerWithHealthBar);

        for (AITank aiTank : aiTanks) {
            HealthBarDecorator decoratedTank = new HealthBarDecorator(aiTank, aiTank);
            gameLevel.addGameObject(decoratedTank);
        }

        gameLevel.addGameObject(new Obstacle("images/greenTree.png", new GridPoint2(5, 5)));
        gameLevel.addGameObject(new Obstacle("images/greenTree.png", new GridPoint2(7, 3)));
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();

        if (Gdx.input.isKeyPressed(L)) {
            if (!lKeyPressed && toggleHealthBarsCommand.canExecute()) {
                toggleHealthBarsCommand.execute();
                lKeyPressed = true;
            }
        } else {
            lKeyPressed = false;
        }
        
        playerController.update(deltaTime);
        
        for (AIController aiController : aiControllers) {
            aiController.update(deltaTime);
            updateTankMovement(aiController.getTank(), deltaTime);
        }

        gameLevel.update(deltaTime);
        gameRenderer.render();
    }
    
    private void updateTankMovement(AITank tank, float deltaTime) {
        float progress = continueProgress(tank.getMovementProgress(), deltaTime, 0.4f);
        tank.setMovementProgress(progress);

        TileMovement tileMovement = context.getBean("tileMovement", TileMovement.class);
        tileMovement.moveRectangleBetweenTileCenters(
            tank.getRectangle(),
            tank.getCoordinates(),
            tank.getDestinationCoordinates(),
            progress
        );

        if (isEqual(progress, 1f)) {
            tank.getCoordinates().set(tank.getDestinationCoordinates());
        }
    }
    
    private float continueProgress(float progress, float deltaTime, float speed) {
        return Math.min(1f, progress + deltaTime / speed);
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
        for (GameObject gameObject : gameLevel.getGameObjects()) {
            gameObject.dispose();
        }
        level.dispose();
        batch.dispose();
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setWindowedMode(1280, 1024);
        new Lwjgl3Application(new GameDesktopLauncher(), config);
    }
}
