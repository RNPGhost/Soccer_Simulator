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

    private void checkBallOutOfBounds() {
        if (!ball.isInPossession()
                && !Pitch.insidePitch(ball.getPosition())) {
            PitchLine line = Pitch.findIntersectionLine(ball.getPosition(), ball.getVelocity());
            if (line == PitchLine.TOP_SIDELINE
                    || line == PitchLine.BOTTOM_SIDELINE) {
                System.out.println("Top or Bottom");
                if (team1.getTeamID() == ball.getPossessorTeamID()) {

                } else {

                }
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

    private static PitchLine findIntersectionLine(Vector2d position, Vector2d velocity) {
        PitchLine line = null;
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
            double newDistance = findDistance(position,
                    getIntersectionPoint(position,velocity,topLeftCorner,topRightCorner));
            if (newDistance < distance) {
                line = PitchLine.TOP_SIDELINE;
                distance = newDistance;
            }
        }
        // check if bottom sideline intersection is valid
        if (isValidIntersection(position,velocity,bottomLeftCorner,bottomRightCorner)) {
            double newDistance = findDistance(position,
                    getIntersectionPoint(position,velocity,bottomLeftCorner,bottomRightCorner));
            if (newDistance < distance) {
                line = PitchLine.BOTTOM_SIDELINE;
                distance = newDistance;
            }
        }
        // check if left sideline intersection is valid
        if (isValidIntersection(position,velocity,topLeftCorner,topLeftGoal) ||
                isValidIntersection(position,velocity,bottomLeftGoal,bottomLeftCorner)) {
            double newDistance = findDistance(position,
                    getIntersectionPoint(position,velocity,topLeftCorner,bottomLeftCorner));
            if (newDistance < distance) {
                line = PitchLine.LEFT_SIDELINE;
                distance = newDistance;
            }
        }
        // check if the left goal intersection is valid
        else if (isValidIntersection(position,velocity,topLeftGoal,bottomLeftGoal)) {
            double newDistance = findDistance(position,
                    getIntersectionPoint(position,velocity,topLeftGoal,bottomLeftGoal));
            if (newDistance < distance) {
                line = PitchLine.LEFT_GOAL;
                distance = newDistance;
            }
        }
        // check if the right sideline intersection is valid
        if (isValidIntersection(position,velocity,topRightCorner,topRightGoal) ||
                isValidIntersection(position,velocity,bottomRightGoal,bottomRightCorner)) {
            double newDistance = findDistance(position,
                    getIntersectionPoint(position,velocity,topRightCorner,bottomRightCorner));
            if (newDistance < distance) {
                line = PitchLine.RIGHT_SIDELINE;
                distance = newDistance;
            }
        }
        // check if the right goal intersection is valid
        else if (isValidIntersection(position,velocity,topRightGoal,bottomRightGoal)) {
            double newDistance = findDistance(position,
                    getIntersectionPoint(position,velocity,topRightGoal,bottomRightGoal));
            if (newDistance < distance) {
                line = PitchLine.RIGHT_GOAL;
                distance = newDistance;
            }
        }

        return line;
    }

    private static boolean isValidIntersection(Vector2d position, Vector2d velocity, Vector2d p1, Vector2d p2) {
        Vector2d gradient = new Vector2d(p2);
        gradient.sub(p1);

        // if the two lines are parallel
        if (velocity.x * gradient.y == gradient.x * velocity.y) { return false; }

        Vector2d intersection = getIntersectionPoint(position,velocity,p1,gradient);
        return (intersection.x >= Math.min(p1.x, p2.x) &&
                intersection.x <= Math.max(p1.x, p2.x) &&
                intersection.y >= Math.min(p1.y, p2.y) &&
                intersection.y <= Math.max(p1.y, p2.y));
    }

    private static Vector2d getIntersectionPoint(Vector2d p1, Vector2d g1, Vector2d p2, Vector2d g2) {
        // l1 = p1 + t*g1
        // l2 = p2 + s*g2

        // if g1 == g2, return null
        if (g1.x * g2.y == g2.x * g1.y) { return null; }

        // otherwise, find t
        double t = ( g2.x * (p1.y - p2.y) + g2.y * (p2.x - p1.x) ) / ( g1.x * g2.y - g2.x * g1.y );

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

    private Team team1; // team 1 is on the left of the pitch
    private Team team2; // team 2 is on the right of the pitch
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
        checkBallOutOfBounds();
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
}
