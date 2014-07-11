package be.kdg.spacecrack.model.game;

import java.util.ArrayList;
import java.util.List;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
public class SpaceCrackMap {
    private List<Planet> planets;

    public SpaceCrackMap(List<Planet> planets ) {
        this.planets = planets;
    }

    public List<Planet> getPlanets() {
        return planets;
    }

    public void setPlanets(ArrayList<Planet> planets) {
        this.planets = planets;
    }
}
