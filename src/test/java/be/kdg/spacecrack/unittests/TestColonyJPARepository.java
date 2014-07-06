package be.kdg.spacecrack.unittests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Colony;
import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.repositories.IColonyRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

import javax.transaction.Transactional;
import java.util.List;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/application-context.xml"})
//@TestExecutionListeners({ DependencyInjectionTestExecutionListener.class, DirtiesContextTestExecutionListener.class,
//        TransactionalTestExecutionListener.class, DbUnitTestExecutionListener.class })
//@DatabaseSetup("/colonyRepositoryTest-data.xml")
@Transactional
@TransactionConfiguration(defaultRollback = true)
public class TestColonyJPARepository {
    @Autowired
    public IColonyRepository colonyRepository;

    @Test
    public void testGetColoniesByGame()
    {

        Game game = new Game();
        game.setId(1);
        List<Colony> coloniesByGame = colonyRepository.findColoniesByGame(game);
        assertEquals(coloniesByGame.size(), 0);
    }

}
