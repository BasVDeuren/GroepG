package be.kdg.spacecrack.services;

import be.kdg.spacecrack.model.Game;

/**
 * Created by Janne on 12/03/14.
 */
public interface IGameSynchronizer {
    void updateGame(Game game);



    void updateGameConcurrent(Game game, Integer actionNumber);
}
