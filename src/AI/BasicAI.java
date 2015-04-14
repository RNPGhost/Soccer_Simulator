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
        Pitch pitch = team.getPitch();
        Vector2d ballPosition = pitch.getBallPosition();
        Vector2d goalPosition = new Vector2d();

        if (pitch.ballIsInPossession()) {
            double angle = new Vector2d(0,-1).angle(new Vector2d(ballPosition.getX()-Pitch.width/2,ballPosition.getY()));
            double goalPositionY = Math.cos(angle)*Pitch.goalWidth/2;
            double goalPositionX = Math.sin(angle)*Pitch.goalWidth/2;
            goalPosition = new Vector2d(Pitch.width/2-goalPositionX,-goalPositionY);
        } else {
            // find best interception point and get there asap
        }

        team.setPlayerGoalPosition(team.getGoalKeeperID(),goalPosition);
    }
}
