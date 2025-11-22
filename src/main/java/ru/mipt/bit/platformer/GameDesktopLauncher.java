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
import com.badlogic.gdx.math.Interpolation;
import ru.mipt.bit.platformer.util.TileMovement;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.badlogic.gdx.Input.Keys.L;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

public class GameDesktopLauncher implements ApplicationListener {

    private static final float MOVEMENT_SPEED = 0.4f;
    private static final String LEVEL_PATH = "level.tmx";
    private static final String PLAYER_TEXTURE = "images/tank_blue.png";
    private static final String AI_TANK_TEXTURE = "images/tank_red.png";
    private static final String OBSTACLE_TEXTURE = "images/greenTree.png";
    private static final int NUM_AI_TANKS = 3;

    private Batch batch;
    private TiledMap level;
    private MapRenderer levelRenderer;
    private TileMovement tileMovement;
    private TiledMapTileLayer groundLayer;

    private Player player;
    private List<AITank> aiTanks;
    private List<GameObject> gameObjects;
    private InputHandler inputHandler;
    private PlayerController playerController;
    private GameRenderer gameRenderer;
    private List<AIController> aiControllers;
    private ToggleHealthBarsCommand toggleHealthBarsCommand;
    private boolean lKeyPressed = false;

    @Override
    public void create() {
        batch = new SpriteBatch();
        initializeLevel();
        initializeGameObjects();
        initializeSystems();
    }

    private void initializeLevel() {
        level = new TmxMapLoader().load(LEVEL_PATH);
        levelRenderer = createSingleLayerMapRenderer(level, batch);
        groundLayer = getSingleLayer(level);
        tileMovement = new TileMovement(groundLayer, Interpolation.smooth);
    }

    private void initializeGameObjects() {
        gameObjects = new ArrayList<>();
        aiTanks = new ArrayList<>();
        aiControllers = new ArrayList<>();

        Random random = new Random();
        
        Player playerObj = new Player(PLAYER_TEXTURE, new GridPoint2(2, 2));
        player = playerObj;
        gameObjects.add(new HealthBarDecorator(playerObj, playerObj));
        
        for (int i = 0; i < NUM_AI_TANKS; i++) {
            int x, y;
            boolean positionOk;
            int attempts = 0;
            
            do {
                positionOk = true;
                x = random.nextInt(8);
                y = random.nextInt(6);
                
                for (GameObject obj : gameObjects) {
                    if (obj.getCoordinates().x == x && obj.getCoordinates().y == y) {
                        positionOk = false;
                        break;
                    }
                }
                attempts++;
            } while (!positionOk && attempts < 20);
            
            if (positionOk) {
                AITank aiTank = new AITank(AI_TANK_TEXTURE, new GridPoint2(x, y));
                HealthBarDecorator decoratedTank = new HealthBarDecorator(aiTank, aiTank);
                aiTanks.add(aiTank);
                gameObjects.add(decoratedTank);
            }
        }
        
        gameObjects.add(new Obstacle(OBSTACLE_TEXTURE, new GridPoint2(5, 5)));
        gameObjects.add(new Obstacle(OBSTACLE_TEXTURE, new GridPoint2(7, 3)));

        positionAllObjects();
    }

    private void positionAllObjects() {
        for (GameObject obj : gameObjects) {
            moveRectangleAtTileCenter(groundLayer, obj.getRectangle(), obj.getCoordinates());
        }
    }

    private void initializeSystems() {
        inputHandler = new KeyboardInputHandler();
        playerController = new PlayerController(player, inputHandler, tileMovement, 
                                              MOVEMENT_SPEED, gameObjects, groundLayer);
        gameRenderer = new GameRenderer(batch, levelRenderer);
        toggleHealthBarsCommand = new ToggleHealthBarsCommand();
        
        for (AITank aiTank : aiTanks) {
            AIController aiController = new AIController(aiTank, gameObjects, groundLayer);
            aiControllers.add(aiController);
        }
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
            updateTankMovement(aiController.tank, deltaTime);
        }

        gameRenderer.render(gameObjects);
    }
    
    private void updateTankMovement(AITank tank, float deltaTime) {
        float progress = continueProgress(tank.getMovementProgress(), deltaTime, MOVEMENT_SPEED);
        tank.setMovementProgress(progress);

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
        // do not react to window resizing
    }

    @Override
    public void pause() {
        // game doesn't get paused
    }

    @Override
    public void resume() {
        // game doesn't get paused
    }

    @Override
    public void dispose() {
        for (GameObject gameObject : gameObjects) {
            gameObject.dispose();
        }
        level.dispose();
        batch.dispose();
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        // level width: 10 tiles x 128px, height: 8 tiles x 128px
        config.setWindowedMode(1280, 1024);
        new Lwjgl3Application(new GameDesktopLauncher(), config);
    }
}
