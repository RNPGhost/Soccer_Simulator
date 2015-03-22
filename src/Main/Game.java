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
    private Team team1; // may not be necessary to store teams
    private Team team2; // may not be necessary to store teams
    private Ball ball;

    public static void main(String[] args) {
        Game game = new Game();
        game.initialize();
    }

    private void initialize() {
        // create 2 teams to play with
        team1 = new Team(this,0,true,createPlayers());
        team2 = new Team(this,1,false,new ArrayList<Player>());

        // create a ball
        ball = new Ball(new Vector2d(200,0),new Vector2d(-200,0));

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
        assert(pitch != null);
        return pitch;
    }

    public Ball getBall() {
        assert(ball != null);
        return ball;
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
