package be.kdg.spacecrack.services;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.*;
import be.kdg.spacecrack.viewmodels.GameViewModel;

import java.util.List;

public interface IGameService {
    public static final int START_COMMAND_POINTS = 8;
    public static final int COMMANDPOINTS_PER_TURN = START_COMMAND_POINTS;
    public static final int BUILDSHIP_COST = 3;
    public static final int MOVESHIPCOST = 1;
    public static final int CREATECOLONYCOST = 1;
    public static final int NEW_SHIP_STRENGTH = 1;

    int createGame(Profile userProfile, String gameName, Profile opponentProfile);

    void moveShip(Integer shipId, String planetName);

    Planet getShipLocationByShipId(int shipId);

    void endTurn(Integer playerID);

    List<Game> getGames(User user);

    Game getGameByGameId(int gameId);

    Player getActivePlayer(User user, Game game);

    void buildShip(Integer colonyId);

    List<Integer> getRevisionNumbers(int gameId);

    GameViewModel getGameRevisionByNumber(int gameId, Number number);

    void acceptGameInvite(int gameId);

    void deleteGame(int gameId);
}
