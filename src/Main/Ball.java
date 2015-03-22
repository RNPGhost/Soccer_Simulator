package Main;

import javax.vecmath.Vector2d;
import java.util.List;

public class Ball {
    private int possessorTeamID;
    private int possessorPlayerID;
    private boolean inPossession;
    private Vector2d position;
    public Ball(int teamID, int playerID) {
        inPossession = true;
        possessorTeamID = teamID;
        possessorPlayerID = playerID;
    }

    public Ball(Vector2d position) {
        inPossession = false;
        this.position = position;
    }

    public boolean isInPossession() { return inPossession; }

    public synchronized int getPossessorTeamID() {
        assert(isInPossession());
        return possessorTeamID;
    }

    public synchronized int getPossessorPlayerID() {
        assert(isInPossession());
        return possessorPlayerID;
    }

    public synchronized Vector2d getPosition() {
        return position;
    }

    public synchronized void updatePossession(int teamID, List<Player> players) {
        // called from team, passing a copy of its players
        // check whether any of them are able to acquire possession
        // the change the possession
    }

    public synchronized boolean kick(int teamID, int playerID, Vector2d direction) {
        return false;
    }

    public synchronized Ball copy() {
        if (isInPossession()) {
            return new Ball(possessorTeamID,possessorPlayerID);
        } else {
            return new Ball(new Vector2d(position));
        }
    }
}
