package AI;

import Graphics.Tools;
import Main.Player;
import Main.Team;

import javax.media.opengl.awt.GLCanvas;
import javax.vecmath.Vector2d;
import java.awt.*;

public class MouseInputAI implements AI {
    private Team team;

    public MouseInputAI() {
        MouseInput mouseInput = new MouseInput();
        mouseInput.initialise(this);
    }

    public void selectPlayer(Point p) {
        team.selectPlayer(0);
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
