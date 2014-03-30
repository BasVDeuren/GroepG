package be.kdg.spacecrack.controllers;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.commands.Action;
import be.kdg.spacecrack.commands.BuildShipAction;
import be.kdg.spacecrack.commands.EndTurnAction;
import be.kdg.spacecrack.commands.MoveShipAction;
import be.kdg.spacecrack.services.IGameService;
import be.kdg.spacecrack.utilities.IFirebaseUtil;
import be.kdg.spacecrack.utilities.IViewModelConverter;
import be.kdg.spacecrack.viewmodels.ActionViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.validation.Valid;
import java.io.IOException;


@Controller
@RequestMapping("/auth/action")
public class ActionController{
    public static final String MOVESHIP = "MOVESHIP";
    public static final String ENDTURN = "ENDTURN";
    public static final String BUILDSHIP = "BUILDSHIP";

    @Autowired
    private IGameService gameService;

    @Autowired
    private IViewModelConverter viewModelConverter;

    @Autowired
    private IFirebaseUtil firebaseUtil;

    public ActionController() {}

    public ActionController(IGameService gameService, IViewModelConverter viewModelConverter, IFirebaseUtil firebaseUtil) {
        this.gameService = gameService;
        this.viewModelConverter = viewModelConverter;
        this.firebaseUtil = firebaseUtil;
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public void executeAction(@RequestBody @Valid ActionViewModel actionViewModel) throws IOException {
        if (actionViewModel.getPlayerId() == null || actionViewModel.getPlayerId() < 1) {
            throw new SpaceCrackNotAcceptableException("An action must contain a valid playerId");
        }

        if (actionViewModel.getGameId() == null || actionViewModel.getGameId() < 1) {
            throw new SpaceCrackNotAcceptableException("An action must contain a valid gameId");
        }

        String actionType = actionViewModel.getActionType();
        Action action;
        if (actionType.equals(MOVESHIP)) {
            action = new MoveShipAction(gameService, actionViewModel.getPlayerId(), actionViewModel.getShip(), actionViewModel.getDestinationPlanetName());
        } else if (actionType.equals(ENDTURN)) {
            action = new EndTurnAction(gameService, actionViewModel.getPlayerId());
        } else if (actionType.equals(BUILDSHIP)) {
            action = new BuildShipAction(gameService, actionViewModel.getPlayerId(), actionViewModel.getColony());
        } else {
            throw new SpaceCrackNotAcceptableException("Unsupported action type");
        }
        action.execute();
//        Game game = gameService.getGameByGameId(actionViewModel.getGameId());
//        GameViewModel gameViewModel = viewModelConverter.convertGameToViewModel(game);
//
//        firebaseUtil.setValue(GameController.GAMESUFFIX + gameViewModel.getGameId(), gameViewModel);

    }


}
