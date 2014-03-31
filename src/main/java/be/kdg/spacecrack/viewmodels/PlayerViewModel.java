package be.kdg.spacecrack.viewmodels;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import java.util.List;

public class PlayerViewModel {
    private int playerId;
    private List<ColonyViewModel> colonies;
    private List<ShipViewModel> ships;
    private int commandPoints;
    private boolean turnEnded;
    private boolean requestAccepted;
    private int profileId;
    private String playerName;
    private int crack;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public boolean isRequestAccepted() {
        return requestAccepted;
    }

    public void setRequestAccepted(boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public List<ColonyViewModel> getColonies() {
        return colonies;
    }

    public void setColonies(List<ColonyViewModel> colonies) {
        this.colonies = colonies;
    }

    public List<ShipViewModel> getShips() {
        return ships;
    }

    public void setShips(List<ShipViewModel> ships) {
        this.ships = ships;
    }

    public int getCommandPoints() {
        return commandPoints;
    }

    public void setCommandPoints(int commandPoints) {
        this.commandPoints = commandPoints;
    }

    public boolean isTurnEnded() {
        return turnEnded;
    }

    public void setTurnEnded(boolean turnEnded) {
        this.turnEnded = turnEnded;
    }

    public int getCrack() {
        return crack;
    }

    public void setCrack(int crack) {
        this.crack = crack;
    }
}
