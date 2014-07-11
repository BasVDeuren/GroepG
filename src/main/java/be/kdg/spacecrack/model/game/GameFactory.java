package be.kdg.spacecrack.model.game;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.authentication.Profile;

public interface GameFactory {
    public Game createGame(SpaceCrackMap map, Profile userProfile, Profile opponentProfile, String gameName);
}
