package AI;

import Graphics.Tools;

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

    public void setGoalPosition(Point p) {
        if (team.isPlayerSelected()) {
            int selectedPlayerID = team.getSelectedPlayerID();
            GLCanvas canvas = Tools.canvas;
            double vectorX = (p.getX() - canvas.getWidth()/2) * Tools.maxX / canvas.getWidth();
            double vectorY = (p.getY() - canvas.getHeight()/2) * (-Tools.maxY) / canvas.getHeight();
            Vector2d goalPosition = new Vector2d(vectorX,vectorY);
            team.setPlayerGoalPosition(selectedPlayerID,goalPosition);
        }
    }

    public void updateTeam(Team t) {
        team = t;
    }
}
