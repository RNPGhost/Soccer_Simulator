package AI;

import Main.Pitch;
import Main.Team;

import javax.vecmath.Vector2d;
import java.util.Timer;
import java.util.TimerTask;

public class BasicAI implements AI{
    // stores the team most recently given to the AI
    private Team newTeam;
    // store the team
    private Team team;

    @Override
    public void updateTeam(Team t) {
        newTeam = t;
    }

    public BasicAI() {
        createActionsTimer();
    }

    private void createActionsTimer() {
        int period = 50;
        Timer actionTimer = new Timer();
        TimerTask actions = new ActionsTask();
        actionTimer.schedule(actions,0,period);
    }

    class ActionsTask extends TimerTask {
        public void run() {
            // don't do anything until we've been given a team
            if (newTeam == null) { return; }
            // update the team
            if (newTeam != team) { team = newTeam; }

            updateGoalKeeper();
        }
    }

    private void updateGoalKeeper() {
        Vector2d ballPosition = team.getPitch().getBallPosition();
        double angle = new Vector2d(0,-1).angle(new Vector2d(ballPosition.getX()-Pitch.width/2,ballPosition.getY()));
        double goalPositionY = Math.cos(angle)*Pitch.goalWidth/2;
        double goalPositionX = Math.sin(angle)*Pitch.goalWidth/2;
        Vector2d goalPosition = new Vector2d(Pitch.width/2-goalPositionX,-goalPositionY);
        team.setPlayerGoalPosition(team.getGoalKeeperID(),goalPosition);
    }
}
