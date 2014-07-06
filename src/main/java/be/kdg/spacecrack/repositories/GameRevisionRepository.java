package be.kdg.spacecrack.repositories;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;

import java.util.List;

public interface GameRevisionRepository  {
    public List<Integer> getRevisionNumbers(int gameId);
    public Game getGameRevision(Number number, int gameId);
}
