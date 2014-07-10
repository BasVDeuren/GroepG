package be.kdg.spacecrack.unittests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.model.Profile;
import be.kdg.spacecrack.model.Ship;
import be.kdg.spacecrack.model.User;
import be.kdg.spacecrack.repositories.*;
import be.kdg.spacecrack.services.GameService;
import be.kdg.spacecrack.services.GameSynchronizer;
import be.kdg.spacecrack.services.IGameSynchronizer;
import be.kdg.spacecrack.utilities.IFirebaseUtil;
import be.kdg.spacecrack.utilities.ViewModelConverter;
import be.kdg.spacecrack.viewmodels.GameViewModel;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/application-context.xml"})
public class ReplayTests {

    @Autowired
    private ApplicationContext applicationContext;

    private User user;
    @Autowired
    private IPlayerRepository playerRepository;
    private Profile opponentProfile;
    private PlatformTransactionManager transactionManager;
    @Autowired
    private IGameRepository gameRepository;
    private GameService gameService;
    @Autowired
    private IColonyRepository colonyRepository;
    @Autowired
    private IPlanetRepository planetRepository;
    @Autowired
    private IShipRepository shipRepository;
    @Autowired
    private PlanetConnectionRepository planetConnectionRepository;
    @Autowired
    private IProfileRepository profileRepository;
    @Autowired
    private GameRevisionRepository gameRevisionRepository;

    @Before
    public void setup() {
        transactionManager = (PlatformTransactionManager) applicationContext.getBean("transactionManager");


        IFirebaseUtil mockedFirebaseUtil = mock(IFirebaseUtil.class);

        IGameSynchronizer gameSynchronizer = new GameSynchronizer(new ViewModelConverter(), mockedFirebaseUtil, gameRepository);
        gameService = new GameService(planetRepository, colonyRepository, shipRepository, playerRepository, gameRepository, new ViewModelConverter(), gameSynchronizer, gameRevisionRepository);
    }


    @Test
    @Transactional
    public void getRevisionNumbers_gameWith3Moves_ListOfRevisions() throws Exception {
        //region Arrange
        //region Make map and setup game
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        createMap();
        createProfiles();

        int gameId = gameService.createGame(user.getProfile(), "SpaceCrackName", opponentProfile);


        Game game = gameService.getGameByGameId(gameId);
        Ship ship = game.getPlayers().get(0).getShips().get(0);
        transactionManager.commit(status);
        //endregion
        doMoves(transactionManager, ship);
        //endregion
        TransactionStatus status5 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        //region Act
        List<Integer> revisionNumbers = gameService.getRevisionNumbers(game.getId());
        //endregion
        transactionManager.commit(status5);
        //region Assert
        assertEquals("Game should have 4 revisions", 4, revisionNumbers.size());
        //endregion

    }

    @Test
    @Transactional
    public void getGameRevisionByNumber() throws Exception {
        //region Arrange
        //region Make map and setup game
        TransactionStatus status = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        createMap();
        createProfiles();

        int gameId = gameService.createGame(user.getProfile(), "SpaceCrackName2", opponentProfile);

        Game game = gameService.getGameByGameId(gameId);
        Ship ship = game.getPlayers().get(0).getShips().get(0);
        transactionManager.commit(status);

        doMoves(transactionManager, ship);


        TransactionStatus status2 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        List<Integer> revisionNumbers = gameService.getRevisionNumbers(game.getId());

        transactionManager.commit(status2);
        //endregion

        //region Act
        TransactionStatus status3 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        List<GameViewModel> viewModels = new ArrayList<GameViewModel>();
        for (Number number : revisionNumbers) {
            GameViewModel gameRevision = gameService.getGameRevisionByNumber(gameId, number);
            viewModels.add(gameRevision);
        }
        //endregion
        assertEquals("player should have 1 colony", 1, viewModels.get(0).getPlayer1().getColonies().size());
        assertEquals("player should have 2 colonies", 2, viewModels.get(1).getPlayer1().getColonies().size());
        assertEquals("player should have 3 colonies", 3, viewModels.get(2).getPlayer1().getColonies().size());
        assertEquals("player should have 3 colonies", 3, viewModels.get(3).getPlayer1().getColonies().size());

        transactionManager.commit(status3);
    }

    private void doMoves(PlatformTransactionManager transactionManager, Ship ship) {
        TransactionStatus status2 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        gameService.moveShip(ship.getShipId(), "b");
        transactionManager.commit(status2);

        TransactionStatus status3 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        gameService.moveShip(ship.getShipId(), "c");
        transactionManager.commit(status3);

        TransactionStatus status4 = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        gameService.moveShip(ship.getShipId(), "b");
        transactionManager.commit(status4);
    }


    private void createProfiles() throws Exception {


        user = new User();
        Profile profile = new Profile();
        opponentProfile = new Profile();
        User opponentUser = new User();
        opponentUser.setProfile(opponentProfile);
        user.setProfile(profile);
        profileRepository.save(profile);
        profileRepository.save(opponentProfile);

    }

    private void createMap() {
        MapFactory mapFactory = new MapFactory(planetRepository, planetConnectionRepository);
        mapFactory.createPlanets();
    }
}
