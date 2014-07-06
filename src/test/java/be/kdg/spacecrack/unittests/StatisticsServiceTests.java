package be.kdg.spacecrack.unittests;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.*;
import be.kdg.spacecrack.repositories.*;
import be.kdg.spacecrack.services.*;
import be.kdg.spacecrack.services.handlers.MoveShipHandler;
import be.kdg.spacecrack.utilities.ViewModelConverter;
import be.kdg.spacecrack.viewmodels.StatisticsViewModel;
import org.junit.Before;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class StatisticsServiceTests extends BaseUnitTest {
    private IGameService gameService;
    private IStatisticsService statisticsService;

    private IGameRepository mockGameRepository;
    @Autowired
    private IGameRepository gameRepository;
    private User user;
    private Profile opponentProfile;
    private IProfileRepository mockProfileRepository;
    @Autowired
    private IColonyRepository colonyRepository;
    @Autowired
    private IPlayerRepository playerRepository;
    @Autowired
    private IShipRepository shipRepository;

    @Before
    public void setUp() throws Exception {




        IGameSynchronizer mockGameSynchronizer = mock(IGameSynchronizer.class);
        gameService = new GameService(planetRepository, colonyRepository, shipRepository, playerRepository, gameRepository, new MoveShipHandler(colonyRepository, planetRepository, mockGameSynchronizer, shipRepository), new ViewModelConverter(), mockGameSynchronizer, null);

        mockGameRepository = mock(IGameRepository.class);
        mockProfileRepository = mock(IProfileRepository.class);
        statisticsService = new StatisticsService(mockGameRepository, mockProfileRepository);

        user = new User();
        Profile profile = new Profile();
        opponentProfile = new Profile();
        User opponentUser = new User();
        opponentUser.setProfile(opponentProfile);
        user.setProfile(profile);
    }

    @Transactional
    @Test
    public void getStatistics_ProfilePlayed0Games_allStatistics0() throws Exception {
        Profile profile = new Profile();

        stub(mockGameRepository.getGamesByProfile(profile)).toReturn(new ArrayList<Game>());

        stub(mockProfileRepository.findOne(1)).toReturn(profile);
        StatisticsViewModel statisticsViewModel = statisticsService.getStatistics(1);

        verify(mockGameRepository, VerificationModeFactory.times(1)).getGamesByProfile(profile);

        assertEquals("Statistics should contain 0 game", 0, statisticsViewModel.getAmountOfGames());
        assertTrue("Statistics winratio should be 0% ", statisticsViewModel.getWinRatio() == 0.0);
        assertTrue("Statistics average amount of colonies per win should be 0", statisticsViewModel.getAverageAmountOfColoniesPerWin() == 0);
        assertTrue("Statistics average amount of ships per win should be 0", statisticsViewModel.getAverageAmountOfShipsPerWin() == 0);
    }

    @Transactional
    @Test
    public void getStatistics_ProfilePlayed1GameWon1_Game100PercentWinRatio() throws Exception {
        Game game = createGame();
        game.setLoserPlayerId(game.getPlayers().get(1).getPlayerId());
        List<Game> games = new ArrayList<>();
        games.add(game);
        Player player = game.getPlayers().get(0);
        Profile profile = player.getProfile();
        int profileId = 1;
        stub(mockGameRepository.getGamesByProfile(profile)).toReturn(games);
        stub(mockProfileRepository.findOne(profileId)).toReturn(profile);
        StatisticsViewModel statisticsViewModel = statisticsService.getStatistics(profileId);
        verify(mockGameRepository, VerificationModeFactory.times(1)).getGamesByProfile(profile);

        assertEquals("Statistics should contain 1 game", 1, statisticsViewModel.getAmountOfGames());
        assertTrue("Statistics winratio should be 100% ", statisticsViewModel.getWinRatio() == 1.0);
        assertTrue("Statistics average amount of colonies per win should be 1", statisticsViewModel.getAverageAmountOfColoniesPerWin() == 1);
        assertTrue("Statistics average amount of ships per win should be 1", statisticsViewModel.getAverageAmountOfShipsPerWin() == 1);
    }

    @Transactional
    @Test
    public void getStatistics_ProfilePlayed3GamesWon2withmanycoloniesandships_Game66PercentWinratio() throws Exception {
        Profile profile = new Profile();
        int profileId = 1;
        profile.setProfileId(profileId);
        Game game1 = getOverGameWithColoniesAndShips(1, 20, 5, 1, profile, opponentProfile);
        Game game2 = getOverGameWithColoniesAndShips(2, 100, 3, 1, profile, opponentProfile);
        Game game3 = getOverGameWithColoniesAndShips(3, 0, 0, 0, profile, opponentProfile);

        List<Game> games = new ArrayList<Game>();
        games.add(game1);
        games.add(game2);
        games.add(game3);

        stub(mockGameRepository.getGamesByProfile(profile)).toReturn(games);
        stub(mockProfileRepository.findOne(profileId)).toReturn(profile);

        StatisticsViewModel statisticsViewModel = statisticsService.getStatistics(profileId);

        verify(mockGameRepository, VerificationModeFactory.times(1)).getGamesByProfile(profile);

        assertEquals("Statistics should contain 3 games", 3, statisticsViewModel.getAmountOfGames());
        assertTrue("Statistics winratio should be around .66 ", statisticsViewModel.getWinRatio() == (2 / 3));
        assertTrue("Statistics average amount of colonies per win should be 60", statisticsViewModel.getAverageAmountOfColoniesPerWin() == 60);
        assertTrue("Statistics average amount of ships per win should be 4", statisticsViewModel.getAverageAmountOfShipsPerWin() == 4);
    }

    private Game getOverGameWithColoniesAndShips(int gameId, int amountOfColonies, int amountOfShips, int lostPlayerIndex, Profile activePlayerProfile, Profile opponentProfile) {
        Game game = new Game();
        game.setId(gameId);
        game.setName("SpaceCrackGame");
        Player player1 = new Player();
        player1.setPlayerId(1);
        player1.setProfile(activePlayerProfile);
        game.addPlayer(player1);
        Player opponentPlayer = new Player();
        opponentPlayer.setProfile(opponentProfile);
        opponentPlayer.setPlayerId(2);
        game.addPlayer(opponentPlayer);

        for (int i = 0; i < amountOfShips; i++) {
            player1.addShip(new Ship());
        }

        for (int i = 0; i < amountOfColonies; i++) {
            Colony colony = new Colony();
            Planet planet = null;
            Game_Planet game_planet = new Game_Planet(planet);
            planet = new Planet();
            colony.setGame_planet(game_planet);
            player1.addColony(colony);
        }

        game.setLoserPlayerId(game.getPlayers().get(lostPlayerIndex).getPlayerId());
        return game;
    }

    private Game createGame() {
        int gameId = gameService.createGame(user.getProfile(), "SpaceCrackName8", opponentProfile);
        Game game = gameService.getGameByGameId(gameId);

        return game;
    }
}
