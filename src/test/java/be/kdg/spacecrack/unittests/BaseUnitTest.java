package be.kdg.spacecrack.unittests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.repositories.IPlanetRepository;
import be.kdg.spacecrack.repositories.MapFactory;
import be.kdg.spacecrack.repositories.PlanetConnectionRepository;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"file:src/test/resources/application-context.xml"})
@Transactional
public abstract class BaseUnitTest {
    @Autowired
    protected IPlanetRepository planetRepository;
    @Autowired
    private PlanetConnectionRepository planetConnectionRepository;
    @Before
    public void createMap()
    {
        MapFactory mapFactory = new MapFactory(planetRepository, planetConnectionRepository );
        mapFactory.createPlanets();
    }
}
