package be.kdg.spacecrack.model.game.action;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.game.Colony;
import be.kdg.spacecrack.services.IGameService;

public class BuildShipAction implements Action {
    private Colony colony;

    public BuildShipAction(Colony colony) {
        this.colony = colony;
    }

    @Override
    public int getCommandPointsCost() {
        return IGameService.BUILDSHIP_COST;
    }

    @Override
    public int getCrackCost() {
        return IGameService.BUILDSHIP_CRACK_COST;
    }

    @Override
    public void execute() {
        colony.buildShip();
    }
}
