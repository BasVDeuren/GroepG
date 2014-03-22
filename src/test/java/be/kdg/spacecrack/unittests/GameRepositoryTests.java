package be.kdg.spacecrack.unittests;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.model.Player;
import be.kdg.spacecrack.model.Profile;
import be.kdg.spacecrack.repositories.GameRepository;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate4.HibernateTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.Assert.assertEquals;


public class GameRepositoryTests extends BaseUnitTest {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    SessionFactory sessionFactory;

    private GameRepository gameRepository;


    @Autowired
    HibernateTransactionManager transactionManager;

    @Before
    public void setUp() throws Exception {
        gameRepository =new GameRepository(sessionFactory);

    }

    @Test
    @Transactional
    public void GetAllGamesByProfile() throws Exception {
        Profile profile1;
        Profile profile2;
        Game expected;
        Session session = sessionFactory.getCurrentSession();

        profile1 = new Profile();
        profile2 = new Profile();
        session.saveOrUpdate(profile1);
        session.saveOrUpdate(profile2);
        Player player1 = new Player(profile1);
        Player player2 = new Player(profile2);
        expected = new Game();
        player1.setGame(expected);
        player2.setGame(expected);
        session.saveOrUpdate(player1);
        session.saveOrUpdate(player2);

        expected.getPlayers().add(player1);
        expected.getPlayers().add(player2);
        session.saveOrUpdate(expected);

        GameRepository gameRepository = new GameRepository(sessionFactory);

        List<Game> games = gameRepository.getGamesByProfile(profile1);

        Game actualGame = games.get(0);
        assertEquals(expected.getGameId(), actualGame.getGameId());
    }

    @Transactional
    @Test
    public void getGameByGameId() throws Exception {
        Profile profile1;
        Profile profile2;
        Game expected;
        Session session = sessionFactory.getCurrentSession();

        profile1 = new Profile();
        profile2 = new Profile();
        session.saveOrUpdate(profile1);
        session.saveOrUpdate(profile2);
        Player player1 = new Player(profile1);
        Player player2 = new Player(profile2);
        expected = new Game();
        player1.setGame(expected);
        player2.setGame(expected);
        session.saveOrUpdate(player1);
        session.saveOrUpdate(player2);

        expected.getPlayers().add(player1);
        expected.getPlayers().add(player2);
        session.saveOrUpdate(expected);


        int expectedId = gameRepository.createOrUpdateGame(expected);
        Game actual = gameRepository.getGameByGameId(expectedId);

        assertEquals("GameId from repository should be the same as actual gameId", expectedId, actual.getGameId());
    }

//    @Test
//    @Transactional
//    public void updateGameOptimisticConcurrency() throws Exception {
//
//        TransactionStatus createTransaction = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//
//        Game initialGame = new Game();
//       // int gameId = 20;
//       // initialGame.setGameId(gameId);
//        initialGame.setActionNumber(0);
//        gameRepository.createOrUpdateGame(initialGame);
//        transactionManager.commit(createTransaction);
//        TransactionStatus readTransaction = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//
//        Game updatedGame = gameRepository.getGameByGameId(initialGame.getGameId());
//        transactionManager.commit(readTransaction);
//        TransactionStatus update1Transaction = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//
//        Game concurrentGamev2 = updatedGame;
//        Integer oldActionNumber = updatedGame.getActionNumber();
//        updatedGame.setName("derp");
//        concurrentGamev2.setName("herp");
//
//
//        boolean shouldbetrue = gameRepository.updateGameOptimisticConcurrent(updatedGame, oldActionNumber);
//
//        assertTrue(shouldbetrue);
//        transactionManager.commit(update1Transaction);
//
//        TransactionStatus concurrentUpdateTransaction = transactionManager.getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
//
//        boolean shouldbefalse = gameRepository.updateGameOptimisticConcurrent(concurrentGamev2, oldActionNumber);
//        assertFalse(shouldbefalse);
//        transactionManager.commit(concurrentUpdateTransaction);
//
//    }
}
