package be.kdg.spacecrack.unittests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.*;
import be.kdg.spacecrack.repositories.IGameRepository;
import be.kdg.spacecrack.repositories.IPlanetRepository;
import be.kdg.spacecrack.repositories.IPlayerRepository;
import be.kdg.spacecrack.repositories.IShipRepository;
import be.kdg.spacecrack.services.GameService;
import be.kdg.spacecrack.repositories.IColonyRepository;
import be.kdg.spacecrack.services.GameSynchronizer;
import be.kdg.spacecrack.services.IGameSynchronizer;
import be.kdg.spacecrack.services.handlers.MoveShipHandler;
import be.kdg.spacecrack.utilities.ViewModelConverter;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;

public class MoveShipUnitTests extends BaseUnitTest {



    @Transactional
    @Test
    public void moveShip_PlanetHasShipFromSamePlayer_ShipsMerged() throws Exception {
        //Arrange
        int argShipId = 1;
        String argDestinationPlanetName = "b";

        IShipRepository mockShipRepository = mock(IShipRepository.class);
        IColonyRepository mockColonyRepository = mock(IColonyRepository.class);
        IPlayerRepository mockPlayerRepository = mock(IPlayerRepository.class);

        Planet[] planets = createSimpleMapWith2Planets();

        Game game = new Game();
        Player player = createActingPlayer(game);
        Player opponent = createOpponent(game);

        Colony colonyA = new Colony();
        colonyA.setPlanet(planets[0]);
        colonyA.setPlayer(player);
        Colony colonyB = new Colony();
        colonyB.setPlanet(planets[1]);
        colonyB.setPlayer(player);
        List<Colony> colonies = new ArrayList<Colony>();
        colonies.add(colonyA);
        colonies.add(colonyB);
        stub(mockColonyRepository.getColoniesByGame(any(Game.class))).toReturn(colonies);

        Ship ship = new Ship();
        ship.setPlanet(planets[0]);
        Ship shipOnDestinationPlanet = new Ship();
        shipOnDestinationPlanet.setPlanet(planets[1]);

        player.addShip(ship);
        player.addShip(shipOnDestinationPlanet);

        stub(mockShipRepository.getShipByShipId(argShipId)).toReturn(ship);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IPlanetRepository mockPlanetRepository = mock(IPlanetRepository.class);
        stub(mockPlanetRepository.getPlanetByName(argDestinationPlanetName)).toReturn(planets[1]);
        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);
        GameService gameServiceWithMockedDependencies = new GameService(mockPlanetRepository, mockColonyRepository, mockShipRepository, mockPlayerRepository, mockGameRepository, new MoveShipHandler(mockColonyRepository, mockPlanetRepository, mockGameSynchronizer), new ViewModelConverter(), mockGameSynchronizer);
        int oldAmountOfShips = player.getShips().size();
        int oldCommandPoints = player.getCommandPoints();
        int expectedStrength = ship.getStrength() + shipOnDestinationPlanet.getStrength();
        //Act
        gameServiceWithMockedDependencies.moveShip(argShipId, argDestinationPlanetName);

        //Assert
        ArgumentCaptor<Game> argumentCaptor = ArgumentCaptor.forClass(Game.class);
        Mockito.verify(mockGameSynchronizer, VerificationModeFactory.times(1)).updateGame(argumentCaptor.capture());
        Game resultGame = argumentCaptor.getValue();
        Player resultPlayer = resultGame.getPlayers().get(0);
        List<Ship> resultPlayerShips = resultPlayer.getShips();
        assertEquals("Ships should be merged", oldAmountOfShips - 1, resultPlayerShips.size());
        assertEquals("Ship's strength should be merged", expectedStrength,  resultPlayerShips.get(0).getStrength()  );
        assertEquals("Player should have lost a commandPoint", oldCommandPoints - 1, resultGame.getPlayers().get(0).getCommandPoints());
    }

    @Transactional @Test
    public void moveShipConquer_ShipStrengthEqualToEnemyColonyStrenght_ShipLostColonyLost() throws Exception {

        int argShipId = 1;
        String argDestinationPlanetName = "b";
        int enemyColonyStrength = 1;
        int alliedShipStrenght = 1;

        IShipRepository mockShipRepository = mock(IShipRepository.class);
        IColonyRepository mockColonyRepository = mock(IColonyRepository.class);
        IPlayerRepository mockPlayerRepository = mock(IPlayerRepository.class);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IPlanetRepository mockPlanetRepository = mock(IPlanetRepository.class);
        GameSynchronizer mockGameSynchronizer = mock(GameSynchronizer.class);
        GameService gameServiceWithMockedDependencies = new GameService(mockPlanetRepository, mockColonyRepository, mockShipRepository, mockPlayerRepository, mockGameRepository, new MoveShipHandler(mockColonyRepository, mockPlanetRepository, mock(IGameSynchronizer.class)) , new ViewModelConverter(), mockGameSynchronizer);

        Planet[] planets = createSimpleMapWith2Planets();
        Game game =new Game();
        Player player = createActingPlayer(game);
        Player opponent = createOpponent(game);
        List<Colony> gameColonies = createAlliedAndEnemyColony(enemyColonyStrength, planets, player, opponent);
        Ship ship = createShip(alliedShipStrenght, planets[0], player);

        stub(mockColonyRepository.getColoniesByGame(any(Game.class))).toReturn(gameColonies);
        stub(mockShipRepository.getShipByShipId(argShipId)).toReturn(ship);
        stub(mockPlanetRepository.getPlanetByName(argDestinationPlanetName)).toReturn(planets[1]);
        int oldCommandPoints = ship.getPlayer().getCommandPoints();
        //Act
        gameServiceWithMockedDependencies.moveShip(argShipId, argDestinationPlanetName);

        //Assert
        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        Mockito.verify(mockGameSynchronizer, VerificationModeFactory.times(1)).updateGame(gameArgumentCaptor.capture());
        Game resultGame = gameArgumentCaptor.getValue();
        assertEquals("Ship should be deleted", 0, resultGame.getPlayers().get(0).getShips().size());
        assertEquals("Colony should be deleted", 0, resultGame.getPlayers().get(1).getColonies().size());
        assertEquals("Player should have lost a commandPoint", oldCommandPoints - 1, resultGame.getPlayers().get(0).getCommandPoints());
    }



    @Transactional @Test
    public void moveShip_ShipStrongerThanEnemyColony_EnemyColonyDeletedAlliedColonyCreated() throws Exception {
        //Arrange
        int argShipId = 1;
        String argDestinationPlanetName = "b";
        int enemyColonyStrength = 1;
        int alliedShipStrenght = 2;

        IShipRepository mockShipRepository = mock(IShipRepository.class);
        IColonyRepository mockColonyRepository = mock(IColonyRepository.class);
        IPlayerRepository mockPlayerRepository = mock(IPlayerRepository.class);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IPlanetRepository mockPlanetRepository = mock(IPlanetRepository.class);

        Planet[] planets = createSimpleMapWith2Planets();
        Game game = new Game();
        Player player = createActingPlayer(game);
        Player opponent = createOpponent(game);
        List<Colony> gameColonies = createAlliedAndEnemyColony(enemyColonyStrength, planets, player, opponent);
        Ship ship = createShip(alliedShipStrenght, planets[0], player);

        stub(mockColonyRepository.getColoniesByGame(any(Game.class))).toReturn(gameColonies);
        stub(mockShipRepository.getShipByShipId(argShipId)).toReturn(ship);
        stub(mockPlanetRepository.getPlanetByName(argDestinationPlanetName)).toReturn(planets[1]);
        stub(mockPlanetRepository.getAll()).toReturn(planets);
        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);
        GameService gameServiceWithMockedDependencies = new GameService(mockPlanetRepository, mockColonyRepository, mockShipRepository, mockPlayerRepository, mockGameRepository, new MoveShipHandler(mockColonyRepository, mockPlanetRepository, mockGameSynchronizer), new ViewModelConverter(), mockGameSynchronizer);
        int oldCommandPoints = player.getCommandPoints();
        //Act
        gameServiceWithMockedDependencies.moveShip(argShipId, argDestinationPlanetName);

         //Assert
        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        Mockito.verify(mockGameSynchronizer, VerificationModeFactory.times(1)).updateGame(gameArgumentCaptor.capture());
        Game resultGame = gameArgumentCaptor.getValue();
        Player resultPlayer1 = resultGame.getPlayers().get(0);
        Player resultPlayer2 = resultGame.getPlayers().get(1);
        Ship resultShip = resultPlayer1.getShips().get(0);

        assertEquals("Player 1 should now have 2 colonies", 2, resultPlayer1.getColonies().size());
        assertEquals("Player 2 should now have 0 colonies", 0, resultPlayer2.getColonies().size());
        assertEquals("ResultShip should now have 1 strength", 1, resultShip.getStrength());
        assertEquals("The player should have lost 2 commandPoints", oldCommandPoints - 2, resultShip.getPlayer().getCommandPoints());



    }


    @Transactional @Test
    public void moveShip_ShipWeakerThanEnemyColony_ShipGoneEnemyColonyWeakened() throws Exception {

        //Arrange
        int argShipId = 1;
        String argDestinationPlanetName = "b";
        int enemyColonyStrength = 2;
        int alliedShipStrength = 1;

        IShipRepository mockShipRepository = mock(IShipRepository.class);
        IColonyRepository mockColonyRepository = mock(IColonyRepository.class);
        IPlayerRepository mockPlayerRepository = mock(IPlayerRepository.class);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IPlanetRepository mockPlanetRepository = mock(IPlanetRepository.class);

        Planet[] planets = createSimpleMapWith2Planets();
        Game game = new Game();
        Player player = createActingPlayer(game);
        Player opponent = createOpponent(game);
        List<Colony> gameColonies = createAlliedAndEnemyColony(enemyColonyStrength, planets, player, opponent);
        Ship ship = createShip(alliedShipStrength, planets[0], player);

        stub(mockColonyRepository.getColoniesByGame(any(Game.class))).toReturn(gameColonies);
        stub(mockShipRepository.getShipByShipId(argShipId)).toReturn(ship);
        stub(mockPlanetRepository.getPlanetByName(argDestinationPlanetName)).toReturn(planets[1]);

        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);
        GameService gameServiceWithMockedDependencies = new GameService(mockPlanetRepository, mockColonyRepository, mockShipRepository, mockPlayerRepository, mockGameRepository, new MoveShipHandler(mockColonyRepository, mockPlanetRepository, mockGameSynchronizer), new ViewModelConverter(), mockGameSynchronizer);
        int oldCommandPoints = player.getCommandPoints();
        //Act
        gameServiceWithMockedDependencies.moveShip(argShipId, argDestinationPlanetName);

        //Assert
        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);

        Mockito.verify(mockGameSynchronizer, VerificationModeFactory.times(1)).updateGame(gameArgumentCaptor.capture());
        Game resultGame = gameArgumentCaptor.getValue();
        Player resultPlayer = resultGame.getPlayers().get(0);
        assertEquals("Ship should be gone", 0, resultPlayer.getShips().size());
        Player resultOpponent = resultGame.getPlayers().get(1);
        assertEquals("Colony should be weakened", 1, resultOpponent.getColonies().get(0).getStrength());
        assertEquals("The player should have lost 1 commandPoint", oldCommandPoints - 1, resultPlayer.getCommandPoints());



    }


    @Transactional @Test
    public void moveShipToPlanetWithEnemyShip_ShipWeakerThanEnemyShip_ShipGoneEnemyShipWeakened() throws Exception {

        //region Arrange
        int argShipId = 1;
        String argDestinationPlanetName = "b";
        int enemyColonyStrength = 1;
        int alliedShipStrength = 1;
        int enemyShipStrenght = 2;

        IShipRepository mockShipRepository = mock(IShipRepository.class);
        IColonyRepository mockColonyRepository = mock(IColonyRepository.class);
        IPlayerRepository mockPlayerRepository = mock(IPlayerRepository.class);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IPlanetRepository mockPlanetRepository = mock(IPlanetRepository.class);

        Planet[] planets = createSimpleMapWith2Planets();
        Game game = new Game();
        Player player = createActingPlayer(game);
        Player opponent = createOpponent(game);
        List<Colony> gameColonies = createAlliedAndEnemyColony(enemyColonyStrength, planets, player, opponent);
        Ship ship = createShip(alliedShipStrength, planets[0], player);
        Ship enemyShip =createShip(enemyShipStrenght, planets[1], opponent);
        int oldEnemyShipStrength = enemyShip.getStrength();
        stub(mockColonyRepository.getColoniesByGame(any(Game.class))).toReturn(gameColonies);
        stub(mockShipRepository.getShipByShipId(argShipId)).toReturn(ship);
        stub(mockPlanetRepository.getPlanetByName(argDestinationPlanetName)).toReturn(planets[1]);

        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);
        GameService gameServiceWithMockedDependencies = new GameService(mockPlanetRepository, mockColonyRepository, mockShipRepository, mockPlayerRepository, mockGameRepository, new MoveShipHandler(mockColonyRepository, mockPlanetRepository, mockGameSynchronizer), new ViewModelConverter(), mockGameSynchronizer);
        int oldCommandPoints = player.getCommandPoints();
        //endregion
        //region Act
        gameServiceWithMockedDependencies.moveShip(argShipId, argDestinationPlanetName);
        //endregion
        //region Assert
        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);

        Mockito.verify(mockGameSynchronizer, VerificationModeFactory.times(1)).updateGame(gameArgumentCaptor.capture());
        Game resultGame = gameArgumentCaptor.getValue();
        Player resultPlayer = resultGame.getPlayers().get(0);
        assertEquals("Ship should be gone", 0, resultPlayer.getShips().size());
        Player resultOpponent = resultGame.getPlayers().get(1);
        Ship resultEnemyShip = resultOpponent.getShips().get(0);
        assertEquals("Enemy Ship should be weakened", oldEnemyShipStrength - alliedShipStrength, resultEnemyShip.getStrength());
        assertEquals("The player should have lost 1 commandPoint", oldCommandPoints - 1, resultPlayer.getCommandPoints());
        //endregion
    }

    @Transactional @Test
    public void moveShipToPlanetWithEnemyShip_ShipsStrengthEqual_BothShipsGoneColonyUnharmed() throws Exception {
        //region Arrange
        int argShipId = 1;
        String argDestinationPlanetName = "b";
        int enemyColonyStrength = 1;
        int alliedShipStrength = 2;
        int enemyShipStrenght = 2;

        IShipRepository mockShipRepository = mock(IShipRepository.class);
        IColonyRepository mockColonyRepository = mock(IColonyRepository.class);
        IPlayerRepository mockPlayerRepository = mock(IPlayerRepository.class);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IPlanetRepository mockPlanetRepository = mock(IPlanetRepository.class);

        Planet[] planets = createSimpleMapWith2Planets();
        Game game = new Game();
        Player player = createActingPlayer(game);
        Player opponent = createOpponent(game);
        List<Colony> gameColonies = createAlliedAndEnemyColony(enemyColonyStrength, planets, player, opponent);
        Ship ship = createShip(alliedShipStrength, planets[0], player);
        Ship enemyShip =createShip(enemyShipStrenght, planets[1], opponent);
        enemyShip.getStrength();
        stub(mockColonyRepository.getColoniesByGame(any(Game.class))).toReturn(gameColonies);
        stub(mockShipRepository.getShipByShipId(argShipId)).toReturn(ship);
        stub(mockPlanetRepository.getPlanetByName(argDestinationPlanetName)).toReturn(planets[1]);

        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);
        GameService gameServiceWithMockedDependencies = new GameService(mockPlanetRepository, mockColonyRepository, mockShipRepository, mockPlayerRepository, mockGameRepository, new MoveShipHandler(mockColonyRepository, mockPlanetRepository, mockGameSynchronizer), new ViewModelConverter(), mockGameSynchronizer);
        int oldEnemyColonyAmount = opponent.getColonies().size();
        int oldCommandPoints = player.getCommandPoints();
        //endregion
        //region Act
        gameServiceWithMockedDependencies.moveShip(argShipId, argDestinationPlanetName);
        //endregion
        //region Assert
        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);

        Mockito.verify(mockGameSynchronizer, VerificationModeFactory.times(1)).updateGame(gameArgumentCaptor.capture());
        Game resultGame = gameArgumentCaptor.getValue();
        Player resultPlayer = resultGame.getPlayers().get(0);

        Player resultOpponent = resultGame.getPlayers().get(1);
        assertEquals("Ship should be gone", 0, resultPlayer.getShips().size());
        assertEquals("Enemy Ship should be gone", 0, resultOpponent.getShips().size());
        assertEquals("The player should have lost 1 commandPoint", oldCommandPoints - 1, resultPlayer.getCommandPoints());
        assertEquals("the opponent shouldn't have lost any colonies", oldEnemyColonyAmount, resultOpponent.getColonies().size());
        //endregion
    }

    @Transactional @Test
    public void moveShipToPlanetWithEnemyShip_ShipStrengthStrongerThanShipWeakerThanCombinedColonyAndShipStrenght_BothShipsDeletedColonyWeakened() throws Exception {
        //region Arrange
        int argShipId = 1;
        String argDestinationPlanetName = "b";
        int enemyColonyStrength = 2;
        int alliedShipStrength = 2;
        int enemyShipStrenght = 1;

        IShipRepository mockShipRepository = mock(IShipRepository.class);
        IColonyRepository mockColonyRepository = mock(IColonyRepository.class);
        IPlayerRepository mockPlayerRepository = mock(IPlayerRepository.class);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IPlanetRepository mockPlanetRepository = mock(IPlanetRepository.class);

        Planet[] planets = createSimpleMapWith2Planets();
        Game game = new Game();
        Player player = createActingPlayer(game);
        Player opponent = createOpponent(game);
        List<Colony> gameColonies = createAlliedAndEnemyColony(enemyColonyStrength, planets, player, opponent);
        Ship ship = createShip(alliedShipStrength, planets[0], player);
        Ship enemyShip =createShip(enemyShipStrenght, planets[1], opponent);

        stub(mockColonyRepository.getColoniesByGame(any(Game.class))).toReturn(gameColonies);
        stub(mockShipRepository.getShipByShipId(argShipId)).toReturn(ship);
        stub(mockPlanetRepository.getPlanetByName(argDestinationPlanetName)).toReturn(planets[1]);

        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);
        GameService gameServiceWithMockedDependencies = new GameService(mockPlanetRepository, mockColonyRepository, mockShipRepository, mockPlayerRepository, mockGameRepository, new MoveShipHandler(mockColonyRepository, mockPlanetRepository, mockGameSynchronizer), new ViewModelConverter(), mockGameSynchronizer);
        int oldEnemyColonyAmount = opponent.getColonies().size();
        int oldCommandPoints = player.getCommandPoints();
        int oldColonyStrenght = opponent.getColonies().get(0).getStrength();
        //endregion
        //region Act
        gameServiceWithMockedDependencies.moveShip(argShipId, argDestinationPlanetName);
        //endregion
        //region Assert
        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);

        Mockito.verify(mockGameSynchronizer, VerificationModeFactory.times(1)).updateGame(gameArgumentCaptor.capture());
        Game resultGame = gameArgumentCaptor.getValue();
        Player resultPlayer = resultGame.getPlayers().get(0);

        Player resultOpponent = resultGame.getPlayers().get(1);

        assertEquals("Ship should be gone", 0, resultPlayer.getShips().size());
        assertEquals("Ship should be gone", 0, resultOpponent.getShips().size());

        assertEquals("The player should have lost 1 commandPoint", oldCommandPoints - 1, resultPlayer.getCommandPoints());
        List<Colony> resultOpponentColonies = resultOpponent.getColonies();
        assertEquals("the opponent shouldn't have lost any colonies", oldEnemyColonyAmount, resultOpponentColonies.size());
        Colony resultOpponentColony = resultOpponentColonies.get(0);
        assertEquals("the opponent's colony has lost some strenght", oldColonyStrenght - 1, resultOpponentColony.getStrength());
        //endregion
    }

    //region private methods
    private List<Colony> createAlliedAndEnemyColony(int strength, Planet[] planets, Player player, Player opponent) {
        Colony colonyA = new Colony();
        colonyA.setPlanet(planets[0]);
        colonyA.setPlayer(player);
        Colony opponentColony = new Colony();
        opponentColony.setStrength(strength);
        opponentColony.setPlanet(planets[1]);
        opponentColony.setPlayer(opponent);


        List<Colony> gameColonies = new ArrayList<Colony>();
        gameColonies.addAll(player.getColonies());
        gameColonies.addAll(opponent.getColonies());
        return gameColonies;
    }

    private Player createActingPlayer(Game game) {
        Player player = new Player();

        player.setTurnEnded(false);
        player.setPlayerId(1);
        player.setCommandPoints(10);
        player.setGame(game);
        return player;
    }


    private Player createOpponent(Game game) {
        Player opponent = new Player();
        opponent.setTurnEnded(false);
        opponent.setPlayerId(2);
        opponent.setGame(game);
        return opponent;
    }

    private Planet[] createSimpleMapWith2Planets() {
        Planet planetA = new Planet();
        planetA.setName("a");
        planetA.setPlanetId(1);
        Planet planetB = new Planet();
        planetB.setName("b");
        planetB.setPlanetId(2);

        planetA.addConnection(new PlanetConnection(planetA, planetB));
        planetB.addConnection(new PlanetConnection(planetB, planetA));

        Planet[] planets = new Planet[2];
        planets[0] = planetA;
        planets[1] = planetB;
        return planets;
    }

    private Ship createShip(int strength, Planet planet, Player player) {
        Ship ship = new Ship();

        ship.setStrength(strength);
        ship.setPlanet(planet);
        ship.setPlayer(player);
        return ship;
    }
    //endregion


}
