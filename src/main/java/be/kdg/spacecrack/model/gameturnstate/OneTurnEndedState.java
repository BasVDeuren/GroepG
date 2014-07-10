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

public class OneTurnEndedState implements IGameTurnState {




    public OneTurnEndedState() {

    }

    @Override
    public GameTurnState notifyTurnEnded(@NonNull Game game) {
       for(Player p :game.getPlayers())
       {
           p.startNewTurn();

       }

        return GameTurnState.NOTURNSENDED;
    }
}
