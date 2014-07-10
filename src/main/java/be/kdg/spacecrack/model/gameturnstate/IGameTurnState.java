package be.kdg.spacecrack.model.gameturnstate;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;
import lombok.NonNull;

public interface IGameTurnState {

    GameTurnState notifyTurnEnded(@NonNull Game game);
}
