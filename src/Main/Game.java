package Main;

import AI.AI;
import AI.BasicAI;
import AI.MouseInputAI;
import Graphics.PitchDrawingFrame;
import Graphics.Tools;

import javax.vecmath.Vector2d;

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
    }
}
