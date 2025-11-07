package ru.mipt.bit.platformer;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Rectangle;
import ru.mipt.bit.platformer.util.TileMovement;

import static com.badlogic.gdx.Input.Keys.*;
import static com.badlogic.gdx.graphics.GL20.GL_COLOR_BUFFER_BIT;
import static com.badlogic.gdx.math.MathUtils.isEqual;
import static ru.mipt.bit.platformer.util.GdxGameUtils.*;

interface GameObject {
    TextureRegion getGraphics();
    Rectangle getRectangle();
    GridPoint2 getCoordinates();
    float getRotation();
    void update(float deltaTime);
    void dispose();
}

interface InputHandler {
    boolean isMoveUp();
    boolean isMoveDown();
    boolean isMoveLeft();
    boolean isMoveRight();
}

class Player implements GameObject {
    private Texture texture;
    private TextureRegion graphics;
    private Rectangle rectangle;
    private GridPoint2 coordinates;
    private GridPoint2 destinationCoordinates;
    private float movementProgress = 1f;
    private Direction rotation;

    public Player(String texturePath, GridPoint2 startPosition) {
        this.texture = new Texture(texturePath);
        this.graphics = new TextureRegion(texture);
        this.rectangle = createBoundingRectangle(graphics);
        this.coordinates = new GridPoint2(startPosition);
        this.destinationCoordinates = new GridPoint2(startPosition);
        this.rotation = Direction.RIGHT;
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
    public void update(float deltaTime) { }
    @Override
    public void dispose() { if (texture != null) texture.dispose(); }

    public GridPoint2 getDestinationCoordinates() { return destinationCoordinates; }
    public void setDestinationCoordinates(GridPoint2 destinationCoordinates) { this.destinationCoordinates = destinationCoordinates; }
    public float getMovementProgress() { return movementProgress; }
    public void setMovementProgress(float movementProgress) { this.movementProgress = movementProgress; }
    public Direction getRotationDirection() { return rotation; }
    public void setRotation(Direction rotation) { this.rotation = rotation; }
}

class Obstacle implements GameObject {
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

class KeyboardInputHandler implements InputHandler {
    @Override
    public boolean isMoveUp() {
        return Gdx.input.isKeyPressed(UP) || Gdx.input.isKeyPressed(W);
    }
    @Override
    public boolean isMoveDown() {
        return Gdx.input.isKeyPressed(DOWN) || Gdx.input.isKeyPressed(S);
    }
    @Override
    public boolean isMoveLeft() {
        return Gdx.input.isKeyPressed(LEFT) || Gdx.input.isKeyPressed(A);
    }
    @Override
    public boolean isMoveRight() {
        return Gdx.input.isKeyPressed(RIGHT) || Gdx.input.isKeyPressed(D);
    }
}

class PlayerController {
    private final Player player;
    private final InputHandler inputHandler;
    private final TileMovement tileMovement;
    private final float movementSpeed;

    public PlayerController(Player player, InputHandler inputHandler, TileMovement tileMovement, float movementSpeed) {
        this.player = player;
        this.inputHandler = inputHandler;
        this.tileMovement = tileMovement;
        this.movementSpeed = movementSpeed;
    }

    public void update(float deltaTime) {
        handleInput();
        updateMovement(deltaTime);
    }

    private void handleInput() {
        if (isEqual(player.getMovementProgress(), 1f)) {
            Direction direction = getMovementDirection();
            if (direction != null) {
                tryMove(direction);
            }
        }
    }

    private Direction getMovementDirection() {
        if (inputHandler.isMoveUp()) return Direction.UP;
        if (inputHandler.isMoveLeft()) return Direction.LEFT;
        if (inputHandler.isMoveDown()) return Direction.DOWN;
        if (inputHandler.isMoveRight()) return Direction.RIGHT;
        return null;
    }

    private void tryMove(Direction direction) {
        GridPoint2 offset = direction.getMovementOffset();
        GridPoint2 targetPosition = new GridPoint2(
            player.getCoordinates().x + offset.x,
            player.getCoordinates().y + offset.y
        );
        
        // Здесь можно добавить проверку коллизий
        player.setDestinationCoordinates(targetPosition);
        player.setMovementProgress(0f);
        player.setRotation(direction);
    }

    private void updateMovement(float deltaTime) {
        float progress = continueProgress(player.getMovementProgress(), deltaTime, movementSpeed);
        player.setMovementProgress(progress);

        tileMovement.moveRectangleBetweenTileCenters(
            player.getRectangle(),
            player.getCoordinates(),
            player.getDestinationCoordinates(),
            progress
        );

        if (isEqual(progress, 1f)) {
            player.getCoordinates().set(player.getDestinationCoordinates());
        }
    }

    private float continueProgress(float progress, float deltaTime, float speed) {
        return Math.min(1f, progress + deltaTime / speed);
    }
}

class GameRenderer {
    private final Batch batch;
    private final MapRenderer levelRenderer;

    public GameRenderer(Batch batch, MapRenderer levelRenderer) {
        this.batch = batch;
        this.levelRenderer = levelRenderer;
    }

    public void render(java.util.List<GameObject> gameObjects) {
        levelRenderer.render();
        batch.begin();
        for (GameObject gameObject : gameObjects) {
            drawTextureRegionUnscaled(batch, gameObject.getGraphics(), gameObject.getRectangle(), gameObject.getRotation());
        }
        batch.end();
    }
}

public class GameDesktopLauncher implements ApplicationListener {

    private static final float MOVEMENT_SPEED = 0.4f;
    private static final String LEVEL_PATH = "level.tmx";
    private static final String PLAYER_TEXTURE = "images/tank_blue.png";
    private static final String OBSTACLE_TEXTURE = "images/greenTree.png";

    private Batch batch;
    private TiledMap level;
    private MapRenderer levelRenderer;
    private TileMovement tileMovement;

    private Player player;
    private java.util.List<GameObject> gameObjects;
    private InputHandler inputHandler;
    private PlayerController playerController;
    private GameRenderer gameRenderer;

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
        TiledMapTileLayer groundLayer = getSingleLayer(level);
        tileMovement = new TileMovement(groundLayer, Interpolation.smooth);
    }
    private void positionAllObjects() {
        TiledMapTileLayer groundLayer = getSingleLayer(level);
        for (GameObject obj : gameObjects) {
            moveRectangleAtTileCenter(groundLayer, obj.getRectangle(), obj.getCoordinates());
        }
    }
    
    private void initializeGameObjectsRandom() {
        gameObjects = new java.util.ArrayList<>();
        Random random = new Random(); 
        
        int playerX = random.nextInt(8); 
        int playerY = random.nextInt(6);
        player = new Player(PLAYER_TEXTURE, new GridPoint2(playerX, playerY));
        gameObjects.add(player);
        
        for (int x = 0; x < 10; x++) { 
            for (int y = 0; y < 8; y++) { 
                if (random.nextFloat() < 0.5f) {
                    if (x != playerX || y != playerY) {
                        Obstacle tree = new Obstacle(OBSTACLE_TEXTURE, new GridPoint2(x, y));
                        gameObjects.add(tree);
                    }
                }
            }
        }
        positionAllObjects();
    }

    private void initializeGameObjectsFromFile() {
        gameObjects = new ArrayList<>();
        List<String> levelLines = new ArrayList<>();
        
        BufferedReader reader = Files.newBufferedReader(Paths.get(LEVEL_FILE_PATH)) {
            String line;
            while ((line = reader.readLine()) != null) {
                levelLines.add(line);
            }
        }
        
        for (int y = 0; y < levelLines.size(); y++) {
            String line = levelLines.get(y);
            for (int x = 0; x < line.length(); x++) {
                char cell = line.charAt(x);
                
                switch (cell) {
                    case 'T':
                        Obstacle obstacle = new Obstacle(OBSTACLE_TEXTURE, new GridPoint2(x, levelLines.size() - 1 - y));
                        gameObjects.add(obstacle);
                        break;
                    case 'X':
                        player = new Player(PLAYER_TEXTURE, new GridPoint2(x, levelLines.size() - 1 - y));
                        gameObjects.add(player);
                        break;
                    case '_':
                        break;
                }
            }
            positionAllObjects();
        }
    
    private void initializeSystems() {
        inputHandler = new KeyboardInputHandler();
        playerController = new PlayerController(player, inputHandler, tileMovement, MOVEMENT_SPEED);
        gameRenderer = new GameRenderer(batch, levelRenderer);
    }

    @Override
    public void render() {
        // clear the screen
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();
        
        playerController.update(deltaTime);

        gameRenderer.render(gameObjects);
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
