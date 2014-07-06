package be.kdg.spacecrack.model;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@Table(name = "T_Game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private int id;

    @Column
    private String name;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "game")
    private List<Player> players = new ArrayList<Player>();


    @Column
    private int actionNumber;

    @Column
    private int loserPlayerId;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "game")
    private List<Game_Planet> gamePlanets = new ArrayList<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Game() {
       players = new ArrayList<Player>();
    }

    public int getId() {
        return id;
    }

    public void setId(int gameId) {
        this.id = gameId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.internalSetGame(this);
    }

    protected void internalAddPlayer(Player player) {
        players.add(player);
    }

    public int getLoserPlayerId() {
        return loserPlayerId;
    }

    public void setLoserPlayerId(int winnerPlayerId) {
        this.loserPlayerId = winnerPlayerId;
    }

    public int getActionNumber() {
        return actionNumber;
    }

    public void setActionNumber(int actionNumber) {
        this.actionNumber = actionNumber;
    }


    public void incrementActionNumber() {

        actionNumber++;
    }

    public void internalAddGame_Planet(Game_Planet game_planet) {
        gamePlanets.add(game_planet);
    }

    public void addGame_Planet(Game_Planet game_planet) {
        game_planet.internalSetGame(this);
        gamePlanets.add(game_planet);
    }

    public List<Game_Planet> getGamePlanets() {
        return gamePlanets;
    }

    public void setGamePlanets(List<Game_Planet> gamePlanets) {
        this.gamePlanets = gamePlanets;
    }

    public Game_Planet getGame_PlanetByPlanet(Planet planet) {
        return gamePlanets.stream().filter(gp -> gp.getPlanet().getName().equals(planet.getName())).findFirst().get();
    }
}
