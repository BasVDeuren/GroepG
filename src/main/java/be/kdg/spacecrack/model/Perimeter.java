package be.kdg.spacecrack.model;

import org.apache.commons.collections.CollectionUtils;

import java.util.List;

/**
 * Created by Janne on 7/03/14.
 */
public class Perimeter {
    List<Planet> outsidePlanets;
    List<Planet> insidePlanets;

    public Perimeter() {}

    public Perimeter(List<Planet> insidePlanets, List<Planet> outsidePlanets) {

        this.insidePlanets = insidePlanets;
        this.outsidePlanets = outsidePlanets;
    }

    public List<Planet> getOutsidePlanets() {
        return outsidePlanets;
    }

    public void setOutsidePlanets(List<Planet> outsidePlanets) {
        this.outsidePlanets = outsidePlanets;
    }

    public List<Planet> getInsidePlanets() {
        return insidePlanets;
    }

    public void setInsidePlanets(List<Planet> insidePlanets) {
        this.insidePlanets = insidePlanets;
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
