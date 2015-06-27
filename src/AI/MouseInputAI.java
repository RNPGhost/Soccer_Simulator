package AI;

import Graphics.Tools;
import Main.Pitch;
import Main.Player;
import Main.Team;

import javax.media.opengl.awt.GLCanvas;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.List;

public class MouseInputAI implements AI {
    private Team team;
    Pitch pitch;

    public MouseInputAI() {
        MouseInput mouseInput = new MouseInput();
        mouseInput.initialise(this);
    }

    public void selectPlayer(Point p) {
        Vector2d clickPosition = convertPointToVector(p);

        List<Player> players = team.getCopyOfPlayers();

        // find the closest player to the click
        double distance = Double.POSITIVE_INFINITY;
        int playerID = -1;
        for (Player player : players) {
            Vector2d direction = player.getPosition();
            direction.sub(clickPosition);
            double newDistance = direction.length();
            if (newDistance < distance) {
                playerID = player.getPlayerID();
                distance = newDistance;
            }
        }
        if (playerID >= 0) { team.selectPlayer(playerID); }
    }

    private Vector2d convertPointToVector(Point p) {
        GLCanvas canvas = Tools.canvas;
        double vectorX = (p.getX() - canvas.getWidth()/2) * Tools.maxX / canvas.getWidth();
        double vectorY = (p.getY() - canvas.getHeight()/2) * (-Tools.maxY) / canvas.getHeight();
        return new Vector2d(vectorX,vectorY);
    }

    public void setGoalPosition(Point p) {
        if (team.isPlayerSelected()) {
            int selectedPlayerID = team.getSelectedPlayerID();
            Vector2d goalPosition = convertPointToVector(p);
            team.setPlayerGoalPosition(selectedPlayerID,goalPosition);
        }
    }

    public void interceptBall() {
        if (team.isPlayerSelected()) {
            int selectedPlayerID = team.getSelectedPlayerID();
            interceptBall(selectedPlayerID);
        }
    }

    private void interceptBall(int playerID) {
        Vector2d intersectionPoint = findIntersectionPoint(playerID);

        List<Player> players = team.getCopyOfPlayers();

        // make the player run past the interception point so that
        // the player intercepts the ball at full speed
        Vector2d runDirection = new Vector2d(intersectionPoint);
        runDirection.sub(players.get(playerID).getPosition());
        runDirection.normalize();
        runDirection.scale(20.0);
        intersectionPoint.add(runDirection);

        team.setPlayerGoalPosition(playerID, intersectionPoint);
    }


    private Vector2d findIntersectionPoint(int playerID) {
        // calculate minimum distance the ball will travel before the player is able to reach it

        Pitch pitch = team.getPitch();

        double lowerBound = 0;
        // maximum possible distance the ball can travel with a = -v/3 is 3 * ball velocity
        double upperBound = 3 * pitch.getBallVelocity().length();
        // we test at distance of player from ball to ensure we get lowest x possible
        Vector2d ballToPlayer = team.getPlayerPosition(playerID);
        ballToPlayer.sub(pitch.getBallPosition());
        double newX = ballToPlayer.length();
        double oldX = Double.POSITIVE_INFINITY;

        while (Math.abs(newX - oldX) > 1) {
            oldX = newX;
            Double ballTime = findBallTime(oldX);
            Double playerTime = findPlayerTime(oldX, playerID);
            if (playerTime >= ballTime) {
                lowerBound = oldX;
            } else {
                upperBound = oldX;
            }
            newX = (upperBound + lowerBound) / 2;
        }

        // calculate point x distance from the ball in the direction of travel
        Vector2d interPoint = pitch.getBallVelocity();
        interPoint.normalize();
        interPoint.scale(newX);
        interPoint.add(pitch.getBallPosition());

        return interPoint;
    }

    private double findBallTime(double x) {
        // time taken for ball to travel x distance is t = -3ln(1 - x/3u)
        // where u is the initial velocity of the ball

        return -3 * Math.log(1 - (x / (3 * pitch.getBallVelocity().length())));
    }

    private double findPlayerTime(double x, int playerID) {
        // time taken for player to reach a ball that has travelled distance x is
        // t = √(x^2+p^2+2x*p*cos(∝))/v
        // where p is the distance between the player and the ball and
        // where alpha is the angle at the ball between the player and the intersection and
        // where v is the maximum velocity of the player
        // this assumes that the player is already at maximum velocity in the correct direction

        List<Player> players = team.getCopyOfPlayers();

        Vector2d ballToPlayer = new Vector2d(team.getPlayerPosition(playerID));
        ballToPlayer.sub(pitch.getBallPosition());
        double p = ballToPlayer.length();
        double alpha = ballToPlayer.angle(pitch.getBallVelocity());
        double v = players.get(playerID).getMaxVelocity();

        return Math.sqrt(x * x + p * p - 2 * x * p * Math.cos(alpha)) / v;
    }

    public void kickBall(Point p) {
        if (team.getPitch().ballIsInPossession()) {
            Vector2d direction = convertPointToVector(p);
            direction.sub(team.getPitch().getBallPosition());
            team.kickBall(direction);
        }
    }

    private Vector2d getSelectedPlayerPosition() {
        for (Player p : team.getCopyOfPlayers()) {
            if (p.getPlayerID() == team.getSelectedPlayerID()) {
                return p.getPosition();
            }
        }
        return null;
    }

    public void updateTeam(Team t) {
        team = t;
        pitch = team.getPitch();
    }
}
