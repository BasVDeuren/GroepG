package be.kdg.spacecrack.unittests;

import be.kdg.spacecrack.controllers.MapController;
import be.kdg.spacecrack.model.game.Planet;
import be.kdg.spacecrack.model.game.PlanetConnection;
import be.kdg.spacecrack.repositories.MapFactory;
import be.kdg.spacecrack.repositories.PlanetConnectionRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
public class MapControllerTests extends BaseUnitTest {
    @Autowired
    private PlanetConnectionRepository planetConnectionRepository;
    @Test
    @Transactional
    public void getMap_valid_AllPlanetsConnected() throws Exception {
        MapController mapController = new MapController(new MapFactory(planetRepository, planetConnectionRepository));
        List<Planet> planets = mapController.getMap().getPlanets();
        Planet startPlanet = planets.get(0);

        ArrayList<Planet> connectedPlanets = new ArrayList<>();
        writeConnectedPlanetstoList(startPlanet, connectedPlanets);

        assertEquals("All planets should be connected", planets.size(), connectedPlanets.size());
    }

    private synchronized void writeConnectedPlanetstoList(Planet startPlanet, ArrayList<Planet> out) {
        out.add(startPlanet);
        for(PlanetConnection planetConnection : startPlanet.getPlanetConnections())
        {
            if(planetConnection != null){
                if(!out.contains(planetConnection.getChildPlanet())){
                    writeConnectedPlanetstoList(planetConnection.getChildPlanet(), out);
                }
            }
        }
    }
}
