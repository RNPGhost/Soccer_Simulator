package AI;

import Graphics.Tools;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseInput extends MouseAdapter {
    MouseInputAI AI;
    int lastButtonPressed;

    public void initialise(MouseInputAI AI) {
        this.AI = AI;
        Tools.canvas.addMouseListener(this);
        Tools.canvas.addMouseMotionListener(this);
    }

    @Override
    public void mousePressed(MouseEvent e) {
        lastButtonPressed = e.getButton();
        if (lastButtonPressed == KeyBinding.setGoalPosition) {
            AI.setGoalPosition(e.getPoint());
        } else if (lastButtonPressed == KeyBinding.selectPlayer) {
            AI.selectPlayer(e.getPoint());
        } else if (lastButtonPressed == KeyBinding.kickBall) {
            AI.selectPlayer(e.getPoint());
            AI.setGoalPosition(e.getPoint());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (lastButtonPressed == KeyBinding.setGoalPosition) {
            AI.setGoalPosition(e.getPoint());
        } else if (lastButtonPressed == KeyBinding.kickBall) {
            AI.setGoalPosition(e.getPoint());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (e.getButton() == KeyBinding.kickBall) {
            AI.kickBall(e.getPoint());
            AI.interceptBall();
        }
    }
}
