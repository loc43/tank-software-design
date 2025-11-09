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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

interface Command {
    void execute();
    boolean canExecute();
}


class MoveCommand implements Command {
    private final GameObject gameObject;
    private final Direction direction;
    private final List<GameObject> allObjects;
    private final TiledMapTileLayer groundLayer;

    public MoveCommand(GameObject gameObject, Direction direction, List<GameObject> allObjects, TiledMapTileLayer groundLayer) {
        this.gameObject = gameObject;
        this.direction = direction;
        this.allObjects = allObjects;
        this.groundLayer = groundLayer;
    }

    @Override
    public void execute() {
        if (!canExecute()) return;

        GridPoint2 offset = direction.getMovementOffset();
        GridPoint2 targetPosition = new GridPoint2(
            gameObject.getCoordinates().x + offset.x,
            gameObject.getCoordinates().y + offset.y
        );

        if (gameObject instanceof Player) {
            Player player = (Player) gameObject;
            player.setDestinationCoordinates(targetPosition);
            player.setMovementProgress(0f);
            player.setRotation(direction);
        }
        else if (gameObject instanceof AITank) {
            AITank aiTank = (AITank) gameObject;
            aiTank.setDestinationCoordinates(targetPosition);
            aiTank.setMovementProgress(0f);
            aiTank.setRotation(direction);
        }
    }

    @Override
    public boolean canExecute() {
        if (gameObject instanceof Player) {
            Player player = (Player) gameObject;
            if (!isEqual(player.getMovementProgress(), 1f)) return false;
        } else if (gameObject instanceof AITank) {
            AITank aiTank = (AITank) gameObject;
            if (!isEqual(aiTank.getMovementProgress(), 1f)) return false;
        }

        GridPoint2 offset = direction.getMovementOffset();
        GridPoint2 targetPosition = new GridPoint2(
            gameObject.getCoordinates().x + offset.x,
            gameObject.getCoordinates().y + offset.y
        );

        if (targetPosition.x < 0 || targetPosition.x >= groundLayer.getWidth() ||
            targetPosition.y < 0 || targetPosition.y >= groundLayer.getHeight()) {
            return false;
        }

        for (GameObject obj : allObjects) {
            if (obj == gameObject) continue; 
            
            if (obj.getCoordinates().equals(targetPosition)) {
                return false;
            }
            
            if (obj instanceof Player) {
                Player p = (Player) obj;
                if (p.getDestinationCoordinates().equals(targetPosition) && !isEqual(p.getMovementProgress(), 1f)) {
                    return false;
                }
            } else if (obj instanceof AITank) {
                AITank ai = (AITank) obj;
                if (ai.getDestinationCoordinates().equals(targetPosition) && !isEqual(ai.getMovementProgress(), 1f)) {
                    return false;
                }
            }
        }

        return true;
    }
}

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

class AITank implements GameObject {
    private Texture texture;
    private TextureRegion graphics;
    private Rectangle rectangle;
    private GridPoint2 coordinates;
    private GridPoint2 destinationCoordinates;
    private float movementProgress = 1f;
    private Direction rotation;
    private Random random = new Random();
    private float timeSinceLastMove = 0f;

    public AITank(String texturePath, GridPoint2 startPosition) {
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
    private final List<GameObject> allObjects; 
    private final TiledMapTileLayer groundLayer; 

    public PlayerController(Player player, InputHandler inputHandler, TileMovement tileMovement, float movementSpeed) {
        this.player = player;
        this.inputHandler = inputHandler;
        this.tileMovement = tileMovement;
        this.movementSpeed = movementSpeed;
        this.allObjects = allObjects;
        this.groundLayer = groundLayer;
    }

    public void update(float deltaTime) {
        handleInput();
        updateMovement(deltaTime);
    }

    private void handleInput() {
        if (isEqual(player.getMovementProgress(), 1f)) {
            Direction direction = getMovementDirection();
            if (direction != null) {
                Command moveCommand = new MoveCommand(player, direction, allObjects, groundLayer);
                if (moveCommand.canExecute()) {
                    moveCommand.execute();
                }
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

class AIController {
    private final AITank tank;
    private final List<GameObject> allObjects;
    private final TiledMapTileLayer groundLayer;

    public AIController(AITank tank, List<GameObject> allObjects, TiledMapTileLayer groundLayer) {
        this.tank = tank;
        this.allObjects = allObjects;
        this.groundLayer = groundLayer;
    }

    public void update(float deltaTime) {
        tank.update(deltaTime);

        if (tank.shouldMakeDecision()) {
            makeRandomMove();
            tank.resetDecisionTimer();
        }
    }

    private void makeRandomMove() {
        for (int i = 0; i < 4; i++) {
            Direction direction = tank.getRandomDirection();
            Command moveCommand = new MoveCommand(tank, direction, allObjects, groundLayer);
            if (moveCommand.canExecute()) {
                moveCommand.execute();
                break;
            }
        }
    }
}


public class GameDesktopLauncher implements ApplicationListener {

    private static final float MOVEMENT_SPEED = 0.4f;
    private static final String LEVEL_PATH = "level.tmx";
    private static final String PLAYER_TEXTURE = "images/tank_blue.png";
    private static final String AITANK_TEXTURE = "images/tank_blue.png";
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
    private void initializeGameObjects() {
        gameObjects = new ArrayList<>();
        aiTanks = new ArrayList<>();
        aiControllers = new ArrayList<>();

        Random random = new Random();
        
        player = new Player(PLAYER_TEXTURE, new GridPoint2(2, 2));
        gameObjects.add(player);
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
                AITank aiTank = new AITank(AITANK_TEXTURE, new GridPoint2(x, y));
                aiTanks.add(aiTank);
                gameObjects.add(aiTank);
            }
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
        
        for (AITank aiTank : aiTanks) {
            AIController aiController = new AIController(aiTank, gameObjects, groundLayer);
            aiControllers.add(aiController);
        }
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

    @Override
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0.2f, 1f);
        Gdx.gl.glClear(GL_COLOR_BUFFER_BIT);

        float deltaTime = Gdx.graphics.getDeltaTime();
        
        playerController.update(deltaTime);
        
        for (AIController aiController : aiControllers) {
            aiController.update(deltaTime);
            updateTankMovement(aiController.tank, deltaTime);
        }

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
