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

import java.util.List;


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
    public static final int NEW_COLONY_STRENGHT = 1;

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

        Player player1 = new Player();
        Player player2 = new Player();

        userProfile.addPlayer(player1);
        opponentProfile.addPlayer(player2);

        player1.setCommandPoints(START_COMMAND_POINTS);
        player2.setCommandPoints(START_COMMAND_POINTS);

        player1.setRequestAccepted(true);
        player2.setRequestAccepted(false);

        player1.setGame(game);
        player2.setGame(game);

        Planet planetA = planetRepository.getPlanetByName("a");
        Planet planetA3 = planetRepository.getPlanetByName("a3");

        Ship player1StartingShip = new Ship(planetA);
        Ship player2StartingShip = new Ship(planetA3);

        player1StartingShip.setStrength(NEW_SHIP_STRENGTH);
        player2StartingShip.setStrength(NEW_SHIP_STRENGTH);

        player1StartingShip.setPlayer(player1);
        player2StartingShip.setPlayer(player2);


        Colony player1StartingColony = new Colony(planetA);
        Colony player2StartingColony = new Colony(planetA3);

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
        return shipDb.getPlanet();
    }

    @Override
    public void endTurn(Integer playerID) {
        Player player = playerRepository.getPlayerByPlayerId(playerID);
        Game game = gameRepository.getGameForUpdate(player.getGame());


        if (!player.isTurnEnded()) {
            int commandPoints = player.getCommandPoints();
            player.setCommandPoints(commandPoints + COMMANDPOINTS_PER_TURN);
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
                    p.setTurnEnded(false);

                }
            }
        } else {
            throw new SpaceCrackNotAcceptableException("Turn is already ended");
        }
        gameSynchronizer.updateGame(game);
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
        for (Player p : user.getProfile().getPlayers()) {
            for (Player gamePlayer : game.getPlayers()) {
                if (gamePlayer.getPlayerId() == p.getPlayerId()) {
                    return gamePlayer;
                }
            }
        }
        throw new SpaceCrackUnexpectedException("This user isn't playing this game");

    }

    @Override
    public void buildShip(Integer colonyId) {
        Ship shipOnPlanet = null;

        Colony colony = colonyRepository.getColonyById(colonyId);
        Player player = colony.getPlayer();

        Game game = player.getGame();
        if (player.getCommandPoints() < BUILDSHIP_COST || player.isTurnEnded()) {
            throw new SpaceCrackNotAcceptableException("Dear Sir or Lady, you have either run out of command points or your turn has ended, please wait for the other players to end their turn.");
        }
        for (Ship ship : player.getShips()) {
            if (ship.getPlanet().getName().equals(colony.getPlanet().getName())) {
                shipOnPlanet = ship;
            }
        }

        Ship ship;
        if (shipOnPlanet == null) {
            ship = new Ship();
            ship.setStrength(NEW_SHIP_STRENGTH);
            ship.setPlayer(player);
            ship.setPlanet(colony.getPlanet());
        } else {
            shipOnPlanet.setStrength(shipOnPlanet.getStrength() + NEW_SHIP_STRENGTH);
        }

        player.setCommandPoints(player.getCommandPoints() - BUILDSHIP_COST);
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
