package Main;

import javax.vecmath.Vector2d;
import java.lang.reflect.InvocationTargetException;

public class Player {
    private int playerID;
    public int getPlayerID() { return playerID; }
    public void setPlayerID(int newPlayerID) { playerID = newPlayerID; }
    private int teamID;
    public int getTeamID() { return teamID; }
    public void setTeamID(int newTeamID) { teamID = newTeamID; }


    private Vector2d position;
    public Vector2d getPosition() { return new Vector2d(position); }
    private Vector2d velocity;
    public Vector2d getVelocity() { return new Vector2d(velocity); }
    private Vector2d goalPosition;
    public Vector2d getGoalPosition() { return new Vector2d(goalPosition); }

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
        Vector2d newVelocity = new Vector2d(velocity);
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
        Vector2d distanceTravelled = new Vector2d(velocity);
        distanceTravelled.scale(deltaTime/1000f);
        position.add(distanceTravelled);
    }

    private Vector2d calculateAcceleration(){
        // a = (direction to goal - velocity) * maxAcceleration / (|direction to goal - velocity|)
        Vector2d direction = new Vector2d(goalPosition);
        direction.sub(position);

        // a = direction
        Vector2d acceleration = new Vector2d(direction);

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

    public synchronized void setGoalPosition(Vector2d goalPosition) {
        this.goalPosition = goalPosition;
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    @Override
    public Player clone() throws CloneNotSupportedException {
        Player p = null;
        try {
            p = getClass().getDeclaredConstructor
                    (int.class, position.getClass(), velocity.getClass(), goalPosition.getClass())
                    .newInstance(playerID, getPosition(), getVelocity(), getGoalPosition());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } finally {
            if (p == null) {
                p = new Player(getPlayerID(), getPosition(), getVelocity(), getGoalPosition());
            }
        }
        p.setTeamID(teamID);
        p.selected = selected;
        return p;
    }
}
