package Main;

import AI.AI;
import AI.MouseInputAI;
import AI.BasicAI;
import Graphics.PitchDrawingFrame;
import Graphics.Tools;

import javax.vecmath.Vector2d;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    Pitch pitch;

   public static void main(String[] args) {
        Game game = new Game();
        game.initialize();
    }

    private void initialize() {

        // create a PitchDrawingFrame
        PitchDrawingFrame pdFrame = new PitchDrawingFrame();

        // initialise the canvas
        Tools.initialiseCanvas(pdFrame);

        // create team 1's AI
        AI mouseInputAI = new MouseInputAI();

        // create team 2's AI
        AI basicAI = new BasicAI();

        // create a ball
        Ball ball = new Ball(new Vector2d(0,0),new Vector2d(0,0));

        // create a pitch to play on
        pitch = new Pitch(mouseInputAI,basicAI,ball);

        // give the pitch and ball to the drawing frame
        pdFrame.initialise(pitch,ball);

        // create update timer
        createUpdateTimer();
    }

    private void createUpdateTimer() {
        int period = 50;
        Timer updateTimer = new Timer();
        TimerTask updateTeams = new UpdateTeamsTask(period);
        updateTimer.schedule(updateTeams,0,period);
    }

    class UpdateTeamsTask extends TimerTask {
        private int period;
        public UpdateTeamsTask(int period) {
            this.period = period;
        }
        public void run() {
            pitch.update(period);
        }
    }
}
