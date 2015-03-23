package Main;

import javax.vecmath.Vector2d;
import java.util.*;

public class Ball {
    private int possessorTeamID;
    private int possessorPlayerID;
    private int team1ID;
    private int team2ID;
    private boolean inPossession;
    private Vector2d position;
    private Vector2d velocity;
    private double maxVelocity = 300;
    private Map<Integer, List<Integer>> illegalPossessors = new HashMap<Integer, List<Integer>>();

    public Ball(int teamID, int playerID, int  team1ID, int team2ID) {
        inPossession = true;
        possessorTeamID = teamID;
        possessorPlayerID = playerID;
        initialise(team1ID, team2ID);
    }

    public Ball(Vector2d position, Vector2d velocity, int team1ID, int team2ID) {
        inPossession = false;
        this.position = position;
        this.velocity = velocity;
        initialise(team1ID,team2ID);
    }

    private void initialise(int team1ID, int team2ID) {
        this.team1ID = team1ID;
        this.team2ID = team2ID;
        initialiseIllegalPossessors();
    }

    private void initialiseIllegalPossessors() {
        illegalPossessors.put(team1ID,new LinkedList<Integer>());
        illegalPossessors.put(team2ID,new LinkedList<Integer>());
    }

    public boolean isInPossession() { return inPossession; }

    public synchronized int getPossessorTeamID() {
        assert(inPossession);
        return possessorTeamID;
    }

    public synchronized int getPossessorPlayerID() {
        assert(inPossession);
        return possessorPlayerID;
    }

    public synchronized Vector2d getPosition() {
        return position;
    }

    public synchronized void update(int deltaTime) {
        if (!inPossession) {
            // calculate acceleration = -v/3
            Vector2d acceleration = new Vector2d(velocity);
            acceleration.scale(-1 / 3f);

            // apply acceleration to velocity
            acceleration.scale(deltaTime / 1000f);
            velocity.add(acceleration);
            if (velocity.length() <= 0.1) {
                velocity = new Vector2d(0,0);
            }

            // apply velocity to position
            if (velocity.length() > 0.1) {
                Vector2d distanceTravelled = new Vector2d(velocity);
                distanceTravelled.scale(deltaTime / 1000f);
                position.add(distanceTravelled);
            }
        }
    }

    public synchronized void updatePossession(List<Player> players1, List<Player> players2) {
        Vector2d possessorPosition;
        if (isPossessingTeam(players1)) {
            possessorPosition = getPossessorPosition(players1);
        } else {
            possessorPosition = getPossessorPosition(players2);
        }
        for (Player p : players1) {
            takePossession(p,possessorPosition);
        }
        for (Player p : players2) {
            takePossession(p,possessorPosition);
        }
    }

    private boolean isPossessingTeam(List<Player> players) {
        for (Player p : players) {
            return (p.teamID == possessorTeamID);
        }
        return false;
    }

    private void takePossession(Player p, Vector2d possessorPosition) {
        // make sure player has not taken possession of the ball recently
        if (illegalPossessors.get(p.teamID).contains(p.playerID)) { return; }

        // make sure that the player isn't trying to tackle a team mate
        if (inPossession && possessorTeamID == p.teamID) { return; }

        // set required distance to take possession
        Double requiredDistance = 8.0; // the centre of the player must be within 0.5m of the centre of the ball

        // calculate distance to ball
        Vector2d distance;
        if (inPossession) {
            distance = possessorPosition;
        } else {
            distance = new Vector2d(position);
        }
        distance.sub(p.getPosition());

        // if the player is close enough, take possession
        if (distance.length() <= requiredDistance) {
            if (inPossession) {
                // tackle has been made, so prevent current possessor from retrieving the ball immediately
                startPossessionGapTimer();
            }
            possessorTeamID = p.teamID;
            possessorPlayerID = p.playerID;
            inPossession = true;
        }
    }

    private void startPossessionGapTimer() {
        // set required time since last possession
        int requiredPossessionGap = 500;

        // create timer and schedule end of gap
        illegalPossessors.get(possessorTeamID).add(possessorPlayerID);
        Timer recentlyTackled = new Timer();
        recentlyTackled.schedule(new RecentlyTackledTask(possessorTeamID,possessorPlayerID),
                requiredPossessionGap);
    }

    class RecentlyTackledTask extends TimerTask {
        private int teamID;
        private int playerID;
        public RecentlyTackledTask(int teamID, int playerID) {
            this.teamID = teamID;
            this.playerID = playerID;
        }
        public void run() {
            illegalPossessors.get(teamID).remove(playerID);
        }
    }

    private Vector2d getPossessorPosition(List<Player> players) {
        for (Player p : players) {
            if (p.playerID == possessorPlayerID) { return p.getPosition(); }
        }
        return null;
    }


    public synchronized boolean kick(int teamID, List<Player> players, Vector2d direction) {
        if (!(inPossession && possessorTeamID == teamID)) { return false; }
        // set position of ball
        position = getPossessorPosition(players);

        // set velocity of ball
        velocity = new Vector2d(direction);
        if (velocity.length() > maxVelocity) {
            velocity.normalize();
            velocity.scale(maxVelocity);
        }

        // prevent current possessor from retrieving the ball immediately
        startPossessionGapTimer();

        // update that ball is no longer in possession
        inPossession = false;

        return true;
    }

    public synchronized Ball copy() {
        if (inPossession) {
            return new Ball(possessorTeamID,possessorPlayerID,team1ID,team2ID);
        } else {
            return new Ball(new Vector2d(position),new Vector2d(velocity),team1ID,team2ID);
        }
    }
}
