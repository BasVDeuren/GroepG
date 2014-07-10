package be.kdg.spacecrack.unittests;

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.model.*;
import be.kdg.spacecrack.repositories.*;
import be.kdg.spacecrack.services.GameService;
import be.kdg.spacecrack.services.GameSynchronizer;
import be.kdg.spacecrack.services.IGameService;
import be.kdg.spacecrack.services.IGameSynchronizer;
import be.kdg.spacecrack.utilities.ViewModelConverter;
import org.apache.commons.collections.CollectionUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
public class GameServiceTests extends BaseUnitTest {
    @Autowired
    private IPlayerRepository playerRepository;
    @Autowired
    private IColonyRepository colonyRepository;
    @Autowired
    private IGameRepository gameRepository;
    @Autowired
    private IShipRepository shipRepository;
    @Autowired
    private PlanetConnectionRepository planetConnectionRepository;
    @Autowired
    private PlatformTransactionManager transactionManager;
    @Autowired
    private EntityManagerFactory entityManagerFactory;
    @Autowired
    private IProfileRepository profileRepository;

    private GameService gameService;
    private User user;
    private Profile opponentProfile;


    @Before
    public void setUp() throws Exception {
        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);
        gameService = new GameService(planetRepository, colonyRepository, shipRepository, playerRepository, gameRepository, new ViewModelConverter(), mockGameSynchronizer, null);
        user = new User();
        Profile profile = new Profile();
        opponentProfile = new Profile();
        profileRepository.save(profile);
        profileRepository.save(opponentProfile);
        User opponentUser = new User();
        opponentUser.setProfile(opponentProfile);
        user.setProfile(profile);

    }

    private Game createGame() {
        int gameId = gameService.createGame(user.getProfile(), "SpaceCrackName", opponentProfile);
        Game game = gameService.getGameByGameId(gameId);

        return game;
    }

    @Transactional
    @Test
    public void moveShipAndBuildColony_validPlanet_shipmovedandColonyBuilt() throws Exception {
        Game game = createGame();

        Player player = game.getPlayers().get(0);
        int oldCommandPoints = player.getCommandPoints();

        Ship ship = player.getShips().get(0);

        gameService.moveShip(ship.getShipId(), "b");
        Player playerDb = playerRepository.findOne(player.getPlayerId());
        Planet shipLocation = gameService.getShipLocationByShipId(ship.getShipId());

        assertEquals("b", shipLocation.getName());
        assertEquals("Player should have lost commandPoints", oldCommandPoints - GameService.MOVESHIPCOST - GameService.CREATECOLONYCOST, playerDb.getCommandPoints());
    }

    @Transactional
    @Test(expected = SpaceCrackNotAcceptableException.class)
    public void moveShip_invalidPlanet_shipNotMoved() throws Exception {
        Game game = createGame();
        Ship ship = game.getPlayers().get(0).getShips().get(0);
        gameService.moveShip(ship.getShipId(), "f");
        Planet shipLocation = gameService.getShipLocationByShipId(ship.getShipId());

        assertEquals("a", shipLocation.getName());
    }

    @Transactional
    @Test
    public void moveShipAndCreateColony_validPlanetNoColonyOnPlanet_ColonyPlaced() throws Exception {
        Game game = createGame();
        Ship ship = game.getPlayers().get(0).getShips().get(0);
        gameService.moveShip(ship.getShipId(), "b");

        Ship shipDb = shipRepository.findOne(ship.getShipId());

        List<Colony> colonies = shipDb.getPlayer().getColonies();
        assertEquals("The player should have 2 colonies", 2, colonies.size());
        assertEquals("The second colony the player should have is on planet b.", "b", colonies.get(1).getGame_planet().getPlanet().getName());
    }

    @Transactional
    @Test
    public void moveShipAndCreateColony_PlanetAlreadyColonizedByPlayer_NoColonyPlaced() throws Exception {
        Game game = createGame();
        Ship ship = game.getPlayers().get(0).getShips().get(0);
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "a");

        Ship shipDb = shipRepository.findOne(ship.getShipId());

        List<Colony> colonies = shipDb.getPlayer().getColonies();
        assertEquals("The player should have Only 2 colonies", 2, colonies.size());
    }

    @Transactional
    @Test(expected = SpaceCrackNotAcceptableException.class)
    public void moveShip_NoCommandPoints_SpaceCrackNotAcceptableException() throws Exception {
        Game game = createGame();
        Ship ship = game.getPlayers().get(0).getShips().get(0);

        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");

    }

    @Transactional
    @Test
    public void getAllGamesFromPlayer_validPlayer_gamesRetrieved() throws Exception {
        Game game = createGame();

        IGameRepository gameRepository = mock(IGameRepository.class);
        ArrayList<Game> expected = new ArrayList<Game>();
        expected.add(new Game());
        expected.add(new Game());
        stub(gameRepository.getGamesByProfile(user.getProfile())).toReturn(expected);

        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);

        GameService gameService1 = new GameService(planetRepository, colonyRepository, shipRepository, playerRepository, gameRepository, new ViewModelConverter(), mockGameSynchronizer, null);

        List<Game> actual = gameService1.getGames(user);

        verify(gameRepository, VerificationModeFactory.times(1)).getGamesByProfile(user.getProfile());
        assertEquals(expected, actual);
    }

    @Test
    @Transactional
    public void getGameFromGameId() throws Exception {
        Game expected = createGame();

        IGameRepository gameRepository = mock(IGameRepository.class);

        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);

        GameService gameService1 = new GameService(planetRepository, colonyRepository, shipRepository, playerRepository, gameRepository, new ViewModelConverter(), mockGameSynchronizer, null);
        stub(gameRepository.findOne(expected.getId())).toReturn(expected);

        Game actual = gameService1.getGameByGameId(expected.getId());

        verify(gameRepository, VerificationModeFactory.times(1)).findOne(expected.getId());
        assertEquals("Actual gameId should be the same as the expected gameId", expected.getId(), actual.getId());
    }

    @Transactional
    @Test(expected = SpaceCrackNotAcceptableException.class)
    public void endPlayerTurn() throws Exception {

        Game game = createGame();
        Player player = game.getPlayers().get(0);
        Player player2 = game.getPlayers().get(1);
        int oldCommandPoints = player.getCommandPoints();
        gameService.endTurn(player.getPlayerId());
        // gameService.endTurn(player2.getPlayerId());
        player = playerRepository.findOne(player.getPlayerId());

        assertEquals(oldCommandPoints, player.getCommandPoints());
        gameService.moveShip(player.getShips().get(0).getShipId(), "b");
    }

    @Transactional
    @Test
    public void endTurnBothPlayers_newCommandPointsAndCrack() throws Exception {
        Game game = createGame();
        Player player1 = game.getPlayers().get(0);
        int oldCommandPointsOfPlayer1 = player1.getCommandPoints();
        int oldCrackOfPlayer1 = player1.getCrack();
        gameService.endTurn(player1.getPlayerId());
        Player player2 = game.getPlayers().get(1);
        int oldCommandPointsOfPlayer2 = player2.getCommandPoints();
        int oldCrackOfPlayer2 = player2.getCrack();


        gameService.endTurn(player2.getPlayerId());
        player1 = playerRepository.findOne(player1.getPlayerId());
        player2 = playerRepository.findOne(player2.getPlayerId());

        assertEquals("player1's turn shouldn't be ended", false, player1.isTurnEnded());
        assertEquals("player2's turn shouldn't be ended", false, player2.isTurnEnded());

        assertEquals(oldCommandPointsOfPlayer1 + GameService.COMMANDPOINTS_PER_TURN, player1.getCommandPoints());
        assertEquals(oldCommandPointsOfPlayer2 + GameService.COMMANDPOINTS_PER_TURN, player2.getCommandPoints());

        assertEquals(oldCrackOfPlayer1 + GameService.CRACK_PER_COLONY, player1.getCrack());
        assertEquals(oldCrackOfPlayer2 + GameService.CRACK_PER_COLONY, player2.getCrack());

        gameService.moveShip(player1.getShips().get(0).getShipId(), "b");
        gameService.moveShip(player2.getShips().get(0).getShipId(), "b3");
    }

    @Transactional
    @Test
    public void buildShip_NoShipOnPlanetEnoughCommandPoints_shipBuilt() {
        Game game = createGame();
        Player player = game.getPlayers().get(0);
        Ship ship = player.getShips().get(0);
        int oldAmountOfShips = player.getShips().size();
        int oldCrack = player.getCrack();

        gameService.moveShip(ship.getShipId(), "b");
        Player playerDb = playerRepository.findOne(player.getPlayerId());
        int oldCommandPoints = playerDb.getCommandPoints();
        Colony colony = player.getColonies().get(0);
        gameService.buildShip(colony.getId());
        playerDb = playerRepository.findOne(player.getPlayerId());
        List<Ship> playerDbShips = playerDb.getShips();
        Ship newShip = playerDbShips.get(1);
        assertEquals("Player should have 1 more ship", oldAmountOfShips + 1, playerDbShips.size());

        assertEquals("The ship should have strength", GameService.NEW_SHIP_STRENGTH, newShip.getStrength());
        assertEquals("Ship should be build on colony's planet", colony.getGame_planet(), playerDbShips.get(playerDbShips.size() - 1).getGame_planet());
        assertEquals("Player should have lost 3 commandPoints", oldCommandPoints - GameService.BUILDSHIP_COST, playerDb.getCommandPoints());
        assertEquals("Player should have lost 30 crack", oldCrack - IGameService.BUILDSHIP_CRACK_COST, playerDb.getCrack());


    }

    @Transactional
    @Test(expected = SpaceCrackNotAcceptableException.class)
    public void buildShip_NoShipOnPlanetNotEnoughCommandPoints_NoShipBuilt() {
        Game game = createGame();
        Player player = game.getPlayers().get(0);
        Ship ship = player.getShips().get(0);
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");
        gameService.moveShip(ship.getShipId(), "b");
        gameService.moveShip(ship.getShipId(), "c");
        Colony colony = player.getColonies().get(0);
        gameService.buildShip(colony.getId());
    }


    @Transactional
    @Test(expected = SpaceCrackNotAcceptableException.class)
    public void buildShip_NoShipOnPlanetNotEnoughCrack_NoShipBuilt() {
        Game game = createGame();
        Player player = game.getPlayers().get(0);
        Colony colony = player.getColonies().get(0);
        gameService.buildShip(colony.getId());
        gameService.buildShip(colony.getId());
        gameService.buildShip(colony.getId());
        gameService.buildShip(colony.getId());


    }

    @Transactional
    @Test
    public void buildShip_ShipOnPlanetEnoughCommandPoints_shipMerged() {
        Game game = createGame();
        Player player = game.getPlayers().get(0);
        Ship ship = player.getShips().get(0);
        int oldShipStrength = ship.getStrength();
        int oldAmountOfShips = player.getShips().size();

        Player playerDb = playerRepository.findOne(player.getPlayerId());
        int oldCommandPoints = playerDb.getCommandPoints();
        int oldCrack = playerDb.getCrack();
        Colony colony = player.getColonies().get(0);
        gameService.buildShip(colony.getId());
        playerDb = playerRepository.findOne(player.getPlayerId());
        Ship shipDb = playerDb.getShips().get(0);
        List<Ship> playerDbShips = playerDb.getShips();

        assertEquals("Player shouldn't have more ships than before", oldAmountOfShips, playerDbShips.size());
        assertEquals("Ship should be build on colony's planet", colony.getGame_planet(), playerDbShips.get(playerDbShips.size() - 1).getGame_planet());
        assertEquals("The ship standing on the planet should now be more powerful", oldShipStrength + GameService.NEW_SHIP_STRENGTH, shipDb.getStrength());
        assertEquals("Player should have lost 1 commandPoint", oldCommandPoints - GameService.BUILDSHIP_COST, playerDb.getCommandPoints());
        assertEquals("Player should have lost 30 crack", oldCrack - IGameService.BUILDSHIP_CRACK_COST, playerDb.getCrack());

    }

    @Transactional
    @Test
    public void checkVictory_player2HasNoColonies_Player1Wins() throws Exception {
        //region Arrange
        Game game = createGame();
        Player player2 = game.getPlayers().get(1);
        player2.setColonies(new ArrayList<>());
        Ship ship = game.getPlayers().get(0).getShips().get(0);

        IShipRepository mockShipRepository = mock(IShipRepository.class);
        stub(mockShipRepository.findOne(ship.getShipId())).toReturn(ship);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IPlanetRepository mockPlanetRepository = mock(IPlanetRepository.class);

        GameSynchronizer mockGameSynchronizer = mock(GameSynchronizer.class);

        GameService gameServiceWithMockedMoveShipHandler = new GameService(planetRepository, null, mockShipRepository, null, mockGameRepository, new ViewModelConverter(), mockGameSynchronizer, null);
        //endregion
        //region Act
        gameServiceWithMockedMoveShipHandler.moveShip(ship.getShipId(), "b");
        //endregion
        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        verify(mockGameSynchronizer, VerificationModeFactory.times(1)).updateGame(gameArgumentCaptor.capture());
        Game gameResultGame = gameArgumentCaptor.getValue();
        assertEquals("Player2 should have lost.", game.getPlayers().get(1).getPlayerId(), gameResultGame.getLoserPlayerId());
    }

    @Transactional
    @Test(expected = SpaceCrackNotAcceptableException.class)
    public void moveShipAfterVictory_Player1HasShipLeft_Player1ShipCantMoveNoMore() throws Exception {
        Game game = createGame();
        game.setLoserPlayerId(game.getPlayers().get(0).getPlayerId());
        Player player2 = game.getPlayers().get(1);
        Ship ship = player2.getShips().get(0);

        IShipRepository mockShipRepository = mock(IShipRepository.class);
        stub(mockShipRepository.findOne(ship.getShipId())).toReturn(ship);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IPlanetRepository mockPlanetRepository = mock(IPlanetRepository.class);

        GameService gameServiceWithMockedMoveShipHandler = new GameService(mockPlanetRepository, null, mockShipRepository, null, mockGameRepository, new ViewModelConverter(), mock(GameSynchronizer.class), null);
        gameServiceWithMockedMoveShipHandler.moveShip(ship.getShipId(), "b3");
    }

    @Transactional
    @Test
    public void detectPerimeter_perimeterWithPlanetNotOnEdge_perimeterDetected() throws Exception {
        Game game = createGame();
        Player player = game.getPlayers().get(0);

        String[] planetNames = {"e", "f", "h", "i"}; // perimeter (without last conquered planet)
        String conqueredPlanetName = "c";
        String surrounded = "g"; // inside
        List<Planet> expectedPerimeter = new ArrayList<Planet>();

        for (String name : planetNames) {
            Planet planet = planetRepository.getPlanetByName(name);
            Colony colony = new Colony(game.getGame_PlanetByPlanet(planet));
            player.addColony(colony);
            expectedPerimeter.add(planet);
        }

        Planet conqueredPlanet = planetRepository.getPlanetByName(conqueredPlanetName);
        Colony newColony = new Colony(game.getGame_PlanetByPlanet(conqueredPlanet));
        expectedPerimeter.add(conqueredPlanet);

        List<Perimeter> perimeters = game.detectPerimeter(player, newColony);

        // perimeters should only include 1 perimeter [hence get(0)]
        assertEquals("Only one planet should be surrounded by this perimeter.", perimeters.get(0).getInsidePlanets().size(), 1);
        assertEquals("Planet 'b' should be surrounded by this perimeter.", perimeters.get(0).getInsidePlanets().get(0).getName().toLowerCase(), surrounded.toLowerCase());
        assertTrue("Outside planets of perimeter should match.", CollectionUtils.isEqualCollection(perimeters.get(0).getOutsidePlanets(), expectedPerimeter));
    }

    @Transactional
    @Test
    public void detectPerimeter_perimeterWithPlanetHugeGraph_perimeterDetected() throws Exception {
        Game game = createGame();
        Player player = game.getPlayers().get(0);

        MapFactory factory = new MapFactory(planetRepository, planetConnectionRepository);
        SpaceCrackMap map = factory.getSpaceCrackMap();

        String[] planetNames = {"e", "f", "h", "i"}; // perimeter (without last conquered planet)
        String conqueredPlanetName = "c";
        String surrounded = "g"; // inside
        List<Planet> expectedPerimeter = new ArrayList<Planet>();

        Colony newColony = new Colony();
        for (Planet planet : map.getPlanets()) { // give the player every planet on the map except for the surrounded planet
            if (!planet.getName().equals(surrounded)) {
                Colony colony = new Colony(game.getGame_PlanetByPlanet(planet));
                player.addColony(colony);
                if (planet.getName().equals(conqueredPlanetName)) {
                    newColony = colony;
                }
            }
        }

        for (String name : planetNames) {
            Planet planet = planetRepository.getPlanetByName(name);
            expectedPerimeter.add(planet);
        }
        Planet conqueredPlanet = planetRepository.getPlanetByName(conqueredPlanetName);
        expectedPerimeter.add(conqueredPlanet);

        List<Perimeter> perimeters = game.detectPerimeter(player, newColony);

        // perimeters should only include 1 perimeter [hence get(0)]
        assertEquals("Only one planet should be surrounded by this perimeter.", perimeters.get(0).getInsidePlanets().size(), 1);
        assertEquals("Planet 'b' should be surrounded by this perimeter.", perimeters.get(0).getInsidePlanets().get(0).getName().toLowerCase(), surrounded.toLowerCase());
        assertTrue("Outside planets of perimeter should match.", CollectionUtils.isEqualCollection(perimeters.get(0).getOutsidePlanets(), expectedPerimeter));
    }

    @Transactional
    @Test
    public void detectPerimeter_perimeterWithTwoPlanets_perimeterDetected() throws Exception {
        Game game = createGame();
        Player player = game.getPlayers().get(0);

        String[] planetNames = {"c", "i", "j", "j2", "i2", "c2", "b2"}; // perimeter (without last conquered planet)
        String conqueredPlanetName = "b";
        String[] surroundedPlanetNames = {"d", "d2"}; // inside
        List<Planet> expectedPerimeter = new ArrayList<Planet>();

        for (String name : planetNames) {
            Planet planet = planetRepository.getPlanetByName(name);
            Colony colony = new Colony(game.getGame_PlanetByPlanet(planet));
            player.addColony(colony);
            expectedPerimeter.add(planet);
        }

        Planet conqueredPlanet = planetRepository.getPlanetByName(conqueredPlanetName);
        Colony newColony = new Colony(game.getGame_PlanetByPlanet(conqueredPlanet));
        expectedPerimeter.add(conqueredPlanet);

        List<Perimeter> perimeters = game.detectPerimeter(player, newColony);

        List<Planet> surroundedPlanets = new ArrayList<Planet>();
        for (String name : surroundedPlanetNames) {
            surroundedPlanets.add(planetRepository.getPlanetByName(name));
        }

        assertTrue("A perimeter should exist", !perimeters.isEmpty());
        assertEquals("Two planets should be surrounded by this perimeter.", perimeters.get(0).getInsidePlanets().size(), 2);
        assertTrue("Planets 'd' and 'd2' should be surrounded by this perimeter.", CollectionUtils.isEqualCollection(perimeters.get(0).getInsidePlanets(), surroundedPlanets));
        assertTrue("Outside planets of perimeter should match.", CollectionUtils.isEqualCollection(perimeters.get(0).getOutsidePlanets(), expectedPerimeter));
    }

    @Transactional
    @Test
    public void detectPerimeter_twoPerimeters_bothPerimetersDetected() throws Exception {
        Game game = createGame();
        Player player = game.getPlayers().get(0);

        String[] firstPerimeterNames = {"c", "i", "j", "j2", "d2", "b2", "b"}; // first perimeter
        String[] secondPerimeterNames = {"c", "i", "h", "f", "e"}; // second perimeter
        String conqueredPlanetName = "c";
        String firstPerimeterSurrounded = "d";
        String secondPerimeterSurrounded = "g";

        Colony newColony = new Colony();
        Perimeter firstExpectedPerimeter = new Perimeter(new ArrayList<Planet>(), new ArrayList<Planet>());
        for (String name : firstPerimeterNames) {
            Planet planet = planetRepository.getPlanetByName(name);
            Colony colony = new Colony(game.getGame_PlanetByPlanet(planet));
            player.addColony(colony);
            firstExpectedPerimeter.getOutsidePlanets().add(planet);
            if (planet.getName().equals(conqueredPlanetName)) {
                newColony = colony;
            }
        }
        firstExpectedPerimeter.getInsidePlanets().add(planetRepository.getPlanetByName(firstPerimeterSurrounded));

        Perimeter secondExpectedPerimeter = new Perimeter(new ArrayList<Planet>(), new ArrayList<Planet>());
        for (String name : secondPerimeterNames) {
            Planet planet = planetRepository.getPlanetByName(name);
            player.addColony(new Colony(game.getGame_PlanetByPlanet(planet)));
            secondExpectedPerimeter.getOutsidePlanets().add(planet);
        }
        secondExpectedPerimeter.getInsidePlanets().add(planetRepository.getPlanetByName(secondPerimeterSurrounded));

        List<Perimeter> expectedPerimeters = new ArrayList<Perimeter>();
        expectedPerimeters.add(firstExpectedPerimeter);
        expectedPerimeters.add(secondExpectedPerimeter);

        List<Perimeter> perimeters = game.detectPerimeter(player, newColony);

        assertTrue("Two perimeters should exist.", !perimeters.isEmpty());
        assertEquals("Two perimeters should exist.", perimeters.size(), 2);
        assertTrue("Expected and actual perimeters should match.", (expectedPerimeters.containsAll(perimeters) && perimeters.containsAll(expectedPerimeters)));
    }

    @Test
    @Transactional
    public void deleteGame() throws Exception {
        TransactionStatus status1 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));

        User deleteGameTestUser = new User("Bart", "bartjetopdocent", "bartjebartje@gmail.com", true);
        Profile profile = new Profile("Bart", "Bartels", null, null);
        deleteGameTestUser.setProfile(profile);

        transactionManager.commit(status1);

        TransactionStatus status2 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));

        int gameId = gameService.createGame(deleteGameTestUser.getProfile(), "SpaceCrackName", opponentProfile);
        Game game = gameService.getGameByGameId(gameId);

        transactionManager.commit(status2);

        TransactionStatus status3 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED));
        gameService.deleteGame(game.getId());
        transactionManager.commit(status3);

    }


}
