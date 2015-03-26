package Main;

import javax.vecmath.Vector2d;

public class StationaryPlayer extends Player {
    public StationaryPlayer(int playerID, Vector2d position, Vector2d velocity, Vector2d goalPosition) {
        super(playerID, position, velocity, goalPosition);
    }

    @Override
    public synchronized void update(int deltaTime) {
        // does not allow movement
    }
}
