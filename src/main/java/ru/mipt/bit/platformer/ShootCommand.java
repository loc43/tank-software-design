package ru.mipt.bit.platformer;

import com.badlogic.gdx.math.GridPoint2;

public class ShootCommand implements Command {
    private final GameObject gameObject;
    private final GameLevel gameLevel;

    public ShootCommand(GameObject gameObject, GameLevel gameLevel) {
        this.gameObject = gameObject;
        this.gameLevel = gameLevel;
    }

    @Override
    public void execute() {
        if (!canExecute()) return;

        Direction direction;
        if (gameObject instanceof Player) {
            Player player = (Player) gameObject;
            direction = player.getRotationDirection();
        } else if (gameObject instanceof AITank) {
            AITank aiTank = (AITank) gameObject;
            direction = aiTank.getRotationDirection();
        } else {
            return;
        }

        GridPoint2 bulletPosition = new GridPoint2(
            gameObject.getCoordinates().x + direction.getMovementOffset().x,
            gameObject.getCoordinates().y + direction.getMovementOffset().y
        );

        Bullet bullet = new Bullet("images/bullet.png", bulletPosition, direction, 25, gameLevel);
        gameLevel.addGameObject(bullet);
    }

    @Override
    public boolean canExecute() {
        if (gameObject instanceof Player) {
            Player player = (Player) gameObject;
            return player.isAlive();
        } else if (gameObject instanceof AITank) {
            AITank aiTank = (AITank) gameObject;
            return aiTank.isAlive();
        }
        return false;
    }
}
