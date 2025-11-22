package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.List;

import static ru.mipt.bit.platformer.util.GdxGameUtils.isEqual;

public class PlayerController {
    private final Player player;
    private final KeyboardInputHandler inputHandler;
    private final TileMovement tileMovement;
    private final float movementSpeed;
    private final GameLevel gameLevel;
    private boolean spacePressed = false;

    public PlayerController(Player player, KeyboardInputHandler inputHandler, TileMovement tileMovement, 
                          float movementSpeed, GameLevel gameLevel) {
        this.player = player;
        this.inputHandler = inputHandler;
        this.tileMovement = tileMovement;
        this.movementSpeed = movementSpeed;
        this.gameLevel = gameLevel;
    }

    public void update(float deltaTime) {
        handleInput();
        updateMovement(deltaTime);
    }

    private void handleInput() {
        if (isEqual(player.getMovementProgress(), 1f)) {
            Direction direction = getMovementDirection();
            if (direction != null) {
                Command moveCommand = new MoveCommand(player, direction, gameLevel.getGameObjects(), gameLevel.getGroundLayer());
                if (moveCommand.canExecute()) {
                    moveCommand.execute();
                }
            }
        }
        
        if (inputHandler.isShoot()) {
            if (!spacePressed) {
                Command shootCommand = new ShootCommand(player, gameLevel);
                if (shootCommand.canExecute()) {
                    shootCommand.execute();
                }
                spacePressed = true;
            }
        } else {
            spacePressed = false;
        }
    }

    private Direction getMovementDirection() {
        if (inputHandler.isMoveUp()) return Direction.UP;
        if (inputHandler.isMoveLeft()) return Direction.LEFT;
        if (inputHandler.isMoveDown()) return Direction.DOWN;
        if (inputHandler.isMoveRight()) return Direction.RIGHT;
        return null;
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
