package Main;

import AI.AI;
import Graphics.Tools;

import javax.vecmath.Vector2d;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

// consider creating 'LeftHalfPlayer' and 'RightHalfPlayer'
// the changes for these would be in 'Team'

public class Pitch {
    // pitch dimensions
    final public static int width = 1200;
    final public static int height = 800;

    // goal dimensions
    final public static int goalWidth = 80;
    final public static int goalDepth = 20;

    // penalty box dimensions
        // distance from far left edge of the penalty box to the far right as seen by the keeper
    final public static int pBoxWidth = 520;
        // distance from goal to the far edge of the penalty box
    final public static int pBoxLength = 180;

    // inner box dimension
        // distance from far left edge of the inner box to the far right as seen by the keeper
    final public static int innerBoxWidth = 200;
        // distance from goal to the far edge of the inner box
    final public static int innerBoxLength = 60;

    // penalty spot dimensions
    final public static int pSpotDist = 120;

    // centre circle dimensions
    final public static int centreCircleRad = 100;

    private void checkBallOutOfBounds() {
        if (!ball.isInPossession() && !Pitch.insidePitch(ball.getPosition())) {
            findIntersectionLine();
        }
    }

    private void findIntersectionLine() {
        // get ball position and direction of travel
        Vector2d position = ball.getPosition();
        Vector2d velocity = ball.getVelocity();

        // initialise line, boundsIntersection and distance
        PitchLine line = null;
        Vector2d boundsIntersection = null;
        double distance = Double.POSITIVE_INFINITY;

        // find pitch corner coordinates
        Vector2d topLeftCorner = new Vector2d(-Pitch.width/2,Pitch.height/2);
        Vector2d topRightCorner = new Vector2d(Pitch.width/2,Pitch.height/2);
        Vector2d bottomLeftCorner = new Vector2d(-Pitch.width/2,-Pitch.height/2);
        Vector2d bottomRightCorner = new Vector2d(Pitch.width/2,-Pitch.height/2);

        // find goal end coordinates
        Vector2d topLeftGoal = new Vector2d(-Pitch.width/2,Pitch.goalWidth/2);
        Vector2d topRightGoal = new Vector2d(Pitch.width/2,Pitch.goalWidth/2);
        Vector2d bottomLeftGoal = new Vector2d(-Pitch.width/2,-Pitch.goalWidth/2);
        Vector2d bottomRightGoal = new Vector2d(Pitch.width/2,-Pitch.goalWidth/2);


        // check if top sideline intersection is valid
        if (isValidIntersection(position,velocity,topLeftCorner,topRightCorner)) {
            Vector2d intersection = getIntersectionPoint(position,velocity,topLeftCorner,topRightCorner);
            double newDistance = findDistance(position,intersection);
            if (newDistance < distance) {
                line = PitchLine.TOP_SIDELINE;
                boundsIntersection = intersection;
                distance = newDistance;
            }
        }
        // check if bottom sideline intersection is valid
        if (isValidIntersection(position,velocity,bottomLeftCorner,bottomRightCorner)) {
            Vector2d intersection = getIntersectionPoint(position,velocity,bottomLeftCorner,bottomRightCorner);
            double newDistance = findDistance(position,intersection);
            if (newDistance < distance) {
                line = PitchLine.BOTTOM_SIDELINE;
                boundsIntersection = intersection;
                distance = newDistance;
            }
        }
        // check if left sideline intersection is valid
        if (isValidIntersection(position,velocity,topLeftCorner,topLeftGoal) ||
                isValidIntersection(position,velocity,bottomLeftGoal,bottomLeftCorner)) {
            Vector2d intersection = getIntersectionPoint(position,velocity,topLeftCorner,bottomLeftCorner);
            double newDistance = findDistance(position,intersection);
            if (newDistance < distance) {
                line = PitchLine.LEFT_SIDELINE;
                boundsIntersection = intersection;
                distance = newDistance;
            }
        }
        // check if the left goal intersection is valid
        else if (isValidIntersection(position,velocity,topLeftGoal,bottomLeftGoal)) {
            Vector2d intersection = getIntersectionPoint(position,velocity,topLeftGoal,bottomLeftGoal);
            double newDistance = findDistance(position,intersection);
            if (newDistance < distance) {
                line = PitchLine.LEFT_GOAL;
                boundsIntersection = intersection;
                distance = newDistance;
            }
        }
        // check if the right sideline intersection is valid
        if (isValidIntersection(position,velocity,topRightCorner,topRightGoal) ||
                isValidIntersection(position,velocity,bottomRightGoal,bottomRightCorner)) {
            Vector2d intersection = getIntersectionPoint(position,velocity,topRightCorner,bottomRightCorner);
            double newDistance = findDistance(position,intersection);
            if (newDistance < distance) {
                line = PitchLine.RIGHT_SIDELINE;
                boundsIntersection = intersection;
                distance = newDistance;
            }
        }
        // check if the right goal intersection is valid
        else if (isValidIntersection(position,velocity,topRightGoal,bottomRightGoal)) {
            Vector2d intersection = getIntersectionPoint(position,velocity,topRightGoal,bottomRightGoal);
            double newDistance = findDistance(position,intersection);
            if (newDistance < distance) {
                line = PitchLine.RIGHT_GOAL;
                boundsIntersection = intersection;
                distance = newDistance;
            }
        }

        ballOutOfBounds(line, boundsIntersection);
    }

    private static boolean isValidIntersection(Vector2d position, Vector2d velocity, Vector2d p1, Vector2d p2) {
        Vector2d gradient = new Vector2d(p2);
        gradient.sub(p1);

        // if the two lines are parallel
        if (velocity.x * gradient.y == gradient.x * velocity.y) { return false; }

        Vector2d intersection = getIntersectionPoint(position,velocity,p1,p2);
        return (Math.round(intersection.x) >= Math.min(p1.x, p2.x) &&
                Math.round(intersection.x) <= Math.max(p1.x, p2.x) &&
                Math.round(intersection.y) >= Math.min(p1.y, p2.y) &&
                Math.round(intersection.y) <= Math.max(p1.y, p2.y));
    }

    private static Vector2d getIntersectionPoint(Vector2d p1, Vector2d g1, Vector2d p2, Vector2d p3) {
        // l1 = p1 + t*g1
        // l2 = p2 + s*(p3 - p2)

        // calculate gradient of l2
        Vector2d g2 = new Vector2d(p3);
        g2.sub(p2);

        // find t
        double t = (( g2.x * (p1.y - p2.y) + g2.y * (p2.x - p1.x) ) / ( (g1.x * g2.y) - (g2.x * g1.y) ));

        // plug t into the equation for l1
        return new Vector2d(p1.x + t * g1.x,p1.y + t * g1.y);
    }

    private static double findDistance(Vector2d p1, Vector2d p2) {
        Vector2d distance = new Vector2d(p2);
        distance.sub(p1);
        return distance.length();
    }

    public static boolean insidePitch(Vector2d p) {
        return (p.x >= Tools.centreX - width/2 &&
                p.x <= Tools.centreX + width/2 &&
                p.y >= Tools.centreY - height/2 &&
                p.y <= Tools.centreY + height/2);
    }

    public static boolean insideLeftPenaltyBox(Vector2d p) {
        return insidePenaltyBox(true,p);
    }

    public static boolean insideRightPenaltyBox(Vector2d p) {
        return insidePenaltyBox(false,p);
    }

    private static boolean insidePenaltyBox(boolean left, Vector2d p) {
        Boolean correctY = (p.y >= Tools.centreY - pBoxWidth/2 &&
                            p.y <= Tools.centreY + pBoxWidth/2);
        Boolean correctXLeft = (p.x >= Tools.centreX - width/2 &&
                                p.x <= Tools.centreX - width/2 + pBoxLength);
        Boolean correctXRight = (p.x >= Tools.centreX + width/2 - pBoxWidth &&
                                 p.x <= Tools.centreX + width/2);
        if (left) {
            return (correctY && correctXLeft);
        } else {
            return (correctY && correctXRight);
        }
    }

    private AI team1AI;
    private AI team2AI;
    private Ball ball;
    private Team team1;
    private Team team2;
    private int team1Score = 0;
    private int team2Score = 0;

    public Pitch(AI team1AI, AI team2AI, Ball ball) {
        this.team1AI = team1AI;
        this.team2AI = team2AI;
        this.ball = ball;

        // set the pitch for the ball
        ball.pitch = this;

        // set the pitch to listen to the ball
        ball.setFirstKick(true);

        // initiate teams
        initiateTeams();

        // initiate kick off
        kickOff(team1.getTeamID());

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
            update(period);
        }
    }

    private void ballOutOfBounds(PitchLine line, Vector2d intersection) {
        Vector2d point = new Vector2d(Math.round(intersection.x),Math.round(intersection.y));
        if (line == PitchLine.TOP_SIDELINE
                || line == PitchLine.BOTTOM_SIDELINE) {
            if (team1.getTeamID() == ball.getPossessorTeamID()) {
                throwIn(team2.getTeamID(),point);
            } else {
                throwIn(team1.getTeamID(),point);
            }
        } else if (line == PitchLine.LEFT_SIDELINE) {
            if (team1.getTeamID() == ball.getPossessorTeamID()) {
                corner(team2.getTeamID(),point);
            } else {
                goalKick(team1.getTeamID());
            }
        } else if (line == PitchLine.RIGHT_SIDELINE) {
            if (team1.getTeamID() == ball.getPossessorTeamID()) {
                goalKick(team2.getTeamID());
            } else {
                corner(team1.getTeamID(),point);
            }
        } else if (line == PitchLine.LEFT_GOAL) {
            team2Score += 1;
            kickOff(team1.getTeamID());
        } else if (line == PitchLine.RIGHT_GOAL) {
            team1Score += 1;
            kickOff(team2.getTeamID());
        }

        // waiting for kick allows all stationary players to become mobile again in 'kicked()'
        ball.setFirstKick(true);
    }

    private void initiateTeams() {
        team1 = new Team(this,ball,0,new ArrayList<Player>());
        team2 = new Team(this,ball,1,new ArrayList<Player>());
    }

    private void kickOff(int teamID) {
        // create teams
        team1 = new Team(this,ball,team1.getTeamID(),createTeam1KickOffPlayers(teamID));
        team2 = new Team(this,ball,team2.getTeamID(),createTeam2KickOffPlayers(teamID));

        // give teams to AI
        team1AI.updateTeam(team1);
        team2AI.updateTeam(team2);

        // put ball back in the middle of the pitch
        ball.setPosition(new Vector2d(0,0));
    }

    private List<Player> createTeam1KickOffPlayers(int teamID) {
        List<Player> players = new ArrayList<Player>();

        // if team1 is kicking
        if (team1.getTeamID() == teamID) {
            // create kick taker
            players.add(createStationaryPlayer(0,new Vector2d(0,0)));
            // create kick receiver
            players.add(createStationaryPlayer(1,new Vector2d(0,20)));

        // if team2 is kicking
        } else {
            // create 2 stationary players
            players.add(createStationaryPlayer(0,new Vector2d(-100,-20)));
            players.add(createStationaryPlayer(1,new Vector2d(-100,20)));
        }

        return players;
    }

    private List<Player> createTeam2KickOffPlayers(int teamID) {
        List<Player> players = new ArrayList<Player>();

        // if team2 is kicking
        if (team2.getTeamID() == teamID) {
            // create kick taker
            players.add(createStationaryPlayer(0,new Vector2d(0,0)));
            // create kick receiver
            players.add(createStationaryPlayer(1,new Vector2d(0,20)));

            // if team1 is kicking
        } else {
            // create 2 stationary players
            players.add(createStationaryPlayer(0,new Vector2d(100,-20)));
            players.add(createStationaryPlayer(1,new Vector2d(100,20)));
        }

        return players;
    }

    private void throwIn(int teamID, Vector2d point) {

        if (team1.getTeamID() == teamID) {
            team1 = new Team(this,ball,team1.getTeamID(),createTeam1ThrowInPlayers(point));
            team1AI.updateTeam(team1);
        } else {
            team2 = new Team(this,ball,team2.getTeamID(),createTeam2ThrowInPlayers(point));
            team2AI.updateTeam(team2);
        }

        ball.setPosition(point);
    }

    private List<Player> createTeam1ThrowInPlayers(Vector2d point) {
        return createThrowInPlayers(team1.getCopyOfPlayers(),point);
    }

    private List<Player> createTeam2ThrowInPlayers(Vector2d point) {
        return createThrowInPlayers(team2.getCopyOfPlayers(),point);
    }

    private List<Player> createThrowInPlayers(List<Player> players, Vector2d point) {
        // initialise distance and player index
        double distance = Double.POSITIVE_INFINITY;
        int playerIndex = -1;
        int playerID = -1;

        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (!(p instanceof Goalkeeper)) {
                double newDistance = findDistance(p.getPosition(), point);
                if (newDistance < distance) {
                    distance = newDistance;
                    playerIndex = i;
                    playerID = p.getPlayerID();
                }
            }
        }

        players.set(playerIndex,createStationaryPlayer(playerID,point));

        return players;
    }

    private void corner(int teamID, Vector2d point) {
        // check whether corner should be taken from the top or bottom corner
        double kickY;
        if (point.y >= 0) {
            kickY = (Pitch.height/2);
        } else {
            kickY = (-Pitch.height/2);
        }

        // create new teams with a 'throw in' from the corner
        if (team1.getTeamID() == teamID) {
            team1 = new Team(this,ball,team1.getTeamID(),createTeam1ThrowInPlayers(new Vector2d(point.x,kickY)));
            team1AI.updateTeam(team1);
        } else {
            team2 = new Team(this,ball,team2.getTeamID(),createTeam2ThrowInPlayers(new Vector2d(point.x,kickY)));
            team2AI.updateTeam(team2);
        }


        ball.setPosition(new Vector2d(point.x,kickY));
    }

    private void goalKick(int teamID) {
        ball.giveToGoalKeeper(teamID);
    }

    private Player createStationaryPlayer(int playerID, Vector2d position) {
        Vector2d pos = new Vector2d(position);
        Vector2d velocity = new Vector2d(0,0);
        Vector2d goalPosition = new Vector2d(pos);
        return new StationaryPlayer(playerID,pos,velocity,goalPosition);
    }

    public int getTeam1ID() { return team1.getTeamID(); }
    public int getTeam2ID() { return team2.getTeamID(); }

    public List<Player> getCopyOfPlayers(int teamID) {
        assert(team1.getTeamID() == teamID || team2.getTeamID() == teamID);
        if (team1.getTeamID() == teamID) { return team1.getCopyOfPlayers(); }
        return team2.getCopyOfPlayers();
    }

    public void update(int deltaTime) {
        team1.updatePlayers(deltaTime);
        team2.updatePlayers(deltaTime);
        ball.update(deltaTime);
        ball.updatePossession();
        checkBallOutOfBounds();
    }

    public boolean ballIsInPossession() {
        return ball.isInPossession();
    }

    public Vector2d getBallPossessorPosition() {
        if (ball.isInPossession()) {
            return getPlayerPosition(ball.getPossessorTeamID(),ball.getPossessorPlayerID());
        }
        return null;
    }

    public Vector2d getPlayerPosition(int teamID, int playerID) {
        if (team1.getTeamID() == teamID) {
            return team1.getPlayerPosition(playerID);
        }
        if (team2.getTeamID() == teamID) {
            return team2.getPlayerPosition(playerID);
        }
        return null;
    }

    public int getGoalKeeperID(int teamID) {
        if (team1.getTeamID() == teamID) {
            return team1.getGoalKeeperID();
        } else {
            return team2.getGoalKeeperID();
        }
    }

    public void kicked() {
        team1 = new Team(this,ball,team1.getTeamID(),mobiliseTeamPlayers(team1.getTeamID()));
        team2 = new Team(this,ball,team2.getTeamID(),mobiliseTeamPlayers(team2.getTeamID()));
        team1AI.updateTeam(team1);
        team2AI.updateTeam(team2);
    }

    private List<Player> mobiliseTeamPlayers(int teamID) {
        // select the players to iterate through
        List<Player> players;
        if (teamID == team1.getTeamID()) {
            players = team1.getCopyOfPlayers();
        } else {
            players = team2.getCopyOfPlayers();
        }

        // copy the players across, turning them into a default player if they are stationary
        List<Player> newPlayers = new ArrayList<Player>(players.size());
        for (int i = 0; i < players.size(); i++) {
            Player p = players.get(i);
            if (p instanceof StationaryPlayer) {
                Player player = new Player(p.getPlayerID(), p.getPosition(), p.getVelocity(), p.getGoalPosition());
                player.selected = p.selected;
                p = player;
            }

            newPlayers.add(i, p);
        }

        return newPlayers;
    }

    public void possessionTaken() {
        // temporary change to allow mouse to control team that's in possession
        if (team1.getTeamID() == ball.getPossessorTeamID()) {
            team1AI.updateTeam(team1);
        } else {
            team1AI.updateTeam(team2);
        }

        team1.possessionTaken();
        team2.possessionTaken();
    }

}
