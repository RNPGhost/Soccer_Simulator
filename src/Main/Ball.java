package Main;

import javax.vecmath.Vector2d;
import java.util.List;

public class Ball {
    private TeamPlayerPair possessor;
    private boolean inPossession;
    private Vector2d position;
    public Pitch pitch;

    public Ball(TeamPlayerPair possessor) {
        this.inPossession = true;
        this.possessor = possessor;
    }

    public Ball(Vector2d position) {
        this.inPossession = false;
        this.position = position;
    }

    public boolean isInPossession() { return inPossession; };

    public Vector2d getPosition() {
        TeamPlayerPair poss = new TeamPlayerPair(possessor.teamID,possessor.playerID);
        if (inPossession) {
            List<Player> players = pitch.getCopyOfPlayers(poss.teamID);
            return players.get(poss.playerID).getPosition();
        } else {
            return new Vector2d(position.x,position.y);
        }
    }

    public synchronized boolean acquirePossession(int teamID, int playerID) {
        return false; // think about how to check whether a player has the ability to kick the ball
        // and where this might be called from
    }

    public synchronized boolean kick(int teamID, int playerID, Vector2d direction) {
        return false;
    }
}
