package AI;

import Main.Goalkeeper;
import Main.Pitch;
import Main.Player;
import Main.Team;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BasicAI implements AI {

    private Pitch pitch;
    // if a new team is given to the AI,
    // it is stored here until the AI is ready to update the team
    private Team newTeam;
    private Team team;
    private List<Player> players;

    @Override
    public void updateTeam(Team t) {
        newTeam = t;
    }

    public BasicAI() {
        createActionsTimer();
    }

    private void createActionsTimer() {
        int period = 50;
        Timer actionTimer = new Timer();
        TimerTask actions = new ActionsTask();
        actionTimer.schedule(actions, 0, period);
    }

    class ActionsTask extends TimerTask {
        public void run() {
            // don't do anything until we've been given a team
            if (newTeam == null) {
                return;
            }
            // update the team and pitch
            if (newTeam != team) {
                team = newTeam;
                pitch = team.getPitch();
            }
            // update the players
            players = team.getCopyOfPlayers();

            setGoalKeeperGoalPosition();
            setPlayerGoalPositions();
        }
    }

    private void setGoalKeeperGoalPosition() {
        if (pitch.ballIsInPossession()) {
            setGoalKeeperGoalPositionInPossession();
        } else {
            setGoalKeeperGoalPositionNotInPossession();
        }
    }


    private void setGoalKeeperGoalPositionInPossession() {
        // if the ball is in possession,
        // the goal keeper will remain goalWidth/2 distance away from the centre of the goal
        // and will remain between the ball and the centre of the goal

        Vector2d ballPosition = pitch.getBallPosition();

        double xGoalLine = Pitch.width / 2;
        if (players.get(team.getGoalKeeperID()).getPosition().getX() < 0) {
            xGoalLine = -xGoalLine;
        }

        Vector2d goalCentreToBall = new Vector2d(ballPosition.getX() - xGoalLine, ballPosition.getY());
        goalCentreToBall.add(pitch.getBallVelocity());

        double angle = new Vector2d(0, -1).angle(goalCentreToBall);
        double yOffset = Math.cos(angle) * Pitch.goalWidth / 2;
        double xOffset = Math.sin(angle) * Pitch.goalWidth / 2;

        double xGoalPosition = xGoalLine - xOffset;
        double yGoalPosition = -yOffset;

        team.setPlayerGoalPosition(team.getGoalKeeperID(), new Vector2d(xGoalPosition, yGoalPosition));
    }

    private void setGoalKeeperGoalPositionNotInPossession() {
        // if the ball is not in possession, try to intercept it at the earliest possible point

        interceptBall(team.getGoalKeeperID());
    }

    private void interceptBall(int playerID) {
        Vector2d intersectionPoint = findIntersectionPoint(playerID);

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

        Vector2d ballToPlayer = new Vector2d(team.getPlayerPosition(playerID));
        ballToPlayer.sub(pitch.getBallPosition());
        double p = ballToPlayer.length();
        double alpha = ballToPlayer.angle(pitch.getBallVelocity());
        double v = players.get(playerID).getMaxVelocity();

        return Math.sqrt(x * x + p * p - 2 * x * p * Math.cos(alpha)) / v;
    }

    private void setPlayerGoalPositions() {
        if (pitch.ballIsInPossession()) {
            if (pitch.getBallPossessorTeamID() == team.getTeamID()) {
                setPlayerGoalPositionsInPossession();
            } else {
                setPlayerGoalPositionsEnemyInPossession();
            }
        } else {
            setPlayerGoalPositionsNotInPossession();
        }
    }

    private void setPlayerGoalPositionsInPossession() {
        return;
    }

    private void setPlayerGoalPositionsEnemyInPossession() {
        // find the closest player to each opponent and
        // assign that player to mark that opponent

        int opponentID = getOpponentID();
        List<Player> opponents = pitch.getCopyOfPlayers(opponentID);
        List<Player> playersCopy = team.getCopyOfPlayers();

        playersCopy = removeGoalKeepers(playersCopy);

        for (Player player: opponents) {
            mark(playersCopy,player);
        }
    }

    private int getOpponentID() {
        if (pitch.getTeam1ID() == team.getTeamID()) {
            return pitch.getTeam2ID();
        } else {
            return pitch.getTeam1ID();
        }
    }

    private List<Player> removeGoalKeepers(List<Player> players) {
        List<Player> newPlayers = new ArrayList<Player>();
        for (Player player: players) {
            if (!(player instanceof Goalkeeper)) {
                newPlayers.add(player.getPlayerID(),player);
            }
        }

        return newPlayers;
    }

    private void mark(List<Player> players, Player mark) {
        // mark the player using one of the players in the list

        if (players.size() == 0) { return; }

        Player defender = findNearest(players, mark);

        players.remove(defender);

        // find the vector from the mark to the ball
        Vector2d markToBall = pitch.getBallPosition();
        markToBall.sub(mark.getPosition());
        if (markToBall.length() > 0.01) {
            markToBall.normalize();
            markToBall.scale(20);
        }

        // mark the target towards the ball in the direction the target is running
        Vector2d markingPosition = mark.getVelocity();
        markingPosition.scale(2); // works well in practice
        markingPosition.add(mark.getPosition());
        markingPosition.add(markToBall);

        team.setPlayerGoalPosition(defender.getPlayerID(),markingPosition);
    }

    private Player findNearest(List<Player> players, Player player) {
        double distance = Double.POSITIVE_INFINITY;
        Player nearest = null;
        Vector2d playerPosition = player.getPosition();

        for (Player p: players) {
            Vector2d direction = p.getPosition();
            direction.sub(playerPosition);
            double newDistance = direction.length();
            if (newDistance < distance) {
                distance = newDistance;
                nearest = p;
            }
        }

        return nearest;
    }


    private void setPlayerGoalPositionsNotInPossession() {
        // intercept the ball

        for (Player p : players) {
            int playerID = p.getPlayerID();
            if (playerID != team.getGoalKeeperID()) {
                interceptBall(playerID);
            }
        }
    }
}
