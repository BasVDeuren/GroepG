package be.kdg.spacecrack.model.game;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.game.gameturnstate.GameTurnState;
import be.kdg.spacecrack.services.GraphAlgorithm;
import lombok.NonNull;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

import javax.persistence.*;
import java.awt.*;
import java.util.*;
import java.util.List;

@Entity
@Audited
@Table(name = "T_Game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String name;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "game")
    private List<Player> players = new ArrayList<>();

    @Column
    private int actionNumber;

    @Version
    private int version;

    @Column
    private int loserPlayerId;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "game")
    private List<Game_Planet> gamePlanets = new ArrayList<>();

    @Column
    @Enumerated(EnumType.STRING)
    private GameTurnState gameTurnState;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Game() {
    }


    public GameTurnState getGameTurnState() {
        return gameTurnState;
    }

    public void setGameTurnState(GameTurnState gameTurnState) {
        this.gameTurnState = gameTurnState;
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


    public List<Colony> getColonies() {
        List<Colony> colonies = new ArrayList<>();
        players.stream().forEach(player -> {
            colonies.addAll(player.getColonies());
        });
        return colonies;
    }


    public List<Ship> getShips() {
        List<Ship> ships = new ArrayList<>();
        players.stream().forEach(player -> {
            ships.addAll(player.getShips());
        });
        return ships;
    }

    public List<Planet> getPlanets() {
        List<Planet> planets = new ArrayList<Planet>();
        gamePlanets.forEach(game_planet -> {
            planets.add(game_planet.getPlanet());
        });
        return planets;
    }

    /**
     * Call when a new colony has been captured, try to find if it is part of a new perimeter
     */
    public List<Perimeter> detectPerimeter(Player player, Colony newColony) {
        // List of perimeters to return
        List<Perimeter> perimeters = new ArrayList<>();
        // Get all colonies of this player (= the graph to find perimeters within)
        UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
        List<Colony> colonies = player.getColonies();
        Map<String, Planet> playerPlanetsMap = new HashMap<>();
        List<Planet> playerPlanetsList = new ArrayList<>();
        // add colonies to the graph
        for (Colony colony : colonies) {
            Planet planet = colony.getGame_planet().getPlanet();
            playerPlanetsList.add(planet);
            playerPlanetsMap.put(planet.getName(), planet);
            graph.addVertex(planet.getName());
        }
        // add the new colony as well
        Planet newPlanet = newColony.getGame_planet().getPlanet();
        playerPlanetsList.add(newPlanet);
        playerPlanetsMap.put(newPlanet.getName(), newPlanet);
        graph.addVertex(newPlanet.getName());
        // add connections between colonies to the graph
        for (Planet planet : playerPlanetsList) {
            for (PlanetConnection connection : planet.getPlanetConnections()) {
                if (playerPlanetsList.contains(connection.getChildPlanet())) {
                    graph.addEdge(connection.getParentPlanet().getName(), connection.getChildPlanet().getName());
                }
            }
        }

        // Get all the other planets of the map (all planets - player colonies)
        List<Planet> targetPlanets = new ArrayList<>(getPlanets()); // all planets
        targetPlanets.removeAll(playerPlanetsList); // all planets without already captured planets

        // Find chordless cycles of the graph
        List<List<String>> cycles = GraphAlgorithm.calculateChordlessCyclesFromVertex(graph, newColony.getGame_planet().getPlanet().getName());

        // For every cycles, make a possible perimeter
        for (List<String> cycle : cycles) {
            Perimeter perimeter = new Perimeter(new ArrayList<>(), new ArrayList<>());
            for (String vertex : cycle) {
                Planet planet = playerPlanetsMap.get(vertex);
                perimeter.getOutsidePlanets().add(planet);
            }
            perimeters.add(perimeter);
        }

        // For every polygon (=cycle) test if it contains a target planet
        for (Planet target : targetPlanets) {
            List<Perimeter> perimetersForTarget = new ArrayList<>();
            for (Perimeter perimeter : perimeters) {
                Polygon polygon = new Polygon();
                for (Planet planet : perimeter.getOutsidePlanets()) {
                    polygon.addPoint(planet.getX(), planet.getY());
                }

                if (polygon.contains(target.getX(), target.getY())) {
                    // This is a perimeter for this target planet (but check if it is the smallest)
                    perimetersForTarget.add(perimeter);
                }
            }

            if (!perimetersForTarget.isEmpty()) {
                Perimeter smallestPerimeter = perimetersForTarget.get(0);
                for (Perimeter perimeter : perimetersForTarget) {
                    if (perimeter.getOutsidePlanets().size() < smallestPerimeter.getOutsidePlanets().size()) {
                        smallestPerimeter = perimeter;
                    }
                }

                smallestPerimeter.getInsidePlanets().add(target);
            }
        }

        // Remove all the perimeters without inside planets
        for (Iterator<Perimeter> i = perimeters.iterator(); i.hasNext(); ) {
            Perimeter perimeter = i.next();
            if (perimeter.getInsidePlanets().size() == 0) {
                i.remove();
            }
        }

        return perimeters;
    }

    public void notifyTurnEnded(@NonNull Player player) {
        gameTurnState = gameTurnState.notifyTurnEnded(player.getGame());
    }


    public boolean isFinished() {
        return loserPlayerId != 0;
    }

    public void checkLost() {
        for (Player player : getPlayers()) {
            if (player.getColonies().size() == 0) {
                setLoserPlayerId(player.getPlayerId());
            }
        }
    }

    public void readyThePlayers() {
        for (Player p : getPlayers()) {
            p.setReadyToPlay(true);
        }
    }

    public int getVersion() {
        return version;
    }

    protected void setVersion(int version) {
        this.version = version;
    }
}
