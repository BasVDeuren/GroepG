package be.kdg.spacecrack.repositories;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Ship;

public interface IShipRepository {
    Ship getShipByShipId(int shipId);

    void delete(Ship ship);
}
