package AI;

import Main.Goalkeeper;
import Main.Pitch;
import Main.Player;
import Main.Team;

import javax.vecmath.Vector2d;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class BasicAI implements AI{
    // store the pitch
    private Pitch pitch;
    // stores the team most recently given to the AI
    private Team newTeam;
    // store the team
    private Team team;
    // store the players
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
        actionTimer.schedule(actions,0,period);
    }

    class ActionsTask extends TimerTask {
        public void run() {
            // don't do anything until we've been given a team
            if (newTeam == null) { return; }
            // update the team and pitch
            if (newTeam != team) {
                team = newTeam;
                pitch = team.getPitch();
            }
            // update the players
            players = team.getCopyOfPlayers();

            updateGoalKeeper();
            updatePlayers();
        }
    }

    private void updateGoalKeeper() {
        Vector2d ballPosition = pitch.getBallPosition();
        Vector2d goalPosition = new Vector2d();

        if (pitch.ballIsInPossession()) {
            double angle = new Vector2d(0,-1).angle(new Vector2d(ballPosition.getX()-Pitch.width/2,ballPosition.getY()));
            double goalPositionY = Math.cos(angle)*Pitch.goalWidth/2;
            double goalPositionX = Math.sin(angle)*Pitch.goalWidth/2;
            goalPosition = new Vector2d(Pitch.width/2-goalPositionX,-goalPositionY);
        } else {
            Player goalKeeper = players.get(team.getGoalKeeperID());
            double v = goalKeeper.getMaxVelocity(); // max goalKeeper velocity
            double k = 3 * pitch.getBallVelocity().length();
            double b = distanceFromPlayerToBall(team.getGoalKeeperID());
            double A = angleBetweenBallDirectionAndPlayer(team.getGoalKeeperID());
            double upperBound = k;
            double lowerBound = 0;
            double x = k/2; // distance the ball will travel until interception
            for (int i = 0; i < 10; i++) {
                // time for player to reach intersection point minus time for ball to reach intersection point
                double d =  Math.pow(b,2) + Math.pow(x,2) - 2*b*x*Math.cos(A)
                        - Math.pow(3*v*(Math.log(k) - Math.log(k-x)),2);
                if (d >= 0) {
                    lowerBound = x;
                } else {
                    upperBound = x;
                }
                x = (upperBound + lowerBound) / 2;
            }

            // find the intersection point
            Vector2d interPoint = pitch.getBallVelocity();
            interPoint.normalize();
            interPoint.scale(x);
            interPoint.add(pitch.getBallPosition());

            // find the running direction
            Vector2d runDirection = new Vector2d(interPoint);
            runDirection.sub(players.get(team.getGoalKeeperID()).getPosition());
            runDirection.normalize();

            // make the player run past the interception point
            // player intercepts the ball at full speed
            runDirection.scale(20.0);
            interPoint.add(runDirection);

            goalPosition = interPoint;

            // find best interception point and get there asap

            // for ball, t = 3(ln(k) - ln(k-x))
                // k >= x
                // where k = ballSpeed / (20 * (1 - e^(-1/60)))
                // this means k is the max range of the ball

            // d = ( b^2 + x^2 - 2bx*cos(A) )^(1/2)
            // where b = distance from player to ball

            // as the time to get to top speed is quite small (max 1.3s), we can assume player is running at full speed
                // t = ( b^2 + x^2 - 2bx*cos(A) )^(1/2) / v

            // setting the two to be equal
                // 3(ln(k) - ln(k-x)) = ( b^2 + x^2 - 2bx*cos(A) )^(1/2) / v
                // (3v(ln(k) - ln(k-x)))^2 = b^2 + x^2 - 2bx*cos(A)

        }

        team.setPlayerGoalPosition(team.getGoalKeeperID(),goalPosition);
    }

    private void updatePlayers() {
        // get a copy of the opponent players
        int opponentID;
        if (pitch.getTeam1ID() != team.getTeamID()) {
            opponentID = pitch.getTeam1ID();
        } else {
            opponentID = pitch.getTeam2ID();
        }
        List<Player> opponents = pitch.getCopyOfPlayers(opponentID);

        // for every player, find the closest opponent that isn't a goal keeper
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p.getPlayerID() != team.getGoalKeeperID()) {
                double dist = Double.POSITIVE_INFINITY;
                int k = -1;
                // for each opponent, check if they're closer than any previous opponents
                for (int j = 0; j < opponents.size(); j++) {
                    Player o = opponents.get(j);
                    if (!(o instanceof Goalkeeper)) {
                        Vector2d direction = new Vector2d(o.getPosition());
                        direction.sub(p.getPosition());
                        if (direction.length() < dist) {
                            dist = direction.length();
                            k = j;
                        }
                    }
                }

                Player target = opponents.get(k);

                // find the vector from the target to the ball
                Vector2d targetToBall = pitch.getBallPosition();
                targetToBall.sub(target.getPosition());
                targetToBall.normalize();

                // mark the target towards the ball in the direction the target is running
                targetToBall.scale(20);
                Vector2d markingPosition = pitch.getBallPosition();
                markingPosition.add(targetToBall);
                markingPosition.add(target.getVelocity());

                // set marking position as goal position for the player
                team.setPlayerGoalPosition(p.getPlayerID(),markingPosition);

                // remove the target from opponents to show he's being marked
                opponents.remove(k);
            }
        }
    }

    private double distanceFromPlayerToBall(int playerID) {
        Vector2d direction = new Vector2d(pitch.getBallPosition());
        direction.sub(players.get(playerID).getPosition());
        return direction.length();
    }

    private double angleBetweenBallDirectionAndPlayer(int playerID) {
        Vector2d playerDirection = new Vector2d(players.get(playerID).getPosition());
        playerDirection.sub(pitch.getBallPosition());
        return playerDirection.angle(pitch.getBallVelocity());
    }
}
