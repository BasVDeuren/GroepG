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
import be.kdg.spacecrack.repositories.IGameRepository;
import be.kdg.spacecrack.repositories.IPlayerRepository;
import be.kdg.spacecrack.repositories.IProfileRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManagerFactory;
import java.util.List;

import static org.junit.Assert.assertEquals;


public class GameRepositoryTests extends BaseUnitTest {

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Autowired
    private IGameRepository gameRepository;


    @Autowired
    PlatformTransactionManager transactionManager;
    @Autowired
    private IPlayerRepository playerRepository;
    @Autowired
    private IProfileRepository profileRepository;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    @Transactional
    public void GetAllGamesByProfile() throws Exception {
        Profile profile1;
        Profile profile2;
        Game expected;


        profile1 = new Profile();
        profile2 = new Profile();
        profileRepository.save(profile1);
        profileRepository.save(profile2);
        Player player1 = new Player(profile1);
        Player player2 = new Player(profile2);
        expected = new Game();
        player1.setGame(expected);
        player2.setGame(expected);
        expected.getPlayers().add(player1);
        expected.getPlayers().add(player2);

        expected = gameRepository.save(expected);

        List<Game> games = gameRepository.getGamesByProfile(profile1);

        Game actualGame = games.get(0);
        assertEquals(expected.getId(), actualGame.getId());
    }

}
