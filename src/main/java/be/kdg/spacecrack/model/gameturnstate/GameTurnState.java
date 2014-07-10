package be.kdg.spacecrack.model.gameturnstate;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.model.Player;
import lombok.NonNull;

public enum GameTurnState implements IGameTurnState{
    NOTURNSENDED {
        @Override
        public GameTurnState notifyTurnEnded(@NonNull Game game) {
            return GameTurnState.ONETURNENDED;

        }
    }, ONETURNENDED {
        @Override
        public GameTurnState notifyTurnEnded(@NonNull Game game) {
            for(Player p :game.getPlayers())
            {
                p.startNewTurn();
            }
            return GameTurnState.NOTURNSENDED;
        }
    }





}
