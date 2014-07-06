
package be.kdg.spacecrack.repositories;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.controllers.MapController;
import be.kdg.spacecrack.model.Planet;
import be.kdg.spacecrack.model.PlanetConnection;
import be.kdg.spacecrack.model.SpaceCrackMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

@Service("mapFactory")
@Transactional
public class MapFactory implements IMapFactory {

    @Autowired
    private IPlanetRepository planetRepository;
    @Autowired
    private PlanetConnectionRepository planetConnectionRepository;

    public MapFactory() {
    }

    public MapFactory(IPlanetRepository planetRepository, PlanetConnectionRepository planetConnectionRepository) {
        this.planetRepository = planetRepository;
        this.planetConnectionRepository = planetConnectionRepository;
    }

    private void connectPlanetsByRadius(Planet[] planets, double radius) {


        for (int i = 0; i < planets.length; i++) {
            Planet checkPlanet = planets[i];
            for (int j = 0; j < planets.length; j++) {
                Planet planet = planets[j];
                // Point coords
                int pX = planet.getX();
                int pY = planet.getY();
                // Circle centre coords
                int cX = checkPlanet.getX();
                int cY = checkPlanet.getY();

                double distance = Math.sqrt((pX - cX) * (pX - cX) + (pY - cY) * (pY - cY));
                if (checkPlanet != planet) {
                    if (distance < radius) {
                        PlanetConnection planetConnection = new PlanetConnection(checkPlanet, planet);
                        planetConnectionRepository.save(planetConnection);
                        checkPlanet.addConnection(planetConnection);
                       planetRepository.save(checkPlanet);
                    }
                }
            }
        }

    }

    private void connectPlanets(Planet p1, Planet p2) {
        PlanetConnection planetConnection = new PlanetConnection(p1, p2);
        p1.addConnection(planetConnection);

        planetConnection = new PlanetConnection(p2, p1);
        p2.addConnection(planetConnection);

        planetRepository.save(p1);
        planetRepository.save(p2);
    }


    @Override
    public SpaceCrackMap getSpaceCrackMap() {
        List<Planet> all = planetRepository.findAll();
        Planet[] planets = all.toArray(new Planet[all.size()]);
        return new SpaceCrackMap(planets);
    }

    public void createPlanets() {
        if (planetRepository.getPlanetByName("a") == null) {
            Planet a = new Planet("a", 50, 250);
            Planet a3 = new Planet("a3", 750, 250);

            Planet b = new Planet("b", 100, 205);
            Planet b2 = new Planet("b2", 100, 295);
            Planet b3 = new Planet("b3", MapController.MAP_LENGTH - 100, 205);
            Planet b4 = new Planet("b4", MapController.MAP_LENGTH - 100, MapController.MAP_HEIGHT - 205);

            Planet c = new Planet("c", 175, 155);
            Planet c2 = new Planet("c2", 175, 345);
            Planet c3 = new Planet("c3", MapController.MAP_LENGTH - 175, 155);
            Planet c4 = new Planet("c4", MapController.MAP_LENGTH - 175, MapController.MAP_HEIGHT - 155);

            Planet d = new Planet("d", 165, 240);
            Planet d2 = new Planet("d2", 165, 260);
            Planet d3 = new Planet("d3", MapController.MAP_LENGTH - 165, 240);
            Planet d4 = new Planet("d4", MapController.MAP_LENGTH - 165, MapController.MAP_HEIGHT - 240);

            Planet e = new Planet("e", 165, 90);
            Planet e2 = new Planet("e2", 165, MapController.MAP_HEIGHT - 90);
            Planet e3 = new Planet("e3", MapController.MAP_LENGTH - 165, 90);
            Planet e4 = new Planet("e4", MapController.MAP_LENGTH - 165, MapController.MAP_HEIGHT - 90);

            Planet f = new Planet("f", 200, 40);
            Planet f2 = new Planet("f2", 200, MapController.MAP_HEIGHT - 40);
            Planet f3 = new Planet("f3", MapController.MAP_LENGTH - 200, 40);
            Planet f4 = new Planet("f4", MapController.MAP_LENGTH - 200, MapController.MAP_HEIGHT - 40);

            Planet g = new Planet("g", 205, 130);
            Planet g2 = new Planet("g2", 205, MapController.MAP_HEIGHT - 130);
            Planet g3 = new Planet("g3", MapController.MAP_LENGTH - 205, 130);
            Planet g4 = new Planet("g4", MapController.MAP_LENGTH - 205, MapController.MAP_HEIGHT - 130);

            Planet h = new Planet("h", 250, 75);
            Planet h2 = new Planet("h2", 250, MapController.MAP_HEIGHT - 75);
            Planet h3 = new Planet("h3", MapController.MAP_LENGTH - 250, 75);
            Planet h4 = new Planet("h4", MapController.MAP_LENGTH - 250, MapController.MAP_HEIGHT - 75);

            Planet i = new Planet("i", 235, 170);
            Planet i2 = new Planet("i2", 235, MapController.MAP_HEIGHT - 170);
            Planet i3 = new Planet("i3", MapController.MAP_LENGTH - 235, 170);
            Planet i4 = new Planet("i4", MapController.MAP_LENGTH - 235, MapController.MAP_HEIGHT - 170);

            Planet j = new Planet("j", 240, 235);
            Planet j2 = new Planet("j2", 240, MapController.MAP_HEIGHT - 235);
            Planet j3 = new Planet("j3", MapController.MAP_LENGTH - 240, 235);
            Planet j4 = new Planet("j4", MapController.MAP_LENGTH - 240, MapController.MAP_HEIGHT - 235);

            Planet k = new Planet("k", 300, 15);
            Planet k2 = new Planet("k2", 300, MapController.MAP_HEIGHT - 15);
            Planet k3 = new Planet("k3", MapController.MAP_LENGTH - 300, 15);
            Planet k4 = new Planet("k4", MapController.MAP_LENGTH - 300, MapController.MAP_HEIGHT - 15);

            Planet l = new Planet("l", 325, 70);
            Planet l2 = new Planet("l2", 325, MapController.MAP_HEIGHT - 70);
            Planet l3 = new Planet("l3", MapController.MAP_LENGTH - 325, 70);
            Planet l4 = new Planet("l4", MapController.MAP_LENGTH - 325, MapController.MAP_HEIGHT - 70);

            Planet m = new Planet("m", 325, 155);
            Planet m2 = new Planet("m2", 325, MapController.MAP_HEIGHT - 155);
            Planet m3 = new Planet("m3", MapController.MAP_LENGTH - 325, 155);
            Planet m4 = new Planet("m4", MapController.MAP_LENGTH - 325, MapController.MAP_HEIGHT - 155);

            Planet n = new Planet("n", 310, 195);
            Planet n2 = new Planet("n2", 310, MapController.MAP_HEIGHT - 195);
            Planet n3 = new Planet("n3", MapController.MAP_LENGTH - 310, 195);
            Planet n4 = new Planet("n4", MapController.MAP_LENGTH - 310, MapController.MAP_HEIGHT - 195);

            Planet o = new Planet("o", 365, 130);
            Planet o2 = new Planet("o2", 365, MapController.MAP_HEIGHT - 130);
            Planet o3 = new Planet("o3", MapController.MAP_LENGTH - 365, 130);
            Planet o4 = new Planet("o4", MapController.MAP_LENGTH - 365, MapController.MAP_HEIGHT - 130);

            Planet p = new Planet("p", 340, 220);
            Planet p2 = new Planet("p2", 340, MapController.MAP_HEIGHT - 220);
            Planet p3 = new Planet("p3", MapController.MAP_LENGTH - 340, 220);
            Planet p4 = new Planet("p4", MapController.MAP_LENGTH - 340, MapController.MAP_HEIGHT - 220);

            Planet q = new Planet("q", 380, 200);
            Planet q2 = new Planet("q2", 380, MapController.MAP_HEIGHT - 200);
            Planet q3 = new Planet("q3", MapController.MAP_LENGTH - 380, 200);
            Planet q4 = new Planet("q4", MapController.MAP_LENGTH - 380, MapController.MAP_HEIGHT - 200);

            Planet[] planets = {
                    a, a3,
                    b, b2, b3, b4,
                    c, c2, c3, c4,
                    d, d2, d3, d4,
                    e, e2, e3, e4,
                    f, f2, f3, f4,
                    g, g2, g3, g4,
                    h, h2, h3, h4,
                    i, i2, i3, i4,
                    j, j2, j3, j4,
                    k, k2, k3, k4,
                    l, l2, l3, l4,
                    m, m2, m3, m4,
                    n, n2, n3, n4,
                    o, o2, o3, o4,
                    p, p2, p3, p4,
                    q, q2, q3, q4
            };

            // Add more space between planets
            for (Planet planet : planets) {
                planet.setX((int) (planet.getX() * 2));
                planet.setY((int) (planet.getY() * 2));
            }


            planetRepository.save(Arrays.asList(planets));

            connectPlanetsByRadius(planets, 105 * 2);
            removeCrossingConnections(planets);

            // There is a bug in the algorithm removing a few connections to many, for now we add them manually
            connectPlanets(d, j);
            connectPlanets(d2, j2);
            connectPlanets(d3, j3);
            connectPlanets(d4, j4);

            // We could also add certain connections manually to make the playing field more interesting
            connectPlanets(j, p);
            connectPlanets(j2, p2);
            connectPlanets(j3, p3);
            connectPlanets(j4, p4);
        }
    }

    // Remove intersecting lines
    // Optimalisation might be possible
    /* Current algorithm:
     * 1. Go through all the planets (starting with the first)
     * 2. Map out all connections from that planet
     * 3. Go through all the planets (neighbours) that are directly (first degree) connected to that planet
     * 4. Map out all connections of every neighbour
     * 5. Check all connections of the planet against all connections of every neighbour
     * 6. If line segments intersect: remove connection both intersecting connections
     * 7. Go to the next planet (back to step 1)
     */
    private void removeCrossingConnections(Planet[] planets) {
        List<PlanetConnection> connectionsToRemove = new ArrayList<PlanetConnection>();
        for (int i = 0; i < planets.length; i++) {
            Planet currentPlanet = planets[i];
            Iterator<PlanetConnection> currentPlanetIterator = currentPlanet.getPlanetConnections().iterator();
            while (currentPlanetIterator.hasNext()) {
                PlanetConnection connectionToTest = currentPlanetIterator.next();
                Line2D line1 = new Line2D.Float(connectionToTest.getParentPlanet().getX(), connectionToTest.getParentPlanet().getY(), connectionToTest.getChildPlanet().getX(), connectionToTest.getChildPlanet().getY());
                Iterator<PlanetConnection> currentPlanetConnectionIterator = currentPlanet.getPlanetConnections().iterator();
                while (currentPlanetConnectionIterator.hasNext()) {
                    PlanetConnection currentPlanetConnection = currentPlanetConnectionIterator.next();
                    Planet neighbour;
                    if (currentPlanetConnection.getParentPlanet().getPlanetId() == currentPlanet.getPlanetId()) {
                        neighbour = currentPlanetConnection.getChildPlanet();
                    } else {
                        neighbour = currentPlanetConnection.getParentPlanet();
                    }

                    Iterator<PlanetConnection> neighbourConnectionIterator = neighbour.getPlanetConnections().iterator();
                    while (neighbourConnectionIterator.hasNext()) {
                        PlanetConnection neighbourConnectionToTest = neighbourConnectionIterator.next();
                        Line2D line2 = new Line2D.Float(neighbourConnectionToTest.getParentPlanet().getX(), neighbourConnectionToTest.getParentPlanet().getY(), neighbourConnectionToTest.getChildPlanet().getX(), neighbourConnectionToTest.getChildPlanet().getY());

                        if (line1.intersectsLine(line2) && !line1.getP1().equals(line2.getP1()) && !line1.getP1().equals(line2.getP2()) && !line1.getP2().equals(line2.getP1()) && !line1.getP2().equals(line2.getP2())) {
                            connectionsToRemove.add(connectionToTest);
                            connectionsToRemove.add(neighbourConnectionToTest);
                        }
                    }
                }
            }
        }



        for (PlanetConnection connection : connectionsToRemove) {
            connection.getParentPlanet().removeConnectionToPlanet(connection.getChildPlanet());
            connection.getChildPlanet().removeConnectionToPlanet(connection.getParentPlanet());

            planetRepository.save(connection.getParentPlanet());
            planetRepository.save(connection.getChildPlanet());
        }
    }
}
