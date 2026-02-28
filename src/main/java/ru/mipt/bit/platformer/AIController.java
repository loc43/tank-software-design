package ru.mipt.bit.platformer;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import java.util.List;

public class AIController {
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
