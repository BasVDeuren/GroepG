package be.kdg.spacecrack.model.game.action;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.game.Planet;
import be.kdg.spacecrack.model.game.Ship;
import be.kdg.spacecrack.services.IGameService;

public class MoveShipAction implements Action {
    private Ship ship;
    private Planet destinationPlanet;

    public MoveShipAction(Ship ship, Planet destinationPlanet) {
        this.ship = ship;
        this.destinationPlanet = destinationPlanet;
    }

    @Override
    public int getCommandPointsCost() {
        return IGameService.MOVESHIPCOMMANDPOINTSCOST;
    }

    @Override
    public int getCrackCost() {
        return 0;
    }

    @Override
    public void execute() {
        ship.move(destinationPlanet);
    }
}
