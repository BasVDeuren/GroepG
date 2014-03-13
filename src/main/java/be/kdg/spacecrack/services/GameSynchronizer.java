package be.kdg.spacecrack.services;

import be.kdg.spacecrack.controllers.GameController;
import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.repositories.IGameRepository;
import be.kdg.spacecrack.utilities.IFirebaseUtil;
import be.kdg.spacecrack.utilities.IViewModelConverter;
import be.kdg.spacecrack.viewmodels.GameViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by Janne on 12/03/14.
 */
@Component("gameSynchronizer")
public class GameSynchronizer implements IGameSynchronizer {
    @Autowired
    private IViewModelConverter viewModelConverter;

    @Autowired
    private IFirebaseUtil firebaseUtil;

    @Autowired
    private IGameRepository gameRepository;

    GameSynchronizer() {}

    public GameSynchronizer(IViewModelConverter viewModelConverter, IFirebaseUtil firebaseUtil, IGameRepository gameRepository) {
        this.viewModelConverter = viewModelConverter;
        this.firebaseUtil = firebaseUtil;
        this.gameRepository = gameRepository;
    }

    @Override
    public void updateGame(Game game) {
        GameViewModel gameViewModel = viewModelConverter.convertGameToViewModel(game);
        firebaseUtil.setValue(GameController.GAME_SUFFIX + gameViewModel.getName(), gameViewModel);
        gameRepository.updateGame(game);
    }
}
