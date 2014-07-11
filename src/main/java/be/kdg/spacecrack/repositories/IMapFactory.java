package be.kdg.spacecrack.repositories;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.game.SpaceCrackMap;

public interface IMapFactory {

    SpaceCrackMap getSpaceCrackMap();


    void createPlanets();
}
