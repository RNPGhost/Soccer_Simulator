package Main;

import javax.vecmath.Vector2d;

public class Ball {
    private int possessorTeamID;
    private int possessorPlayerID;
    private boolean inPossession;
    private Vector2d position;
    private Vector2d velocity;
    public Pitch pitch;

    public Ball(int teamID, int playerID) {
        inPossession = true;
        possessorTeamID = teamID;
        possessorPlayerID = playerID;
    }

    public Ball(Vector2d position, Vector2d velocity) {
        inPossession = false;
        this.position = position;
        this.velocity = velocity;
    }

    public boolean isInPossession() { return inPossession; }

    public synchronized int getPossessorTeamID() {
        assert(inPossession);
        return possessorTeamID;
    }

    public synchronized int getPossessorPlayerID() {
        assert(inPossession);
        return possessorPlayerID;
    }

    public synchronized Vector2d getPosition() {
        return position;
    }

    public synchronized void update(int deltaTime) {
        if (!inPossession) {
            // calculate acceleration = -v/2
            Vector2d acceleration = new Vector2d(velocity);
            acceleration.scale(-1/2f);

            // apply acceleration to velocity
            acceleration.scale(deltaTime / 1000f);
            velocity.add(acceleration);
            if (velocity.length() <= 0.1) {
                velocity = new Vector2d(0,0);
            }

            // apply velocity to position
            if (velocity.length() > 0.1) {
                Vector2d distanceTravelled = new Vector2d(velocity);
                distanceTravelled.scale(deltaTime / 1000f);
                position.add(distanceTravelled);
            }
        }
    }

    public synchronized void updatePossession() {
        for (Player p : pitch.getCopyOfPlayers(pitch.getTeam1ID())) {
            takePossession(p);
        }
        for (Player p : pitch.getCopyOfPlayers(pitch.getTeam2ID())) {
            takePossession(p);
        }
    }

    private void takePossession(Player p) {
        // if ball isn't in possession, or other team has ball, and player is close enough, take possession
        // update possessorTeamID and possessorPlayerID
        Double requiredDistance = 5.0; // the centre of the player must be within 0.5m of the centre of the ball
        Vector2d distance;
        if (inPossession) {
            distance = getPossessorPosition();
        } else {
            distance = new Vector2d(position);
        }
        distance.sub(p.getPosition());
        if (distance.length() <= requiredDistance) {
            possessorTeamID = p.teamID;
            possessorPlayerID = p.playerID;
            inPossession = true;
            // at some point, must make sure that this player has not possessed the ball in the last 2 seconds
        }
    }

    private Vector2d getPossessorPosition() {
        for (Player p : pitch.getCopyOfPlayers(possessorTeamID)) {
            if (p.playerID == possessorPlayerID) { return p.getPosition(); }
        }
        return null;
    }


    public synchronized boolean kick(int teamID, int playerID, Vector2d direction) {
        return false;
    }

    public synchronized Ball copy() {
        if (inPossession) {
            return new Ball(possessorTeamID,possessorPlayerID);
        } else {
            return new Ball(new Vector2d(position),new Vector2d(velocity));
        }
    }
}
