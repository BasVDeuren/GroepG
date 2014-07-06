package be.kdg.spacecrack.unittests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.repositories.IGameRepository;
import be.kdg.spacecrack.services.GameSynchronizer;
import be.kdg.spacecrack.utilities.IFirebaseUtil;
import be.kdg.spacecrack.utilities.IViewModelConverter;
import be.kdg.spacecrack.viewmodels.GameViewModel;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.verification.VerificationModeFactory;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class GameSynchronizerTest {
    @Test
    public void updateGameConcurrent_HappyPath_GameUpdatedAndSentToFirebase() throws Exception {
        IFirebaseUtil mockFirebaseUtil = mock(IFirebaseUtil.class);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IViewModelConverter mockViewModelConverter = mock(IViewModelConverter.class);
        GameSynchronizer gameSynchronizer = new GameSynchronizer(mockViewModelConverter, mockFirebaseUtil, mockGameRepository);
        Game game = new Game();
        String updatedGameName = "updatedGame";
        game.setName(updatedGameName);

        Game dbGame= new Game();
        dbGame.setName("dbGame");

        Integer oldActionNumber = 1;
        stub(mockGameRepository.getGameOptimisticConcurrent(game.getId(), oldActionNumber)).toReturn(game);
        stub(mockGameRepository.findOne(any(Integer.class))).toReturn(dbGame);
        stub(mockViewModelConverter.convertGameToViewModel(any(Game.class))).toReturn(new GameViewModel());
        //Act
        gameSynchronizer.updateGameConcurrent(game, oldActionNumber);
        //Assert
        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        verify(mockViewModelConverter, VerificationModeFactory.times(1)).convertGameToViewModel(gameArgumentCaptor.capture());
        Game value = gameArgumentCaptor.getValue();
        assertEquals(value.getName(), updatedGameName);
    }

    @Test
    public void updateGameConcurrent_gameChangedMeanwhile_gameNotUpdatedButRetrievedFromDb() throws Exception {
        IFirebaseUtil mockFirebaseUtil = mock(IFirebaseUtil.class);
        IGameRepository mockGameRepository = mock(IGameRepository.class);
        IViewModelConverter mockViewModelConverter = mock(IViewModelConverter.class);
        GameSynchronizer gameSynchronizer = new GameSynchronizer(mockViewModelConverter, mockFirebaseUtil, mockGameRepository);

        Game game = new Game();
        String updatedGameName = "updatedGame";
        game.setName(updatedGameName);
        Game dbGame= new Game();
        String dbGameName = "dbGame";
        dbGame.setName(dbGameName);

        Integer oldActionNumber = 1;
        stub(mockGameRepository.getGameOptimisticConcurrent(game.getId(), oldActionNumber)).toReturn(null);
        stub(mockGameRepository.findOne(any(Integer.class))).toReturn(dbGame);
        stub(mockViewModelConverter.convertGameToViewModel(any(Game.class))).toReturn(new GameViewModel());

        //Act

        gameSynchronizer.updateGameConcurrent(game, oldActionNumber);

        //Assert
        ArgumentCaptor<Game> gameArgumentCaptor = ArgumentCaptor.forClass(Game.class);
        verify(mockViewModelConverter, VerificationModeFactory.times(1)).convertGameToViewModel(gameArgumentCaptor.capture());
        Game value = gameArgumentCaptor.getValue();
        assertEquals(value.getName(),  dbGameName);
    }
}
