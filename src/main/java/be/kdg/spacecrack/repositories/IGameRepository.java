package be.kdg.spacecrack.repositories;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.model.Profile;

import java.util.List;

public interface IGameRepository {

    int createOrUpdateGame(Game game);

    int updateGame(Game game);

    List<Game> getGamesByProfile(Profile profile);

    Game getGameByGameId(int gameId);

    void deleteGame(int gameId);

    Game getGameRevision(Number number, int gameId);

    List<Integer> getRevisionNumbers(int gameId);

    Game getGameForUpdate(Game game);
}
