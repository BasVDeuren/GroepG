package be.kdg.spacecrack.services.handlers;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.model.*;
import be.kdg.spacecrack.repositories.IColonyRepository;
import be.kdg.spacecrack.repositories.IPlanetRepository;
import be.kdg.spacecrack.repositories.IShipRepository;
import be.kdg.spacecrack.services.GameService;
import be.kdg.spacecrack.services.GraphAlgorithm;
import be.kdg.spacecrack.services.IGameService;
import be.kdg.spacecrack.services.IGameSynchronizer;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

@Component("moveShipHandler")
public class MoveShipHandler implements IMoveShipHandler {
    @Autowired
    private IColonyRepository colonyRepository;

    @Autowired
    private IPlanetRepository planetRepository;

    @Autowired
    private IShipRepository shipRepository;

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private IGameSynchronizer gameSynchronizer;

    public MoveShipHandler() {
    }

    public MoveShipHandler(IColonyRepository colonyRepository, IPlanetRepository planetRepository, IGameSynchronizer gameSynchronizer, IShipRepository shipRepository) {
        this.colonyRepository = colonyRepository;
        this.planetRepository = planetRepository;
        this.gameSynchronizer = gameSynchronizer;
        this.shipRepository = shipRepository;
    }

    @Override
    public void moveShip(Ship ship, Planet destinationPlanet) {
        Player player = ship.getPlayer();
        Game game = player.getGame();
        player.setCommandPoints(player.getCommandPoints() - GameService.MOVESHIPCOST);

        List<Colony> colonies = colonyRepository.findColoniesByGame(game);

        Optional<Colony> colonyOnPlanet = colonies.stream().filter(c -> colonyIsOnPlanet(c, destinationPlanet)).findFirst();
        if (colonyOnPlanet.isPresent()) {
            Colony colony = colonyOnPlanet.get();
            if (colonyIsFromThisPlayer(colony, player)) {
                moveShipToColony(ship, colony);
            } else {
                attackEnemyColony(ship, colony);
            }

        } else {
            moveAndColonize(ship, destinationPlanet);
        }
    }

    private boolean colonyIsFromThisPlayer(Colony colony, Player player) {
        return colony.getPlayer().getPlayerId() == player.getPlayerId();
    }

    private boolean colonyIsOnPlanet(Colony colony, Planet planet) {
        return colony.getGame_planet().getPlanet().getName().equals(planet.getName());
    }

    private Colony colonizePlanet(Planet planet, final Player player) {
        Game game = player.getGame();
        Game_Planet game_planetOptional = game.getGame_PlanetByPlanet(planet);
        final Colony colony = new Colony(game_planetOptional, player, GameService.NEW_COLONY_STRENGHT);
        colonizePerimeteredPlanets(player, colony);
        return colony;
    }



    private void colonizePerimeteredPlanets(Player player, Colony colony) {
        List<Perimeter> perimeters = detectPerimeter(player, colony);
        Game game = player.getGame();
        List<Colony> coloniesByGame = getColoniesByGame(game);
        List<Ship> shipsByGame = getShipsByGame(game);

        for (Perimeter perimeter : perimeters) {
            List<Planet> insidePlanets = perimeter.getInsidePlanets();

            insidePlanets.forEach(new Consumer<Planet>() {
                @Override
                public void accept(Planet insidePlanet) {
                    //If there is an existing colony on the insidePlanet delete it
                    coloniesByGame.stream()
                            .filter(c -> colonyIsOnPlanet(c, insidePlanet))
                            .forEach(c -> deletePiece(c));
                    //If there is a ship the enemy player on the insidePlanet delete it
                    shipsByGame.stream()
                            .filter(s -> shipIsOnPlanet(s, insidePlanet) && s.getPlayer().getPlayerId() != player.getPlayerId())
                            .forEach(s -> deletePiece(s));
                    //Create new Colonies on the insidePlanets.
                    Colony newColony = new Colony();
                    newColony.setGame_planet(game.getGame_PlanetByPlanet(insidePlanet));
                    newColony.setStrength(GameService.NEW_COLONY_STRENGHT);
                    player.addColony(newColony);
                }
            });
        }
    }

    private List<Ship> getShipsByGame(Game game) {
        List<Ship> ships = new ArrayList<Ship>();
        for (Player player : game.getPlayers()) {
            ships.addAll(player.getShips());
        }
        return ships;
    }

    private List<Colony> getColoniesByGame(Game game) {
        List<Colony> colonies = new ArrayList<>();
        game.getPlayers().forEach(p -> {
            colonies.addAll(p.getColonies());
        });
        return colonies;
    }

    private void moveAndColonize(Ship ship, Planet destinationPlanet) {
        Player player = ship.getPlayer();
        if (player.getCommandPoints() < IGameService.CREATECOLONYCOST) {
            throw new SpaceCrackNotAcceptableException("Insufficient CommandPoints");
        }

        Colony colony = colonizePlanet(destinationPlanet, player);
        ship.setGame_planet(colony.getGame_planet());
        player.setCommandPoints(player.getCommandPoints() - IGameService.CREATECOLONYCOST);
    }

    private void attackEnemyColony(Ship actingShip, Colony colony) {
        Player enemyPlayer = colony.getPlayer();
        List<Ship> enemyShips = enemyPlayer.getShips();

        Planet planet = colony.getGame_planet().getPlanet();
        Optional<Ship> enemyShip = enemyShips.stream().filter(s -> shipIsOnPlanet(s, planet)).findFirst();

        //if enemyShip is on the same planet as the colony
        if (enemyShip.isPresent()) {
            Piece winner = fightAndDetermineWinner(actingShip, enemyShip.get());
            if (winner == actingShip) {
                attackEnemyColony((Ship) winner, colony);
            }
            return;
        }


        Piece winner = fightAndDetermineWinner(actingShip, colony);
        if (winner == actingShip) {
            moveAndColonize(actingShip, planet);
        }
    }

    private boolean shipIsOnPlanet(Ship ship, Planet planet) {
        return ship.getGame_planet().getPlanet().getName().equals(planet.getName());
    }

    private Piece fightAndDetermineWinner(Piece piece1, Piece piece2) {
        int strengthDifference = piece1.getStrength() - piece2.getStrength();
        if (strengthDifference < 0) {
            deletePiece(piece1);
            piece2.setStrength(-strengthDifference);
            return piece2;
        } else if (strengthDifference > 0) {
            deletePiece(piece2);
            piece1.setStrength(strengthDifference);
            return piece1;
        } else {
            deletePiece(piece1);
            deletePiece(piece2);
            return null;
        }
    }

    private void deletePiece(Piece piece) {
        if (piece instanceof Ship) {
            Ship ship = (Ship) piece;
            ship.getGame_planet().removeShip();
            ship.getPlayer().removeShip(ship);
            shipRepository.delete(ship);
        } else {
            Colony colony = (Colony) piece;
            colony.getGame_planet().removeColony();
            colony.getPlayer().removeColony(colony);
            colonyRepository.delete(colony);
        }
    }

    /**
     * This method does an early check if the player is allowed to attempt to make a move.
     * If not the method will throw an unchecked exception which will translate to HttpStatusCode 406: NotAcceptable
     *
     * @param ship
     * @param destinationPlanet
     */
    @Override
    public void validateMove(Ship ship, Planet destinationPlanet) {
        if (ship.getPlayer().getCommandPoints() < IGameService.MOVESHIPCOST) {
            throw new SpaceCrackNotAcceptableException("The player cannot move because he has insufficient commandPoints!");
        }

        if (ship.getPlayer().isTurnEnded()) {
            throw new SpaceCrackNotAcceptableException("The player cannot execute the action because his turn has ended");
        }
        Planet sourcePlanet = ship.getGame_planet().getPlanet();
        boolean connected = false;
        Set<PlanetConnection> planetConnections = sourcePlanet.getPlanetConnections();

        for (PlanetConnection planetConnection : planetConnections) {
            if (planetConnection.getChildPlanet().getName().equals(destinationPlanet.getName())) {
                connected = true;
            }
        }
        if (!connected) {
            throw new SpaceCrackNotAcceptableException("Invalid move, the planets are not connected!");
        }
    }

    private void moveShipToColony(Ship ship, Colony colony) {
        Player player = ship.getPlayer();
        Planet planet = colony.getGame_planet().getPlanet();
        List<Ship> ships = player.getShips();
        Optional<Ship> shipToMergeWith = ships.stream().filter(s -> shipIsOnPlanet(s, planet)).findFirst();

        if (!shipToMergeWith.isPresent()) {
            ship.setGame_planet(colony.getGame_planet());
        } else {
            mergeAndGetShip(ship, shipToMergeWith.get());
        }
    }

    private Ship mergeAndGetShip(Ship shipToMerge, Ship shipToMergeWith) {
        shipToMergeWith.setStrength(shipToMergeWith.getStrength() + shipToMerge.getStrength());
        deletePiece(shipToMerge);
        return shipToMergeWith;
    }

    /**
     * Call when a new colony has been captured, try to find if it is part of a new perimeter
     */
    @Override
    public List<Perimeter> detectPerimeter(Player player, Colony newColony) {
        // List of perimeters to return
        List<Perimeter> perimeters = new ArrayList<Perimeter>();
        // Get all colonies of this player (= the graph to find perimeters within)
        UndirectedGraph<String, DefaultEdge> graph = new SimpleGraph<String, DefaultEdge>(DefaultEdge.class);
        List<Colony> colonies = player.getColonies();
        Map<String, Planet> playerPlanetsMap = new HashMap<String, Planet>();
        List<Planet> playerPlanetsList = new ArrayList<Planet>();
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
        List<Planet> targetPlanets = new ArrayList<Planet>(planetRepository.findAll()); // all planets
        targetPlanets.removeAll(playerPlanetsList); // all planets without already captured planets

        // Find chordless cycles of the graph
        List<List<String>> cycles = GraphAlgorithm.calculateChordlessCyclesFromVertex(graph, newColony.getGame_planet().getPlanet().getName());

        // For every cycles, make a possible perimeter
        for (List<String> cycle : cycles) {
            Perimeter perimeter = new Perimeter(new ArrayList<Planet>(), new ArrayList<Planet>());
            for (String vertex : cycle) {
                Planet planet = playerPlanetsMap.get(vertex);
                perimeter.getOutsidePlanets().add(planet);
            }
            perimeters.add(perimeter);
        }

        // For every polygon (=cycle) test if it contains a target planet
        for (Planet target : targetPlanets) {
            List<Perimeter> perimetersForTarget = new ArrayList<Perimeter>();
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
}
