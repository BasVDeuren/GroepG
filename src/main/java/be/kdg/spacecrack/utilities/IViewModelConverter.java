package be.kdg.spacecrack.utilities;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.game.Colony;
import be.kdg.spacecrack.model.game.Game;
import be.kdg.spacecrack.model.game.Player;
import be.kdg.spacecrack.model.game.Ship;
import be.kdg.spacecrack.viewmodels.ColonyViewModel;
import be.kdg.spacecrack.viewmodels.GameViewModel;
import be.kdg.spacecrack.viewmodels.PlayerViewModel;
import be.kdg.spacecrack.viewmodels.ShipViewModel;

public interface IViewModelConverter {
    ColonyViewModel convertColonyToViewModel(Colony colony);

    GameViewModel convertGameToViewModel(Game game);

    PlayerViewModel convertPlayerToViewModel(Player player);

    ShipViewModel convertShipToViewModel(Ship ship);

    GameViewModel convertGameToReplayViewModel(Game gameRevision);
}
