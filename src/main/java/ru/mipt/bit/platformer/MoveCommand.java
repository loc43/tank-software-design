package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;

import java.util.List;

import static ru.mipt.bit.platformer.util.GdxGameUtils.isEqual;

public class MoveCommand implements Command {
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
