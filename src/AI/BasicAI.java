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

            setGoalKeeperGoalPosition();
            // updatePlayers();
        }
    }

    private void setGoalKeeperGoalPosition() {
        if (pitch.ballIsInPossession()) {
            setGoalKeeperGoalPositionInPossession();
        } else {
            setGoalKeeperGoalPositionNotInPossession();
        }
    }

    // if the ball is in possession, the goal keeper will remain goalWidth/2 distance away from the centre of the goal
    // and will remain between the ball and the centre of the goal
    private void setGoalKeeperGoalPositionInPossession() {
        Vector2d ballPosition = pitch.getBallPosition();

        double xGoalLine = Pitch.width/2;
        if (players.get(team.getGoalKeeperID()).getPosition().getX() < 0) { xGoalLine = -xGoalLine; }

        Vector2d goalCentreToBall = new Vector2d(ballPosition.getX()-xGoalLine,ballPosition.getY());
        goalCentreToBall.add(pitch.getBallVelocity());

        double angle = new Vector2d(0,-1).angle(goalCentreToBall);
        double yOffset = Math.cos(angle)*Pitch.goalWidth/2;
        double xOffset = Math.sin(angle)*Pitch.goalWidth/2;

        double xGoalPosition = xGoalLine-xOffset;
        double yGoalPosition = -yOffset;

        team.setPlayerGoalPosition(team.getGoalKeeperID(),new Vector2d(xGoalPosition,yGoalPosition));
    }

    // if the ball is not in possession, try to intercept it at the earliest possible point.
    private void setGoalKeeperGoalPositionNotInPossession() {
        interceptBall(team.getGoalKeeperID());
    }

    private void interceptBall(int playerID) {
        Vector2d intersectionPoint = findIntersectionPoint(playerID);

        // make the player run past the interception point
        // player intercepts the ball at full speed
        Vector2d runDirection = new Vector2d(intersectionPoint);
        runDirection.sub(players.get(playerID).getPosition());
        runDirection.normalize();
        runDirection.scale(20.0);
        intersectionPoint.add(runDirection);

        team.setPlayerGoalPosition(playerID,intersectionPoint);
    }


    private Vector2d findIntersectionPoint(int playerID) {

        // calculate minimum distance the ball will travel before the player is able to reach it
        double lowerBound = 0;
        // maximum possible distance the ball can travel with a = -v/3 is 3 * ball velocity
        double upperBound = 3 * pitch.getBallVelocity().length();
        double x = (upperBound + lowerBound) / 2;

        for (int i = 0; i < 10; i++) {
            Double ballTime = findBallTime(x);
            Double playerTime = findPlayerTime(x, playerID);
            if (playerTime >= ballTime) {
                lowerBound = x;
            } else {
                upperBound = x;
            }
            x = (upperBound + lowerBound) / 2;
        }

        // calculate point x distance from the ball in the direction of travel
        Vector2d interPoint = pitch.getBallVelocity();
        interPoint.normalize();
        interPoint.scale(x);
        interPoint.add(pitch.getBallPosition());

        System.out.println("Sum " + interPoint.x + " " + interPoint.y);

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

        return Math.sqrt(x*x + p*p - 2*x*p*Math.cos(alpha)) / v;
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

        if (pitch.ballIsInPossession()) {

            if (pitch.getBallPossessorTeamID() == team.getTeamID()) {
                // run away from the two nearest players
                for (Player p: players) {
                    if (opponents.size() > 1) {
                        Player defender1;
                        Player defender2;
                        defender1 = opponents.get(0);
                        defender2 = opponents.get(1);

                        for (int i = 2; i < opponents.size(); i++) {
                            Vector2d direction1 = defender1.getPosition();
                            direction1.sub(p.getPosition());
                            Vector2d direction2 = defender2.getPosition();
                            direction2.sub(p.getPosition());

                            // maintain invariant that defender1 is always closer than defender2
                            if (direction2.length() < direction1.length()) {
                                Player temp = defender1;
                                defender1 = defender2;
                                defender2 = temp;
                            }

                            Vector2d direction3 = opponents.get(i).getPosition();
                            direction3.sub(p.getPosition());

                            if (direction3.length() < direction2.length()) {
                                defender2 = opponents.get(i);
                            }
                        }

                        Vector2d direction1 = defender1.getPosition();
                        direction1.sub(p.getPosition());
                        Vector2d direction2 = defender2.getPosition();
                        direction2.sub(p.getPosition());

                        Vector2d avoid = direction1;
                        avoid.add(direction2);
                        avoid.scale(0.5);

                        Vector2d travelDirection = p.getPosition();
                        travelDirection.sub(avoid);
                        travelDirection.normalize();
                        travelDirection.scale(50);
                        travelDirection.add(p.getPosition());

                        team.setPlayerGoalPosition(p.getPlayerID(),travelDirection);
                    }
                }
            } else {
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
                        if (targetToBall.length() > 0.01) {
                            targetToBall.normalize();
                            targetToBall.scale(20);
                        }

                        // mark the target towards the ball in the direction the target is running
                        Vector2d markingPosition = target.getVelocity();
                        markingPosition.scale(2);
                        markingPosition.add(target.getPosition());
                        markingPosition.add(targetToBall);

                        // set marking position as goal position for the player
                        team.setPlayerGoalPosition(p.getPlayerID(),markingPosition);

                        // remove the target from opponents to show he's being marked
                        opponents.remove(k);
                    }
                }
            }

        } else {
            // intercept the ball

            for (Player p: players) {
                double v = p.getMaxVelocity();
                double k = 3 * pitch.getBallVelocity().length();
                double b = distanceFromPlayerToBall(p.getPlayerID());
                double A = angleBetweenBallDirectionAndPlayer(p.getPlayerID());
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
                runDirection.sub(p.getPosition());
                runDirection.normalize();

                // make the player run past the interception point
                // player intercepts the ball at full speed
                runDirection.scale(20.0);
                interPoint.add(runDirection);

                team.setPlayerGoalPosition(p.getPlayerID(), interPoint);
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
