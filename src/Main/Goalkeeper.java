package Main;

import javax.vecmath.Vector2d;

public class Goalkeeper extends Player {
    public Goalkeeper(int playerID, Vector2d position, Vector2d velocity, Vector2d goalPosition) {
        super(playerID,position,velocity,goalPosition);
    }

    private boolean validGoalPosition(Vector2d goalPosition) {
        // work out how to determine if this is the left goalie or the right goalie
        // eg if teamID matches team1.getTeamID() then left else right
        // either that, or push this decision up to team, and assume goal positions are valid at Player level
        return (Pitch.insideLeftPenaltyBox(goalPosition) ||
                Pitch.insideRightPenaltyBox(goalPosition));
    }

}
