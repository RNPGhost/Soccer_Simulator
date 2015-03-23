package Main;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.List;

public class Team {
    private Game game;
    private int teamID;
    private boolean left;
    private int selectedPlayerID;
    private boolean playerSelected = false;
    public int getTeamID() { return teamID; }

    List<Player> players = new ArrayList<Player>();

    public Team(Game game, int teamID, boolean left, List<Player> players) {
        this.game = game;
        this.teamID = teamID;
        this.left = left;
        this.players = players;
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            p.playerID = i; // maintain invariant players[i].playerID = i
            p.teamID = teamID;
        }
    }

    public void updatePlayers(int deltaTime) {
        for (Player p : players) {
            p.update(deltaTime);
        }
    }

    public List<Player> getCopyOfPlayers() {
        List<Player> playersCopy = new ArrayList<Player>();
        for (Player p : players) {
            try {
                playersCopy.add(p.clone());
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }
        return playersCopy;
    }

    public Pitch getPitch() {
        return game.getPitch();
    }

    private boolean isValidPlayerID(int playerID) {
        return (0 <= playerID && playerID < players.size());
    }

    private Player getPlayer(int playerID) {
        assert(isValidPlayerID(playerID));
        return players.get(playerID);
    }

    public boolean setPlayerGoalPosition(int playerID, Vector2d position) {
        if (!isValidPlayerID(playerID)) { return false; }
        if (getPlayer(playerID) instanceof Goalkeeper) {
            if (left) {
                if (!Pitch.insideLeftPenaltyBox(position)) { return false; }
            } else {
                if (!Pitch.insideRightPenaltyBox(position)) { return false; }
            }
        } else {
            if (!Pitch.insidePitch(position)) { return false; }
        }
        getPlayer(playerID).setGoalPosition(position);
        return true;
    }

    public boolean kickBall(Vector2d direction) {
        Ball ball = game.getBall();
        return ball.kick(teamID,direction);
    }

    public void selectPlayer(int playerID) {
        assert(isValidPlayerID(playerID));
        if (playerSelected) {
            getPlayer(selectedPlayerID).selected = false;
        }
        selectedPlayerID = playerID;
        getPlayer(selectedPlayerID).selected = true;
        playerSelected = true;
    }

    public int getSelectedPlayerID() {
        assert(playerSelected);
        return selectedPlayerID;
    }

    public boolean isPlayerSelected() {
        return playerSelected;
    }
}
