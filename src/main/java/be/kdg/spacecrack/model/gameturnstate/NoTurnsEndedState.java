package be.kdg.spacecrack.model.gameturnstate;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;

public class NoTurnsEndedState implements IGameTurnState {

    @Override
    public GameTurnState notifyTurnEnded(Game game) {

        return GameTurnState.ONETURNENDED;
    }
}
