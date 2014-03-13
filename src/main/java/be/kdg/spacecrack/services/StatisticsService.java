package be.kdg.spacecrack.services;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.model.Player;
import be.kdg.spacecrack.model.Profile;
import be.kdg.spacecrack.repositories.IGameRepository;
import be.kdg.spacecrack.repositories.IProfileRepository;
import be.kdg.spacecrack.viewmodels.StatisticsViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StatisticsService implements IStatisticsService {

    @Autowired
    private IGameRepository gameRepository;

    @Autowired
    private IProfileRepository profileRepository;

    public StatisticsService() {}

    public StatisticsService(IGameRepository mockGameRepository, IProfileRepository mockProfileRepository) {
        gameRepository = mockGameRepository;
        profileRepository = mockProfileRepository;
    }

    @Override
    public StatisticsViewModel getStatistics(int profileId) {
        Profile profile = profileRepository.getProfileByProfileId(profileId);
        List<Game> games = gameRepository.getGamesByProfile(profile);
        int amountOfGames = 0;
        int amountOfWonGames = 0;
        int totalColoniesWhenWon = 0;
        int totalShipsWhenWon = 0;

        for (Game game : games) {
            amountOfGames += 1;
            Player playerInGame = null;
            for (Player player : game.getPlayers()) {
                if (profile.getPlayers().contains(player)) {
                    playerInGame = player;
                }
            }
            if (game.getLoserPlayerId() != playerInGame.getPlayerId() && game.getLoserPlayerId() != 0) {
                amountOfWonGames += 1;
                totalColoniesWhenWon += playerInGame.getColonies().size();
                totalShipsWhenWon += playerInGame.getShips().size();
            }
        }


        StatisticsViewModel statisticsViewModel = new StatisticsViewModel();
        statisticsViewModel.setAmountOfGames(amountOfGames);
        if (amountOfGames != 0) {
            statisticsViewModel.setWinRatio(amountOfWonGames / amountOfGames);
            if (amountOfWonGames != 0) {
                statisticsViewModel.setAverageAmountOfColoniesPerWin(totalColoniesWhenWon / amountOfWonGames);
                statisticsViewModel.setAverageAmountOfShipsPerWin(totalShipsWhenWon / amountOfWonGames);
            } else {
                statisticsViewModel.setAverageAmountOfColoniesPerWin(0);
                statisticsViewModel.setAverageAmountOfShipsPerWin(0);
                statisticsViewModel.setWinRatio(0);
            }
        }
        return statisticsViewModel;
    }
}
