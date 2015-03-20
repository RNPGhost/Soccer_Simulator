package Graphics;

import Main.Game;
import Main.Pitch;
import Main.Player;

import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.vecmath.Vector2d;
import java.awt.*;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class PitchDrawingFrame implements GLEventListener {
    private Game game;

    public PitchDrawingFrame(Game game) {
        this.game = game;
    }

    public void init(GLAutoDrawable glAutoDrawable) {
        Tools.gl = glAutoDrawable.getGL().getGL2();
        createDrawingTimer();
    }

    public void display(GLAutoDrawable glAutoDrawable) {
        // update the gl
        Tools.gl = glAutoDrawable.getGL().getGL2();

        // clear the buffer
        Tools.clearBuffer();

        // draw the pitch
        drawPitch();

        // draw the players
        drawPlayers();
    }

    private void createDrawingTimer(){
        GraphicsDevice graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        int targetFPS = graphicsDevice.getDisplayMode().getRefreshRate();
        if (targetFPS == DisplayMode.REFRESH_RATE_UNKNOWN) {
            targetFPS = 120;
        }
        Timer graphicsTimer = new Timer();
        TimerTask refreshGraphics = new refreshGraphicsTask();
        graphicsTimer.schedule(refreshGraphics,0,1000/targetFPS);
    }

    class refreshGraphicsTask extends TimerTask {
        public void run() {
            Tools.displayCanvas();
        }
    }

    private void drawPitch() {
        // get the pitch
        Pitch pitch = game.getPitch();

        // get the centre canvas coordinates
        double centreX = Tools.centreX;
        double centreY = Tools.centreY;

        // find pitch corner coordinates
        double rightX = centreX + Pitch.width/2;
        double leftX = centreX - Pitch.width/2;
        double topY = centreY + Pitch.height/2;
        double bottomY = centreY - Pitch.height/2;

        // find left penalty box corner x coordinates
        double rightLPBoxX = leftX + Pitch.pBoxLength;
        double leftLPBoxX = leftX;

        // find right penalty box corner x coordinates
        double rightRPBoxX = rightX;
        double leftRPBoxX = rightX - Pitch.pBoxLength;

        // find penalty box corner y coordinates
        double topPBoxY = centreY + Pitch.pBoxWidth/2;
        double bottomPBoxY = centreY - Pitch.pBoxWidth/2;

        // find left inner box corner x coordinates
        double rightLInnerBoxX = leftX + Pitch.innerBoxLength;
        double leftLInnerBoxX = leftX;

        // find right inner box corner x coordinates
        double rightRInnerBoxX = rightX;
        double leftRInnerBoxX = rightX - Pitch.innerBoxLength;

        // find inner box corner y coordinates
        double topInnerBoxY = centreY + Pitch.innerBoxWidth/2;
        double bottomInnerBoxY = centreY - Pitch.innerBoxWidth/2;

        // find left goal corner x coordinates
        double rightLGoalX = leftX;
        double leftLGoalX = leftX - Pitch.goalDepth;

        // find right goal corner x coordinates
        double rightRGoalX = rightX + Pitch.goalDepth;
        double leftRGoalX = rightX;

        // find goal corner y coordinates
        double topGoalY = centreY + Pitch.goalWidth/2;
        double bottomGoalY = centreY - Pitch.goalWidth/2;

        // draw green background
        Tools.drawFilledRectangle(centreX - Tools.maxX/2, centreY - Tools.maxY/2,
                Tools.maxX,Tools.maxY, new Color(41, 93, 41));

        // draw the outer edge lines
        Tools.drawLine(leftX,bottomY,rightX,bottomY,Color.white);
        Tools.drawLine(rightX,bottomY,rightX,topY,Color.white);
        Tools.drawLine(rightX,topY,leftX,topY,Color.white);
        Tools.drawLine(leftX,topY,leftX,bottomY,Color.white);

        // draw centre line
        Tools.drawLine(centreX,bottomY,centreX,topY,Color.white);

        // draw centre circle
        Tools.drawCircle(centreX,centreY,Pitch.centreCircleRad,Color.white);

        // draw left penalty box
        Tools.drawLine(leftLPBoxX,bottomPBoxY,rightLPBoxX,bottomPBoxY,Color.white);
        Tools.drawLine(rightLPBoxX,bottomPBoxY,rightLPBoxX,topPBoxY,Color.white);
        Tools.drawLine(rightLPBoxX,topPBoxY,leftLPBoxX,topPBoxY,Color.white);

        // draw right penalty box
        Tools.drawLine(rightRPBoxX,topPBoxY,leftRPBoxX,topPBoxY,Color.white);
        Tools.drawLine(leftRPBoxX,topPBoxY,leftRPBoxX,bottomPBoxY,Color.white);
        Tools.drawLine(leftRPBoxX,bottomPBoxY,rightRPBoxX,bottomPBoxY,Color.white);

        // draw left inner box
        Tools.drawLine(leftLInnerBoxX,bottomInnerBoxY,rightLInnerBoxX,bottomInnerBoxY,Color.white);
        Tools.drawLine(rightLInnerBoxX,bottomInnerBoxY,rightLInnerBoxX,topInnerBoxY,Color.white);
        Tools.drawLine(rightLInnerBoxX,topInnerBoxY,leftLInnerBoxX,topInnerBoxY,Color.white);

        // draw right inner box
        Tools.drawLine(rightRInnerBoxX,topInnerBoxY,leftRInnerBoxX,topInnerBoxY,Color.white);
        Tools.drawLine(leftRInnerBoxX,topInnerBoxY,leftRInnerBoxX,bottomInnerBoxY,Color.white);
        Tools.drawLine(leftRInnerBoxX,bottomInnerBoxY,rightRInnerBoxX,bottomInnerBoxY,Color.white);

        // draw left penalty spot
        Tools.drawFilledCircle(leftX + Pitch.pSpotDist,centreY,2,Color.white);

        // draw right penalty spot
        Tools.drawFilledCircle(rightX - Pitch.pSpotDist,centreY,2,Color.white);

        // draw left goal
        Tools.drawLine(rightLGoalX,topGoalY,leftLGoalX,topGoalY,Color.black);
        Tools.drawLine(leftLGoalX,topGoalY,leftLGoalX,bottomGoalY,Color.black);
        Tools.drawLine(leftLGoalX,bottomGoalY,rightLGoalX,bottomGoalY,Color.black);

        // draw left goal line
        Tools.drawLine(rightLGoalX,bottomGoalY,rightLGoalX,topGoalY,Color.blue);

        // draw right goal
        Tools.drawLine(leftRGoalX,bottomGoalY,rightRGoalX,bottomGoalY,Color.black);
        Tools.drawLine(rightRGoalX,bottomGoalY,rightRGoalX,topGoalY,Color.black);
        Tools.drawLine(rightRGoalX,topGoalY,leftRGoalX,topGoalY,Color.black);

        // draw right goal line
        Tools.drawLine(leftRGoalX,topGoalY,leftRGoalX,bottomGoalY,Color.red);
    }

    private void drawPlayers(){
        // get the pitch
        Pitch pitch = game.getPitch();

        // get players from the pitch
        List<Player> players1 = pitch.getCopyOfPlayers(pitch.getTeam1ID());
        List<Player> players2 = pitch.getCopyOfPlayers(pitch.getTeam2ID());

        // set player radius
        double playerRadius = 5;

        // set team colours
        Color team1Colour = Color.blue;
        Color team2Colour = Color.red;

        // draw players on team 1
        for (Player p : players1) {
            Vector2d position = p.getPosition();
            Tools.drawFilledCircle(position.x,position.y,playerRadius,team1Colour);
        }

        // draw players on team 2
        for (Player p : players2) {
            Vector2d position = p.getPosition();
            Tools.drawFilledCircle(position.x,position.y,playerRadius,team2Colour);
        }
    }

    public void dispose(GLAutoDrawable glAutoDrawable) {}

    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {}
}
