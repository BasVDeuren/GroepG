package be.kdg.spacecrack.model;

import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

/**
 * This class represents the player in a specific game, it contains information on his current turn, his resources, and contains his Ships and Colonies on the map.
 */
@Entity
@Table(name = "T_Player")
@Audited
public class Player {
    @Id
    @GeneratedValue
    private int playerId;

    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @ManyToOne( fetch = FetchType.EAGER)
    @JoinColumn(name = "profileId")
    private Profile profile;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL)
    private List<Colony> colonies = new ArrayList<Colony>();

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "player")
    private List<Ship> ships = new ArrayList<Ship>();

    @Column
    private int crack;

    @Column(name = "commandPoints")
    private int commandPoints;

    @Column
    private boolean turnEnded;

    @Column
    private boolean requestAccepted;

    @ManyToOne(cascade = CascadeType.ALL)
    private Game game;

    @Version
    private int versionNumber;
    public int getCommandPoints() {
        return commandPoints;
    }

    public void setCommandPoints(int commandPoints) {
        this.commandPoints = commandPoints;
    }

    public boolean isRequestAccepted() {
        return requestAccepted;
    }

    public void setRequestAccepted(boolean requestAccepted) {
        this.requestAccepted = requestAccepted;
    }

    public Player() {}

    public Player(Profile profile) {
        this.profile = profile;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public void setColonies(List<Colony> colonies) {
        this.colonies = colonies;
    }

    public void setShips(List<Ship> ships) {
        this.ships = ships;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
        profile.internalAddPlayer(this);
    }


    public List<Colony> getColonies() {
        return colonies;
    }

    public List<Ship> getShips() {
        return ships;
    }

    public boolean isTurnEnded() {
        return turnEnded;
    }

    public void setTurnEnded(boolean turnEnded) {
        this.turnEnded = turnEnded;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        this.game = game;
        game.internalAddPlayer(this);
    }

    protected void internalAddColony(Colony colony) {
        colonies.add(colony);
    }

    public void addColony(Colony colony) {

        colonies.add(colony);
        colony.internalSetPlayer(this);
    }

    public void removeColony(Colony colony) {
        colonies.remove(colony);
        colony.internalSetPlayer(null);
    }

    protected void internalAddShip(Ship ship) {
        ships.add(ship);
    }

    public void addShip(Ship ship) {
        ships.add(ship);
        ship.internalSetPlayer(this);
    }

    public void removeShip(Ship ship) {
        ships.remove(ship);
        ship.internalSetPlayer(null);
    }

    protected void internalSetGame(Game game) {
        this.game = game;
    }

    public void internalSetProfile(Profile profile) {
        this.profile = profile;
    }

    public int getCrack() {
        return crack;
    }

    public void setCrack(int crack) {
        this.crack = crack;
    }

    public void addCrack(int crack) {
        this.crack += crack;

    }
}
