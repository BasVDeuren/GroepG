package be.kdg.spacecrack.services;

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.Exceptions.SpaceCrackUnexpectedException;
import be.kdg.spacecrack.model.*;
import be.kdg.spacecrack.repositories.*;
import be.kdg.spacecrack.services.handlers.IMoveShipHandler;
import be.kdg.spacecrack.utilities.IViewModelConverter;
import be.kdg.spacecrack.viewmodels.GameViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;


/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Component(value = "gameService")
@Transactional
public class GameService implements IGameService {


    @Autowired
    private IPlanetRepository planetRepository;

    @Autowired
    private IShipRepository shipRepository;

    @Autowired
    private IColonyRepository colonyRepository;

    @Autowired
    private IPlayerRepository playerRepository;

    @Autowired
    private IGameRepository gameRepository;

    @Autowired
    public IMoveShipHandler moveShipHandler;

    @Autowired
    private IViewModelConverter viewModelConverter;

    @Autowired
    private IGameSynchronizer gameSynchronizer;

    public GameService() {
    }

    public GameService(IPlanetRepository planetRepository, IColonyRepository colonyRepository, IShipRepository shipRepository, IPlayerRepository playerRepository, IGameRepository gameRepository, IMoveShipHandler moveShipHandler, IViewModelConverter viewModelConverter, IGameSynchronizer gameSynchronizer) {
        this.planetRepository = planetRepository;
        this.shipRepository = shipRepository;
        this.colonyRepository = colonyRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.moveShipHandler = moveShipHandler;
        this.viewModelConverter = viewModelConverter;
        this.gameSynchronizer = gameSynchronizer;
    }

    @Override
    public int createGame(Profile userProfile, String gameName, Profile opponentProfile) {
        Game game = new Game();

        Planet[] planetsArray = planetRepository.getAll();
        List<Planet> planets = Arrays.asList(planetsArray);

        planets.forEach(planet -> {
          Game_Planet game_planet = new Game_Planet(planet);
          game.addGame_Planet(game_planet);

        });
        Player player1 = new Player();
        Player player2 = new Player();

        userProfile.addPlayer(player1);
        opponentProfile.addPlayer(player2);

        player1.setCommandPoints(START_COMMAND_POINTS);
        player2.setCommandPoints(START_COMMAND_POINTS);

        player1.setCrack(PLAYER_START_CRACK);
        player2.setCrack(PLAYER_START_CRACK);

        player1.setRequestAccepted(true);
        player2.setRequestAccepted(false);

        player1.setGame(game);
        player2.setGame(game);

        Planet planetA = planetRepository.getPlanetByName("a");
        Planet planetA3 = planetRepository.getPlanetByName("a3");

        Optional<Game_Planet> game_planetAOptional = game.getGamePlanets().stream().filter(gp -> gp.getPlanet().getName().equals("a")).findFirst();
        Optional<Game_Planet> game_planetA3Optional = game.getGamePlanets().stream().filter(gp -> gp.getPlanet().getName().equals("a3")).findFirst();


        Game_Planet game_planetA = game_planetAOptional.get();
        Ship player1StartingShip = new Ship(game_planetA);
        Game_Planet game_planetA3 = game_planetA3Optional.get();
        Ship player2StartingShip = new Ship(game_planetA3);

        player1StartingShip.setStrength(NEW_SHIP_STRENGTH);
        player2StartingShip.setStrength(NEW_SHIP_STRENGTH);

        player1StartingShip.setPlayer(player1);
        player2StartingShip.setPlayer(player2);


        Colony player1StartingColony = new Colony(game_planetA);
        Colony player2StartingColony = new Colony(game_planetA3);

        player1StartingColony.setStrength(NEW_COLONY_STRENGHT);
        player2StartingColony.setStrength(NEW_COLONY_STRENGHT);

        player1StartingColony.setPlayer(player1);
        player2StartingColony.setPlayer(player2);

        game.setName(gameName);

        gameRepository.createOrUpdateGame(game);

        return game.getGameId();
    }

    @Override
    public void moveShip(Integer shipId, String planetName) {
        Ship ship = shipRepository.getShipByShipId(shipId);
        Game game = ship.getPlayer().getGame();
        Planet destinationPlanet = planetRepository.getPlanetByName(planetName);
        validateActionMakeSureGameIsNotFinishedYet(game);
        moveShipHandler.validateMove(ship, destinationPlanet);
        moveShipHandler.moveShip(ship, destinationPlanet);
        checkLost(game);
        game.incrementActionNumber();
        gameSynchronizer.updateGame(game);
    }

    private void validateActionMakeSureGameIsNotFinishedYet(Game game) {
        if (game.getLoserPlayerId() != 0) {
            throw new SpaceCrackNotAcceptableException("Game is already finished.");
        }
    }


    @Override
    public Planet getShipLocationByShipId(int shipId) {
        Ship shipDb = shipRepository.getShipByShipId(shipId);
        return shipDb.getGame_planet().getPlanet();
    }

    @Override
    public void endTurn(Integer playerID) {
        Player player = playerRepository.getPlayerByPlayerId(playerID);

        Game game = gameRepository.getGameByGameId(player.getGame().getGameId());
        Integer oldActionNumber = game.getActionNumber();


        if (!player.isTurnEnded()) {

            player.setTurnEnded(true);
            boolean allTurnsEnded = true;
            List<Player> players = game.getPlayers();

            for (Player p : players) {
                if (!p.isTurnEnded()) {
                    allTurnsEnded = false;
                }
            }
            if (allTurnsEnded) {
                for (Player p : players) {
                    startNewTurn(p);

                }
            }
        } else {
            throw new SpaceCrackNotAcceptableException("Turn is already ended");
        }

        gameSynchronizer.updateGameConcurrent(game, oldActionNumber);
    }

    private void startNewTurn(Player p) {
        int commandPoints = p.getCommandPoints();
        p.setCommandPoints(commandPoints + COMMANDPOINTS_PER_TURN);
        p.getColonies().forEach(colony ->  p.addCrack(IGameService.CRACK_PER_COLONY));
        p.setTurnEnded(false);
    }

    @Override
    public List<Game> getGames(User user) {
        return gameRepository.getGamesByProfile(user.getProfile());
    }

    @Override
    public Game getGameByGameId(int gameId) {
        return gameRepository.getGameByGameId(gameId);
    }

    private void checkLost(Game gameByGameId) {
        for (Player player : gameByGameId.getPlayers()) {
            if (player.getColonies().size() == 0) {
                gameByGameId.setLoserPlayerId(player.getPlayerId());
            }
        }
    }

    @Override
    public Player getActivePlayer(User user, Game game) {

        Stream<Player> gamePlayersStream = game.getPlayers().stream();
        Optional<Player> shipOptional = gamePlayersStream.filter(p -> user.getProfile().getPlayers().contains(p)).findFirst();
        if(!shipOptional.isPresent()){
            throw new SpaceCrackUnexpectedException("This user isn't playing this game");
        }
        return shipOptional.get();

    }

    @Override
    public void buildShip(Integer colonyId) {
        Ship shipOnPlanet = null;

        Colony colony = colonyRepository.getColonyById(colonyId);
        Player player = colony.getPlayer();

        Game game = player.getGame();
        if (player.getCommandPoints() < BUILDSHIP_COST) {
            throw new SpaceCrackNotAcceptableException("Insufficient commandpoints.");
        } else if (player.isTurnEnded()) {
            throw new SpaceCrackNotAcceptableException("Your turn has ended.");
        }else if(player.getCrack() < BUILDSHIP_CRACK_COST){
            throw new SpaceCrackNotAcceptableException("Insufficient crack.");
        }

        Optional<Ship> shipOptional = player.getShips().stream().filter(s -> s.getGame_planet().getPlanet().getName().equals(colony.getGame_planet().getPlanet().getName())).findFirst();

        if (shipOptional.isPresent()) {
            shipOnPlanet = shipOptional.get();
        }

        if (shipOnPlanet == null) {
            Ship ship = new Ship();
            ship.setStrength(NEW_SHIP_STRENGTH);
            ship.setPlayer(player);
            ship.setGame_planet(colony.getGame_planet());
        } else {
            shipOnPlanet.setStrength(shipOnPlanet.getStrength() + NEW_SHIP_STRENGTH);
        }

        player.setCommandPoints(player.getCommandPoints() - BUILDSHIP_COST);
        player.setCrack(player.getCrack()-BUILDSHIP_CRACK_COST);
        game.incrementActionNumber();
        gameSynchronizer.updateGame(game);
    }


    @Override
    public List<Integer> getRevisionNumbers(int gameId) {
        return gameRepository.getRevisionNumbers(gameId);
    }


    @Override
    public GameViewModel getGameRevisionByNumber(int gameId, Number number) {
        Game gameRevision = gameRepository.getGameRevision(number, gameId);
        return viewModelConverter.convertGameToReplayViewModel(gameRevision);

    }

    @Override
    public void acceptGameInvite(int gameId) {
        Game game = getGameByGameId(gameId);
        for (Player p : game.getPlayers()) {
            p.setRequestAccepted(true);
        }
        gameSynchronizer.updateGame(game);
    }

    @Override
    public void deleteGame(int gameId) {
        gameRepository.deleteGame(gameId);
    }


}
