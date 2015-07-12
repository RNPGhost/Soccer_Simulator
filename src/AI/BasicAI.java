package AI;

import Main.*;

import javax.vecmath.Vector2d;
import java.util.*;

public class BasicAI implements AI {

    private Pitch pitch;
    // if a new team is given to the AI,
    // it is stored here until the AI is ready to update the team
    private Team newTeam;
    private Team team;
    private List<Player> players;
    private boolean leftSideOfPitch;

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
                leftSideOfPitch = setPitchSide();
            }
            // update the players
            players = team.getCopyOfPlayers();

            setGoalKeeperGoalPosition();
            setPlayerGoalPositions();
        }
    }

    public boolean setPitchSide() {
        return (team.getPlayerPosition(team.getGoalKeeperID()).getX() < 0);
    }

    private void setGoalKeeperGoalPosition() {
        if (pitch.ballIsInPossession()) {
            if ((leftSideOfPitch &&
                    Pitch.insideLeftPenaltyBox(pitch.getBallPosition()))
                    || (!leftSideOfPitch &&
                    Pitch.insideRightPenaltyBox(pitch.getBallPosition()))) {
                setGoalKeeperGoalPositionBallInBox();
            } else {
                setGoalKeeperGoalPositionInPossession();
            }
        } else {
            setGoalKeeperGoalPositionNotInPossession();
        }
    }

    private void setGoalKeeperGoalPositionBallInBox() {
        // if the ball is in the box, try to intercept it at the earliest possible point

        interceptBall(team.getGoalKeeperID());
    }

    private void setGoalKeeperGoalPositionInPossession() {
        // if the ball is in possession,
        // the goal keeper will remain goalWidth/2 distance away from the centre of the goal
        // and will remain between the ball and the centre of the goal

        Vector2d ballPosition = pitch.getBallPosition();

        double xGoalLine = (leftSideOfPitch ? -Pitch.width/2 : Pitch.width / 2);

        Vector2d goalCentreToBall = new Vector2d(ballPosition.getX() - xGoalLine, ballPosition.getY());
        goalCentreToBall.add(pitch.getBallVelocity());

        double angle = new Vector2d(0, -1).angle(goalCentreToBall);
        double yOffset = Math.cos(angle) * Pitch.goalWidth / 2;
        double xOffset = Math.sin(angle) * Pitch.goalWidth / 2;
        if (!leftSideOfPitch) { xOffset = -xOffset; }

        double xGoalPosition = xGoalLine + xOffset;
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

        if (pitch.getBallVelocity().length() < 0.1) { return pitch.getBallPosition(); }

        double lowerBound = 0;
        // maximum possible distance the ball can travel with a = -v/3 is 3 * ball velocity
        double upperBound = 3 * pitch.getBallVelocity().length() + 1;
        // we test at distance of player from ball to ensure we get lowest x possible
        Vector2d ballToPlayer = team.getPlayerPosition(playerID);
        ballToPlayer.sub(pitch.getBallPosition());
        double newX = ballToPlayer.length();
        double oldX = Double.POSITIVE_INFINITY;

        while (Math.abs(newX - oldX) > 1) {
            oldX = newX;
            Double ballTime = findBallTime(pitch.getBallVelocity().length(), oldX);
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

    private double findBallTime(double u, double x) {
        // time taken for ball to travel x distance is t = -3ln(1 - x/3u)
        // where u is the initial velocity of the ball

        return -3 * Math.log(1 - (x / (3 * u)));
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
        // for all players who aren't the ball possessor
        // spread out

        List<Player> newPlayers = new ArrayList<Player>(players);
        Player ballPossessor = newPlayers.get(pitch.getBallPossessorPlayerID());
        setPlayerGoalPositionsBallPossessor(ballPossessor);
        newPlayers.remove(ballPossessor);
        newPlayers.remove(team.getGoalKeeperID());

        spreadOut(newPlayers);
    }

    private void setPlayerGoalPositionsBallPossessor(Player player) {
        // prioritises passing forward
        // then aggressive movement
        // then other movement
        // then passing backward

        List<Player> orderedPlayers = new ArrayList<Player>(players);
        orderedPlayers = sortByClosestToEnemyGoal(orderedPlayers);

        int playerIndex = orderedPlayers.indexOf(player);
        List<Player> closerPlayers = orderedPlayers.subList(0,playerIndex);
        List<Player> furtherPlayers = orderedPlayers.subList(playerIndex + 1, orderedPlayers.size());

        if (!tryToShoot(player, true)) {
            if (!tryToPass(closerPlayers)) {
                if (!tryToRunAtGoal(player)) {
                    if (!tryToPass(furtherPlayers)) {
                        tryToShoot(player, false);
                    }
                }
            }
        }
    }

    private List<Player> sortByClosestToEnemyGoal(List<Player> teamMates) {
        final Vector2d goal = new Vector2d(leftSideOfPitch ? Pitch.width/2 : -Pitch.width/2, 0);

        teamMates.sort(new Comparator<Player>() {
            public int compare(Player a, Player b) {
                Vector2d aToGoal = new Vector2d(goal); aToGoal.sub(a.getPosition()); double distA = aToGoal.length();
                Vector2d bToGoal = new Vector2d(goal); bToGoal.sub(b.getPosition()); double distB = bToGoal.length();
                return Double.compare(distA, distB);
            }
        });

        return teamMates;
    }

    private boolean tryToShoot(Player player, boolean waitUntilClose) {
        double goalXPos = (leftSideOfPitch ? Pitch.width/2 : -Pitch.width/2);

        Vector2d distanceToGoal = new Vector2d(goalXPos, 0);
        distanceToGoal.sub(player.getPosition());

        if (waitUntilClose && distanceToGoal.length() > 200) { return false; }

        Vector2d topPost = new Vector2d(goalXPos, Pitch.goalWidth/2);
        Vector2d bottomPost = new Vector2d(goalXPos, -Pitch.goalWidth/2);

        Vector2d enemyGoalKeeperPosition = pitch.getPlayerPosition(
                getOpponentID(), pitch.getGoalKeeperID(getOpponentID()));

        Vector2d playerToGoalKeeper = new Vector2d(enemyGoalKeeperPosition);
        playerToGoalKeeper.sub(player.getPosition());

        Vector2d playerToTopPost = new Vector2d(topPost);
        playerToTopPost.add(new Vector2d(0,-1));
        playerToTopPost.sub(player.getPosition());

        Vector2d playerToBottomPost = new Vector2d(bottomPost);
        playerToBottomPost.add(new Vector2d(0,1));
        playerToBottomPost.sub(player.getPosition());

        double angle = Math.PI/36; // 5 degrees

        if (playerToGoalKeeper.angle(playerToTopPost) > playerToGoalKeeper.angle(playerToBottomPost)) {
            if (!tryShot(angle, player.getPosition(), playerToTopPost)) {
                if (!tryShot(angle, player.getPosition(), playerToBottomPost)) {
                    if (!waitUntilClose) {
                        team.kickBall(playerToTopPost);
                    } else {
                        return false;
                    }
                }
            }
        } else {
            if (!tryShot(angle, player.getPosition(), playerToBottomPost)) {
                if (!tryShot(angle, player.getPosition(), playerToTopPost)) {
                    if (!waitUntilClose) {
                        team.kickBall(playerToBottomPost);
                    } else {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private boolean tryShot(double angle, Vector2d playerPosition, Vector2d shotDirection) {
        if (checkConeForEnemies(angle, playerPosition, shotDirection)) { return false; }
        Vector2d shot = new Vector2d(shotDirection);
        shot.normalize();
        shot.scale(pitch.getBallMaxVelocity());
        team.kickBall(shot);
        return true;

    }

    private boolean tryToPass(List<Player> receivers) {
        for (Player p: receivers) {
            if (makePass(p)) {
                return true;
            }
        }
        return false;
    }

    private boolean tryToRunAtGoal(Player player) {
        // if not a stationary player, run at the enemy goal
        // eventually will check whether is it safe to move forward
        if (player instanceof StationaryPlayer
                || player instanceof Goalkeeper) { return false; }

        Vector2d goal = new Vector2d(leftSideOfPitch ? Pitch.width/2 : -Pitch.width/2, 0);

        double angle = Math.PI/8;
        Vector2d path = new Vector2d(goal);
        path.sub(player.getPosition());
        path.normalize();
        path.scale(50);

        if (!checkConeForEnemies(angle, player.getPosition(), path)) {
            team.setPlayerGoalPosition(player.getPlayerID(), goal);
            return true;
        }

        return false;
    }

    private boolean makePass(Player player) {
        // estimate pass time by calculating time for ball to reach player
        // pass where the player will be if their velocity does not change

        Vector2d passVector = player.getPosition();
        passVector.sub(pitch.getBallPosition());

        double passSpeed = 5 * passVector.length();
        if (passSpeed > pitch.getBallMaxVelocity()) { passSpeed = pitch.getBallMaxVelocity(); }

        Vector2d passLeadComponent = player.getVelocity();
        passLeadComponent.scale(findBallTime(passSpeed, passVector.length()));

        passVector.add(passLeadComponent);

        double angle = Math.PI / 32;

        if (!checkConeForEnemies(angle, pitch.getBallPosition(), passVector)) {
            passVector.normalize();
            passVector.scale(passSpeed);
            team.kickBall(passVector);
            return true;
        }

        return false;
    }

    private boolean checkConeForEnemies(double angle, Vector2d conePoint, Vector2d centreLine) {
        List<Player> opponents = pitch.getCopyOfPlayers(getOpponentID());

        for (Player o: opponents) {
            Vector2d direction = o.getPosition();
            direction.sub(conePoint);
            if (direction.length() <= centreLine.length()
                    && centreLine.angle(direction) <= angle) {
                return true;
            }
        }

        return false;
    }

    private void spreadOut(List<Player> players) {
        int xMultiplier = (leftSideOfPitch ? 1 : -1);

        List<Vector2d> formation = new ArrayList<Vector2d>();
        // attackers
        formation.add(0, new Vector2d(xMultiplier * Pitch.width/3,Pitch.height/6));
        formation.add(1, new Vector2d(xMultiplier * Pitch.width/3,-Pitch.height/6));
        // midfield
        formation.add(2, new Vector2d(xMultiplier * Pitch.width/6,Pitch.height/3));
        formation.add(3, new Vector2d(xMultiplier * Pitch.width/6,-Pitch.height/3));
        formation.add(4, new Vector2d(0,0));
        // defenders
        formation.add(5, new Vector2d(xMultiplier * -Pitch.width/6, Pitch.height/3));
        formation.add(6, new Vector2d(xMultiplier * -Pitch.width/6, -Pitch.height/3));
        formation.add(7, new Vector2d(xMultiplier * -Pitch.width/3, Pitch.height/6));
        formation.add(8, new Vector2d(xMultiplier * -Pitch.width/3, -Pitch.height/6));

        for (Vector2d v: formation) {
            Player player = findAndRemoveNearest(players, v);
            team.setPlayerGoalPosition(player.getPlayerID(), v);
        }
    }

    private Player findAndRemoveNearest(List<Player> players, Vector2d position) {
        Player nearest = findNearest(players, position);
        players.remove(nearest);

        return nearest;
    }

    private Player findNearest(List<Player> players, Vector2d position) {
        double distance = Double.POSITIVE_INFINITY;
        Player nearest = null;

        for (Player p: players) {
            Vector2d direction = p.getPosition();
            direction.sub(position);
            double newDistance = direction.length();
            if (newDistance < distance) {
                distance = newDistance;
                nearest = p;
            }
        }

        return nearest;
    }

    private Player findAndRemoveNearest(List<Player> players, Player player) {
        return findAndRemoveNearest(players, player.getPosition());
    }

    private void setPlayerGoalPositionsEnemyInPossession() {
        markOpponentsWithPlayers(players);
    }

    private void markOpponentsWithPlayers(List<Player> defenders) {
        // find the closest player to each opponent and
        // assign that player to mark that opponent

        int opponentID = getOpponentID();
        List<Player> opponents = pitch.getCopyOfPlayers(opponentID);
        List<Player> playersCopy = new ArrayList<Player>(defenders);

        playersCopy.remove(team.getGoalKeeperID());
        opponents.remove(pitch.getGoalKeeperID(getOpponentID()));

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

    private void mark(List<Player> defenders, Player mark) {
        // mark the player using one of the players in the list
        // if the mark has the ball, go for the ball

        if (defenders.size() == 0) { return; }

        Player defender = findAndRemoveNearest(defenders, mark);

        if (mark.getPlayerID() == pitch.getBallPossessorPlayerID()) {
            interceptBall(defender.getPlayerID());
        } else {
            // find the vector from the mark to the ball
            Vector2d markToBall = pitch.getBallPosition();
            markToBall.sub(mark.getPosition());
            if (markToBall.length() > 0.01) {
                markToBall.normalize();
                markToBall.scale(20);
            }

            // mark the target towards the ball in the direction the target is running
            Vector2d markingPosition = mark.getVelocity();
            markingPosition.add(mark.getPosition());
            markingPosition.add(markToBall);

            team.setPlayerGoalPosition(defender.getPlayerID(),markingPosition);
        }
    }

    private void setPlayerGoalPositionsNotInPossession() {
        // CHANGE SO THAT ONLY THE CLOSEST PLAYER TO THEIR INTERCEPTION POINT INTERCEPTS THE BALL

        List<Player> newPlayers = new ArrayList<Player>(players);
        newPlayers.remove(players.get(team.getGoalKeeperID()));

        if (newPlayers.size() < 0) { return; }

        Player closestPlayer = players.get(0);
        double distance = Double.POSITIVE_INFINITY;

        for (Player player: newPlayers) {
            Vector2d ballDistance = findIntersectionPoint(player.getPlayerID());
            ballDistance.sub(pitch.getBallPosition());
            double newDistance = ballDistance.length();
            if (newDistance < distance) {
                closestPlayer = player;
                distance = newDistance;
            }
        }

        interceptBall(closestPlayer.getPlayerID());
        newPlayers.remove(closestPlayer);

        if (pitch.getBallPossessorTeamID() == team.getTeamID()) {
            spreadOut(newPlayers);
        } else {
            markOpponentsWithPlayers(newPlayers);
        }


    }
}
