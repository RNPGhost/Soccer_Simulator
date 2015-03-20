package Main;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.List;

public class Team {
    private Game game;
    private int teamID;
    private int selectedPlayerID;
    private boolean playerSelected = false;
    public int getTeamID() { return teamID; }

    List<Player> players = new ArrayList<Player>();

    public Team(Game game, int teamID, List<Player> players) {
        this.game = game;
        this.teamID = teamID;
        this.players = players;
        for (int i = 0; i < 0; i++) {
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
            playersCopy.add(p.clone());
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
        assert(isValidPlayerID(playerID));
        return getPlayer(playerID).setGoalPosition(position);
    };

    public boolean kickBall(int playerID, Vector2d direction) {
        assert(isValidPlayerID(playerID));
        Ball ball = game.getBall();
        return ball.kick(teamID,playerID,direction);
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
