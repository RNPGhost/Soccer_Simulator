package Main;

import Graphics.Tools;

import javax.vecmath.Vector2d;
import java.util.List;

public class Pitch{

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


    private Team team1;
    private Team team2;
    private Ball ball;

    public Pitch(Ball ball, Team team1, Team team2) {
        this.ball = ball;
        ball.pitch = this;
        this.team1 = team1;
        this.team2 = team2;
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
    }
}
