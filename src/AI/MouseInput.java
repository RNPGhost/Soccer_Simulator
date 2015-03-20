package AI;

import Graphics.Tools;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class MouseInput extends MouseAdapter {
    MouseInputAI AI;

    public void initialise(MouseInputAI AI) {
        this.AI = AI;
        Tools.canvas.addMouseListener(this);
        Tools.canvas.addMouseMotionListener(this);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (e.getButton() == KeyBinding.setGoalPosition) {
            AI.setGoalPosition(e.getPoint());
        } else if (e.getButton() == KeyBinding.selectPlayer) {
            AI.selectPlayer(e.getPoint());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (e.getButton() == KeyBinding.setGoalPosition) {
            AI.setGoalPosition(e.getPoint());
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (e.getButton() == KeyBinding.setGoalPosition) {
            AI.setGoalPosition(e.getPoint());
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
}
