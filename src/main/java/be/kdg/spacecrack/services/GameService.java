package be.kdg.spacecrack.services;

import be.kdg.spacecrack.Exceptions.SpaceCrackUnexpectedException;
import be.kdg.spacecrack.model.authentication.Profile;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.model.game.*;
import be.kdg.spacecrack.model.game.action.Action;
import be.kdg.spacecrack.model.game.action.BuildShipAction;
import be.kdg.spacecrack.model.game.action.MoveShipAction;
import be.kdg.spacecrack.repositories.*;
import be.kdg.spacecrack.utilities.IViewModelConverter;
import be.kdg.spacecrack.viewmodels.GameViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

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
    private IViewModelConverter viewModelConverter;

    @Autowired
    private IGameSynchronizer gameSynchronizer;

    @Autowired
    private GameRevisionRepository gameRevisionRepository;
    @Autowired
    private IMapFactory mapFactory;
    @Autowired
    private GameFactory gameFactory;

    public GameService() {
    }

    public GameService(IPlanetRepository planetRepository,
                       IColonyRepository colonyRepository,
                       IShipRepository shipRepository,
                       IPlayerRepository playerRepository,
                       IGameRepository gameRepository,
                       IViewModelConverter viewModelConverter,
                       IGameSynchronizer gameSynchronizer,
                       GameRevisionRepository gameRevisionRepository,
                       IMapFactory mapFactory,
                       GameFactory gameFactory) {
        this.planetRepository = planetRepository;
        this.shipRepository = shipRepository;
        this.colonyRepository = colonyRepository;
        this.playerRepository = playerRepository;
        this.gameRepository = gameRepository;
        this.viewModelConverter = viewModelConverter;
        this.gameSynchronizer = gameSynchronizer;
        this.gameRevisionRepository = gameRevisionRepository;
        this.mapFactory = mapFactory;
        this.gameFactory = gameFactory;
    }

    @Override
    public int createGame(Profile userProfile, String gameName, Profile opponentProfile) {
        SpaceCrackMap spaceCrackMap = mapFactory.getSpaceCrackMap();
        Game game = gameFactory.createGame(spaceCrackMap, userProfile, opponentProfile, gameName);
        Game savedGame = gameRepository.save(game);
        return savedGame.getId();
    }

    @Override
    public void moveShip(Integer shipId, String planetName) {
        Ship ship = shipRepository.findOne(shipId);
        Player player = ship.getPlayer();
        Game game = player.getGame();
        Planet destinationPlanet = planetRepository.getPlanetByName(planetName);

        Action action = new MoveShipAction(ship, destinationPlanet);
        player.execute(action);

        gameSynchronizer.updateGame(game);
    }

    @Override
    public Planet getShipLocationByShipId(int shipId) {
        Ship shipDb = shipRepository.findOne(shipId);
        return shipDb.getGame_planet().getPlanet();
    }

    @Override
    public void endTurn(Integer playerID) {
        Player player = playerRepository.findOne(playerID);
        Game game = player.getGame();

        player.endTurn();

        gameSynchronizer.updateGame(game);
    }

    @Override
    public List<Game> getGames(User user) {
        return gameRepository.getGamesByProfile(user.getProfile());
    }

    @Override
    public Game getGameByGameId(int gameId) {
        return gameRepository.findOne(gameId);
    }

    @Override
    public Player getActivePlayer(User user, Game game) {
        Stream<Player> gamePlayersStream = game.getPlayers().stream();
        Optional<Player> playerOptional = gamePlayersStream.filter(p -> user.getProfile().getPlayers().contains(p)).findFirst();
        if (!playerOptional.isPresent()) {
            throw new SpaceCrackUnexpectedException("This user isn't playing this game");
        }
        return playerOptional.get();

    }

    @Override
    public void buildShip(Integer colonyId) {
        Colony colony = colonyRepository.findOne(colonyId);
        Player player = colony.getPlayer();
        Game game = player.getGame();

        Action action = new BuildShipAction(colony);
        player.execute(action);

        gameSynchronizer.updateGame(game);
    }

    @Override
    public List<Integer> getRevisionNumbers(int gameId) {
        return gameRevisionRepository.getRevisionNumbers(gameId);
    }

    @Override
    public GameViewModel getGameRevisionByNumber(int gameId, Number number) {
        Game gameRevision = gameRevisionRepository.getGameRevision(number, gameId);
        return viewModelConverter.convertGameToReplayViewModel(gameRevision);
    }

    @Override
    public void acceptGameInvite(int gameId) {
        Game game = gameRepository.findOne(gameId);
        game.readyThePlayers();
        gameSynchronizer.updateGame(game);
    }

    @Override
    public void deleteGame(int gameId) {
        gameRepository.delete(gameId);
    }
}
