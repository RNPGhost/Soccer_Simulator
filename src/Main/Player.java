package Main;

import javax.vecmath.Vector2d;

public class Player {
    public int playerID;
    public int teamID;


    private Vector2d position;
    public Vector2d getPosition() { return new Vector2d(position.x,position.y); }

    private Vector2d velocity;
    private Vector2d goalPosition;
    private double maxAcceleration = 75;
    private double maxVelocity = 50;
    public boolean selected = false;

    public Player(int playerID, Vector2d position, Vector2d velocity, Vector2d goalPosition) {
        this.playerID = playerID;
        this.position = position;
        this.velocity = velocity;
        this.goalPosition = goalPosition;
    }

    /**
     *
     * @param deltaTime is time since last update in milliseconds
     */
    public synchronized void update(int deltaTime) {
        // calculate required acceleration
        Vector2d acceleration = calculateAcceleration();

        // apply acceleration to velocity
            // calculate the change in velocity over deltaTime
        acceleration.scale(deltaTime / 1000f);
            // add the difference in velocity to the current velocity
        Vector2d newVelocity = new Vector2d(velocity.x,velocity.y);
        newVelocity.add(acceleration);
            // if no acceleration, then we are at the goal
        if (acceleration.length() == 0) {
            velocity = new Vector2d(0,0);
            // if our new velocity does not break the max velocity
        } else if (newVelocity.length() <= maxVelocity) {
            velocity.add(acceleration);
            // otherwise, scale the new velocity to conserve max velocity
        } else {
            newVelocity.scale(maxVelocity / newVelocity.length());
            velocity = newVelocity;
        }

        // apply velocity to position
        Vector2d distanceTravelled = new Vector2d(velocity.x,velocity.y);
        distanceTravelled.scale(deltaTime/1000f);
        position.add(distanceTravelled);
    }

    private Vector2d calculateAcceleration(){
        // a = (direction to goal - velocity) * maxAcceleration / (|direction to goal - velocity|)
        Vector2d direction = new Vector2d(goalPosition.x,goalPosition.y);
        direction.sub(position);

        // a = direction
        Vector2d acceleration = direction;

        // a = direction - velocity
        acceleration.sub(velocity);

        // if the player is close the goal and travelling slowly or
        // the acceleration is very small
        if (direction.length() < 1 && velocity.length() < 5 ||
                acceleration.length() < 0.01) {
            return new Vector2d(0,0);
        }

        // a = (direction to goal - velocity) * maxAcceleration / (|direction to goal - velocity|)
        acceleration.normalize();
        acceleration.scale(maxAcceleration);

        return acceleration;
    }

    public synchronized boolean setGoalPosition(Vector2d goalPosition) {
        boolean insidePitch = validGoalPosition(goalPosition);
        if (insidePitch) {
            this.goalPosition = goalPosition;
            return true;
        }
        return false;
    }

    private boolean validGoalPosition(Vector2d goalPosition) {
        return Pitch.insidePitch(goalPosition);
    }

    public Player clone() {
        return new Player(playerID, new Vector2d(position.x,position.y), new Vector2d(velocity.x,velocity.y),
                new Vector2d(goalPosition.x,goalPosition.y));
    }
}
