package be.kdg.spacecrack.model.game;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */public class Perimeter {
    List<Planet> outsidePlanets;
    List<Planet> insidePlanets;


    public Perimeter(List<Planet> insidePlanets, List<Planet> outsidePlanets) {

        this.insidePlanets = insidePlanets;
        this.outsidePlanets = outsidePlanets;
    }

    public List<Planet> getOutsidePlanets() {
        return outsidePlanets;
    }



    public List<Planet> getInsidePlanets() {
        return insidePlanets;
    }



    @Override
    public boolean equals(Object other) {
        if(!(other instanceof Perimeter)) {
            return false;
        }
        Perimeter otherPerimeter = (Perimeter) other;
        return CollectionUtils.isEqualCollection(outsidePlanets, otherPerimeter.getOutsidePlanets()) && CollectionUtils.isEqualCollection(insidePlanets, otherPerimeter.getInsidePlanets());
    }
}
