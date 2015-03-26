package Main;

import javax.vecmath.Vector2d;
import java.util.*;

public class Ball {
    private int possessorTeamID;
    private int possessorPlayerID;
    private boolean inPossession;
    private Vector2d position;
    private Vector2d velocity;
    public Pitch pitch;
    private double maxVelocity = 300;
    private Map<Integer, List<Integer>> illegalPossessors = new HashMap<Integer, List<Integer>>();
    private boolean firstPossessionCheck = true;

    public Ball(int teamID, int playerID) {
        inPossession = true;
        possessorTeamID = teamID;
        possessorPlayerID = playerID;
    }

    public Ball(Vector2d position, Vector2d velocity) {
        inPossession = false;
        this.position = position;
        this.velocity = velocity;
    }

    private void initialiseIllegalPossessors() {
        illegalPossessors.put(pitch.getTeam1ID(),new LinkedList<Integer>());
        illegalPossessors.put(pitch.getTeam2ID(),new LinkedList<Integer>());
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
        return new Vector2d(position);
    }

    public synchronized Vector2d getVelocity() { return new Vector2d(velocity); }

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

    public synchronized void updatePossession() {
        if (firstPossessionCheck) {
            initialiseIllegalPossessors();
            firstPossessionCheck = false;
        }
        for (Player p : pitch.getCopyOfPlayers(pitch.getTeam1ID())) {
            takePossession(p);
        }
        for (Player p : pitch.getCopyOfPlayers(pitch.getTeam2ID())) {
            takePossession(p);
        }
    }

    private void takePossession(Player p) {
        // make sure player has not taken possession of the ball recently
        if (illegalPossessors.get(p.teamID).contains(p.playerID)) { return; }

        // make sure that the player isn't trying to tackle a team mate
        if (inPossession && possessorTeamID == p.teamID) { return; }

        // set required distance to take possession
        Double requiredDistance = 8.0; // the centre of the player must be within 0.5m of the centre of the ball

        // calculate distance to ball
        Vector2d distance;
        if (inPossession) {
            distance = getPossessorPosition();
        } else {
            distance = new Vector2d(position);
        }
        distance.sub(p.getPosition());

        // if the player is close enough, take possession
        if (distance.length() <= requiredDistance) {
            if (inPossession) {
                // tackle has been made, so prevent current possessor from retrieving the ball immediately
                startPossessionGapTimer(500);
            }
            possessorTeamID = p.teamID;
            possessorPlayerID = p.playerID;
            inPossession = true;
        }
    }

    private void startPossessionGapTimer(int requiredPossessionGap) {
        // create timer and schedule end of gap
        if(illegalPossessors.get(possessorTeamID).add(new Integer(possessorPlayerID))) {
            Timer recentlyTackled = new Timer();
            recentlyTackled.schedule(new RecentlyTackledTask(possessorTeamID,possessorPlayerID),
                    requiredPossessionGap);
        }
    }

    class RecentlyTackledTask extends TimerTask {
        private int teamID;
        private int playerID;
        public RecentlyTackledTask(int teamID, int playerID) {
            this.teamID = teamID;
            this.playerID = playerID;
        }
        public void run() {
            illegalPossessors.get(teamID).remove(new Integer(playerID));
        }
    }

    private Vector2d getPossessorPosition() {
        return pitch.getPlayerPosition(possessorTeamID,possessorPlayerID);
    }

    public synchronized boolean kick(int teamID, Vector2d direction) {
        if (!(inPossession && possessorTeamID == teamID)) { return false; }
        // set position of ball
        position = getPossessorPosition();

        // set velocity of ball
        velocity = new Vector2d(direction);
        if (velocity.length() > maxVelocity) {
            velocity.normalize();
            velocity.scale(maxVelocity);
        }

        // prevent current possessor from retrieving the ball immediately
        startPossessionGapTimer(300);

        // update that ball is no longer in possession
        inPossession = false;

        return true;
    }

    public synchronized Ball copy() {
        if (inPossession) {
            return new Ball(possessorTeamID,possessorPlayerID);
        } else {
            return new Ball(new Vector2d(position),new Vector2d(velocity));
        }
    }
}
