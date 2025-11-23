package ru.mipt.bit.platformer;

import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import java.util.List;

public class AIController {
    private final AITank tank;
    private final GameLevel gameLevel;

    public AIController(AITank tank, GameLevel gameLevel) {
        this.tank = tank;
        this.gameLevel = gameLevel;
    }

    public void update(float deltaTime) {
        tank.update(deltaTime);

        if (tank.shouldMakeDecision()) {
            if (tank.shouldShoot()) {
                Command shootCommand = new ShootCommand(tank, gameLevel);
                if (shootCommand.canExecute()) {
                    shootCommand.execute();
                }
            } else {
                makeRandomMove();
            }
            tank.resetDecisionTimer();
        }
    }

    private void makeRandomMove() {
        for (int i = 0; i < 4; i++) {
            Direction direction = tank.getRandomDirection();
            Command moveCommand = new MoveCommand(tank, direction, gameLevel.getGameObjects(), gameLevel.getGroundLayer());
            if (moveCommand.canExecute()) {
                moveCommand.execute();
                break;
            }
        }
    }
}
