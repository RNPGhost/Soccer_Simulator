package AI;

import Graphics.Tools;
import Main.Player;
import Main.Team;

import javax.media.opengl.awt.GLCanvas;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.List;

public class MouseInputAI implements AI {
    private Team team;

    public MouseInputAI() {
        MouseInput mouseInput = new MouseInput();
        mouseInput.initialise(this);
    }

    public void selectPlayer(Point p) {
        // convert the click position to a vector in the correct coordinates
        Vector2d clickPosition = convertPointToVector(p);

        // get a list of the players from the team
        List<Player> players = team.getCopyOfPlayers();

        // find the closest player to the click
        double distance = Double.POSITIVE_INFINITY;
        int playerID = -1;
        for (Player player : players) {
            Vector2d direction = player.getPosition();
            direction.sub(clickPosition);
            double newDistance = direction.length();
            if (newDistance < distance) {
                playerID = player.playerID;
                distance = newDistance;
            }
        }
        if (playerID >= 0) { team.selectPlayer(playerID); }
    }

    private Vector2d convertPointToVector(Point p) {
        GLCanvas canvas = Tools.canvas;
        double vectorX = (p.getX() - canvas.getWidth()/2) * Tools.maxX / canvas.getWidth();
        double vectorY = (p.getY() - canvas.getHeight()/2) * (-Tools.maxY) / canvas.getHeight();
        return new Vector2d(vectorX,vectorY);
    }

    public void setGoalPosition(Point p) {
        if (team.isPlayerSelected()) {
            int selectedPlayerID = team.getSelectedPlayerID();
            Vector2d goalPosition = convertPointToVector(p);
            team.setPlayerGoalPosition(selectedPlayerID,goalPosition);
        }
    }

    public void kickBall(Point p) {
        if (team.isPlayerSelected()) {
            Vector2d direction = convertPointToVector(p);
            direction.sub(getSelectedPlayerPosition());
            team.kickBall(direction);
        }
    }

    private Vector2d getSelectedPlayerPosition() {
        for (Player p : team.getCopyOfPlayers()) {
            if (p.playerID == team.getSelectedPlayerID()) {
                return p.getPosition();
            }
        }
        return null;
    }

    public void updateTeam(Team t) {
        team = t;
    }
}
