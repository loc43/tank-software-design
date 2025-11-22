package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.List;

import static ru.mipt.bit.platformer.util.GdxGameUtils.isEqual;

public class PlayerController {
    private final Player player;
    private final InputHandler inputHandler;
    private final TileMovement tileMovement;
    private final float movementSpeed;
    private final List<GameObject> allObjects;
    private final TiledMapTileLayer groundLayer;

    public PlayerController(Player player, InputHandler inputHandler, TileMovement tileMovement, 
                          float movementSpeed, List<GameObject> allObjects, TiledMapTileLayer groundLayer) {
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
