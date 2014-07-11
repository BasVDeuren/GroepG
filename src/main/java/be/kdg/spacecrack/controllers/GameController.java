package be.kdg.spacecrack.controllers;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.model.game.Game;
import be.kdg.spacecrack.model.game.Player;
import be.kdg.spacecrack.model.authentication.Profile;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.repositories.IMapFactory;
import be.kdg.spacecrack.services.IAuthorizationService;
import be.kdg.spacecrack.services.IGameService;
import be.kdg.spacecrack.services.IProfileService;
import be.kdg.spacecrack.utilities.FirebaseUtil;
import be.kdg.spacecrack.utilities.IFirebaseUtil;
import be.kdg.spacecrack.utilities.IViewModelConverter;
import be.kdg.spacecrack.viewmodels.GameActivePlayerWrapper;
import be.kdg.spacecrack.viewmodels.GameParameters;
import be.kdg.spacecrack.viewmodels.GameViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/auth/game")
public class GameController {


    public static final String GAMESUFFIX = "/gameName/";
    @Autowired
    private IAuthorizationService authorizationService;

    @Autowired
    private IGameService gameService;

    @Autowired
    private IProfileService profileService;

    @Autowired
    private IViewModelConverter viewModelConverter;

    @Autowired
    private IFirebaseUtil firebaseUtil;

    @Autowired
    private IMapFactory mapFactory;

    public GameController() {}

    public GameController(IAuthorizationService authorizationService, IGameService gameService, IProfileService profileService, IViewModelConverter viewModelConverter, IFirebaseUtil firebaseUtil) {
        this.viewModelConverter = viewModelConverter;
        this.authorizationService = authorizationService;
        this.firebaseUtil = firebaseUtil;
        this.gameService = gameService;
        this.profileService = profileService;
    }

    @PostConstruct
    private void createMap() {
        firebaseUtil.clearSpaceCrack();
        firebaseUtil.clearChat();
        mapFactory.createPlanets();
    }

    @RequestMapping(method = RequestMethod.POST)
    @ResponseBody
    public String createGame(@CookieValue("accessToken") String accessTokenValue, @RequestBody @Valid GameParameters gameData) {
        User user = authorizationService.getUserByAccessTokenValue(accessTokenValue);
        int profileId;

        profileId = gameData.getOpponentProfileId();

        Profile opponentProfile = profileService.getProfileByProfileId(profileId);
        int gameId = gameService.createGame(user.getProfile(), gameData.getGameName(), opponentProfile);
        return gameId + "";
    }

    @RequestMapping(value = "/specificGame/{gameId}", method = RequestMethod.GET)
    @ResponseBody
    public GameActivePlayerWrapper getGameByGameId(@CookieValue("accessToken") String accessTokenValue, @PathVariable String gameId) {
        Game game;
        try {
            game = gameService.getGameByGameId(Integer.parseInt(gameId));
        } catch (NumberFormatException nex) {
            throw new SpaceCrackNotAcceptableException("Invalid number format for pathvariable gameId");
        }
        User user = authorizationService.getUserByAccessTokenValue(accessTokenValue);
        Player player = gameService.getActivePlayer(user, game);
        GameViewModel gameViewModel = viewModelConverter.convertGameToViewModel(game);
        GameActivePlayerWrapper gameActivePlayerWrapper = new GameActivePlayerWrapper(gameViewModel, player.getPlayerId(), FirebaseUtil.FIREBASEURLBASE + GAMESUFFIX + game.getId());
        firebaseUtil.setValue(GAMESUFFIX + game.getId(), gameViewModel);
        return gameActivePlayerWrapper;
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public List<GameViewModel> getGamesByAccessToken(@CookieValue("accessToken") String accessTokenValue) {
        User user = authorizationService.getUserByAccessTokenValue(accessTokenValue);
        List<Game> games = gameService.getGames(user);
        List<GameViewModel> gameViewModels = new ArrayList<>();
        for (Game g : games) {
            GameViewModel gameViewModel;
            gameViewModel = viewModelConverter.convertGameToViewModel(g);
            gameViewModels.add(gameViewModel);
        }
        return gameViewModels;
    }

    @RequestMapping(value="/invite/{gameId}",method = RequestMethod.DELETE)
    @ResponseBody
    public void deleteGame(@PathVariable String gameId) throws Exception {
        gameService.deleteGame(Integer.parseInt(gameId));
    }

    @RequestMapping(value = "/invite/{gameId}", method = RequestMethod.POST)
    @ResponseBody
    public void acceptGameInvite(@PathVariable String gameId) {
        gameService.acceptGameInvite(Integer.parseInt(gameId));
    }
}
