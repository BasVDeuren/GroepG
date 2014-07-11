package be.kdg.spacecrack.model.game;

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.model.authentication.Profile;
import be.kdg.spacecrack.model.game.action.Action;
import be.kdg.spacecrack.services.GameService;
import be.kdg.spacecrack.services.IGameService;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;

import javax.persistence.*;
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


    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "profileId")
    private Profile profile;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL)
    private List<Colony> colonies = new ArrayList<>();

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "player")
    private List<Ship> ships = new ArrayList<>();

    @Column
    private int crack;

    @Column(name = "commandPoints")
    private int commandPoints;

    @Column
    private boolean turnEnded;

    @Column
    private boolean readyToPlay;

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

    public boolean isReadyToPlay() {
        return readyToPlay;
    }

    public void setReadyToPlay(boolean requestAccepted) {
        this.readyToPlay = requestAccepted;
    }

    public Player() {
    }

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

    protected void colonizePlanet(Game_Planet game_planet) {
        final Colony colony = new Colony(game_planet, this, IGameService.NEW_COLONY_STRENGHT);
        colonizePerimeteredPlanets(colony);
    }

    private void colonizePerimeteredPlanets(Colony colony) {
        List<Perimeter> perimeters = game.detectPerimeter(this, colony);
        List<Colony> coloniesByGame = game.getColonies();
        List<Ship> shipsByGame = game.getShips();

        for (Perimeter perimeter : perimeters) {
            List<Planet> insidePlanets = perimeter.getInsidePlanets();
            insidePlanets.forEach(insidePlanet -> {
                //If there is an existing colony on the insidePlanet delete it
                coloniesByGame.stream()
                        .filter(c -> c.isOnPlanet(insidePlanet))
                        .forEach(Colony::kill);
                //If there is a ship the enemy player on the insidePlanet delete it
                shipsByGame.stream()
                        .filter(s -> s.isOnPlanet(insidePlanet) && s.getPlayer().getPlayerId() != playerId)
                        .forEach(Ship::kill);
                //Create new Colonies on the insidePlanets.
                Colony newColony = new Colony();
                newColony.setGame_planet(game.getGame_PlanetByPlanet(insidePlanet));
                newColony.setStrength(GameService.NEW_COLONY_STRENGHT);
                addColony(newColony);
            });
        }
    }

    public void endTurn() {
        if (!isTurnEnded()) {
            setTurnEnded(true);
            game.notifyTurnEnded(this);
        } else {
            throw new SpaceCrackNotAcceptableException("Turn is already ended");
        }
    }

    public void startNewTurn() {

        int commandPoints = getCommandPoints();
        setCommandPoints(commandPoints + IGameService.COMMANDPOINTS_PER_TURN);
        getColonies().forEach(colony -> addCrack(IGameService.CRACK_PER_COLONY));
        setTurnEnded(false);
    }

    public void execute(Action action) {
        if(game.isFinished())        {
            throw new SpaceCrackNotAcceptableException("Game is already finished.");
        }
        if (turnEnded) {
            throw new SpaceCrackNotAcceptableException("Your turn has ended.");
        }
        if (commandPoints < action.getCommandPointsCost()) {
            throw new SpaceCrackNotAcceptableException("Insufficient commandpoints.");
        }
        if (crack < action.getCrackCost()) {
            throw new SpaceCrackNotAcceptableException("Insufficient crack.");
        }
        action.execute();
        game.incrementActionNumber();

    }
}
