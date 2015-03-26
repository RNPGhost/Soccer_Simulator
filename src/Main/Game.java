package Main;

import AI.MouseInputAI;
import Graphics.PitchDrawingFrame;
import Graphics.Tools;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Game {
    private Pitch pitch;
    private Team team1;
    private Team team2;
    private Ball ball;

   public static void main(String[] args) {
        Game game = new Game();
        game.initialize();
    }

    private void initialize() {
        // create 2 teams to play with
        team1 = new Team(this,0,createPlayers());
        team2 = new Team(this,1,new ArrayList<Player>());

        // create a ball
        ball = new Ball(team1.getTeamID(),0);

        // create a pitch to play on
        pitch = new Pitch(ball,team1,team2);

        // create a PitchDrawingFrame
        PitchDrawingFrame pdFrame = new PitchDrawingFrame(this);

        // initialise the canvas
        Tools.initialiseCanvas(pdFrame);

        // create update timer
        createUpdateTimer();

        // create team1 AI
        initializeTeam1AI();
    }

    private void createUpdateTimer() {
        int period = 50;
        Timer updateTimer = new Timer();
        TimerTask updateTeams = new UpdateTeamsTask(period);
        updateTimer.schedule(updateTeams,0,period);
    }

    private List<Player> createPlayers() {
        int playerID = 0;
        Vector2d position = new Vector2d(0,0);
        Vector2d velocity = new Vector2d(0,0);
        Vector2d goalPosition = new Vector2d(0,0);
        Player player = new Player(playerID,position,velocity,goalPosition);
        List<Player> players = new ArrayList<Player>();
        players.add(player);
        return players;
    }

    private void initializeTeam1AI() {
        MouseInputAI mouseInputAI = new MouseInputAI();
        mouseInputAI.updateTeam(team1);
    }

    public Pitch getPitch() {
        return pitch;
    }

    public Ball getBall() {
        return ball;
    }

    class UpdateTeamsTask extends TimerTask {
        private int period;
        public UpdateTeamsTask(int period) {
            this.period = period;
        }
        public void run() {
            pitch.update(period);
            // lots of bugs
            // doesn't print anything when kicked out of the top
            // flips between left and right when kicked out of the side
            if (!(ball.isInPossession()
                    || Pitch.insidePitch(ball.getPosition()))) {
                PitchLine line = Pitch.findIntersectionLine(ball.getPosition(), ball.getVelocity());
                if (line == PitchLine.TOP_SIDELINE
                        || line == PitchLine.BOTTOM_SIDELINE) {
                    System.out.println("Top or Bottom");
                    // check who last possessed the ball
                    // give it to the closest member of the opposing team
                    // force that player to stay on the line
                    // update the AI controlling that team
                } else if (line == PitchLine.LEFT_SIDELINE) {
                    System.out.println("Left");
                    // check possession to see whether it's a corner or goal kick
                    // if goal kick
                        // give to goalkeeper and force him not to move
                    // if corner
                        // give to closest member of the opposing team and force him not to move
                } else if (line == PitchLine.RIGHT_SIDELINE) {
                    System.out.println("Right");
                    // check possession to see whether it's a corner or goal kick
                    // if goal kick
                        // give to goalkeeper and force him not to move
                    // if corner
                        // give to closest member of the opposing team and force him not to move
                } else if (line == PitchLine.LEFT_GOAL) {
                    System.out.println("Left Goal");
                    // goal to right team (team 2)
                    // reset pitch
                } else if (line == PitchLine.RIGHT_GOAL) {
                    System.out.println("Right Goal");
                    // goal to left team (team 1)
                    // reset pitch
                }
            }
        }
    }
}
