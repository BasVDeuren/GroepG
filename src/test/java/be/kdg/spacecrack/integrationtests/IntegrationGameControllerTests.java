package be.kdg.spacecrack.integrationtests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.controllers.ActionController;
import be.kdg.spacecrack.model.Profile;
import be.kdg.spacecrack.services.GameService;
import be.kdg.spacecrack.services.IGameService;
import be.kdg.spacecrack.utilities.IFirebaseUtil;
import be.kdg.spacecrack.utilities.IViewModelConverter;
import be.kdg.spacecrack.viewmodels.*;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import javax.servlet.http.Cookie;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class IntegrationGameControllerTests extends BaseFilteredIntegrationTests {

    @Test
    public void CreateGame_AuthorisedUserValidName_GameWithShipsAndColonies() throws Exception {
        String accessTokenValue = loginAndRetrieveAccessToken();

        Profile opponentProfile = createOpponent();

        GameParameters gameParameters = new GameParameters("SpacecrackGame1", opponentProfile.getProfileId());
        String gameParametersJson = objectMapper.writeValueAsString(gameParameters);

        String gameIdJson = mockMvc.perform(post("/auth/game")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(gameParametersJson)
                .cookie(new Cookie("accessToken", accessTokenValue))).andReturn().getResponse().getContentAsString();

        int gameId = objectMapper.readValue(gameIdJson, Integer.class);

        mockMvc.perform(get("/auth/game/specificGame/" + gameId)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessTokenValue)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.game.player1.colonies[0].planetName", CoreMatchers.is("a")))
                .andExpect(jsonPath("$.game.player1.colonies[0].strength", CoreMatchers.is(GameService.NEW_COLONY_STRENGHT)))
                .andExpect(jsonPath("$.game.player1.ships[0].planetName", CoreMatchers.is("a")))
                .andExpect(jsonPath("$.game.player1.ships[0].shipId", CoreMatchers.notNullValue()))
                .andExpect(jsonPath("$.game.player1.ships[0].shipId", CoreMatchers.not(0)))
                .andExpect(jsonPath("$.game.player1.ships[0].strength", CoreMatchers.is(GameService.NEW_SHIP_STRENGTH)))
                .andExpect(jsonPath("$.game.player1.crack", CoreMatchers.is(GameService.PLAYER_START_CRACK)))
                .andExpect(jsonPath("$.game.player2.colonies[0].planetName", CoreMatchers.is("a3")))
                .andExpect(jsonPath("$.game.player2.colonies[0].strength", CoreMatchers.is(GameService.NEW_COLONY_STRENGHT)))
                .andExpect(jsonPath("$.game.player2.ships[0].planetName", CoreMatchers.is("a3")))
                .andExpect(jsonPath("$.game.player2.ships[0].shipId", CoreMatchers.notNullValue()))
                .andExpect(jsonPath("$.game.player2.ships[0].shipId", CoreMatchers.not(0)))
                .andExpect(jsonPath("$.game.player2.ships[0].strength", CoreMatchers.is(GameService.NEW_SHIP_STRENGTH)))
                .andExpect(jsonPath("$.game.player2.crack", CoreMatchers.is(GameService.PLAYER_START_CRACK)));
    }

    @Test
    public void CreateGame_InvalidName_SpaceCrackNotAcceptableException() throws Exception {
        String accessTokenValue = loginAndRetrieveAccessToken();

        Profile opponentProfile = createOpponent();

        GameParameters gameParameters = new GameParameters(".$[]#/", opponentProfile.getProfileId());
        String gameParametersJson = objectMapper.writeValueAsString(gameParameters);

        MockMvc customMockMvc = MockMvcBuilders.standaloneSetup(baseGameController).setHandlerExceptionResolvers(getGlobalExceptionHandler()).build();

        customMockMvc.perform(post("/auth/game")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(gameParametersJson)
                .cookie(new Cookie("accessToken", accessTokenValue))).andExpect(status().isNotAcceptable());
    }

    @Test
    public void MoveShip_validPlanet_Ok() throws Exception {
        String accessToken = loginAndRetrieveAccessToken();
        GameActivePlayerWrapper gameActivePlayerWrapper = createAGame(accessToken);
        GameViewModel game = gameActivePlayerWrapper.getGame();

        ShipViewModel ship = game.getPlayer1().getShips().get(0);
        String destinationPlanet = "b";

        ActionViewModel moveShipActionViewModel = new ActionViewModel("MOVESHIP", ship, destinationPlanet, null, game.getPlayer1().getPlayerId(), game.getGameId());

        String moveShipActionJson = objectMapper.writeValueAsString(moveShipActionViewModel);

        mockMvc.perform(post("/auth/action")
                .contentType(MediaType.APPLICATION_JSON)
                .content(moveShipActionJson)
                .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk());
    }


    @Test
    public void getAllGamesByProfile() throws Exception {
        String accessToken = loginAndRetrieveAccessToken();

        Profile opponentProfile = createOpponent();

        GameParameters gameParameters = new GameParameters("SpacecrackGame1", opponentProfile.getProfileId());
        String gameParametersJson = objectMapper.writeValueAsString(gameParameters);

        mockMvc.perform(post("/auth/game")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(gameParametersJson)
                .cookie(new Cookie("accessToken", accessToken)));


        mockMvc.perform(get("/auth/game")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].gameId", CoreMatchers.notNullValue()))
                .andExpect(jsonPath("$.[0].name", CoreMatchers.notNullValue()));
    }

    @Test
    public void getGameByGameId() throws Exception {
        String accessToken = loginAndRetrieveAccessToken();
        GameActivePlayerWrapper gameActivePlayerWrapper = createAGame(accessToken);
        GameViewModel expected = gameActivePlayerWrapper.getGame();


        mockMvc.perform(get("/auth/game/specificGame/" + expected.getGameId())
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.game.gameId", CoreMatchers.notNullValue()));
    }

    @Test
    public void endPlayerTurn() throws Exception {
        String accessToken = loginAndRetrieveAccessToken();
        GameActivePlayerWrapper gameActivePlayerWrapper = createAGame(accessToken);
        GameViewModel game = gameActivePlayerWrapper.getGame();

        String actionViewModelJSon = objectMapper.writeValueAsString(new ActionViewModel("ENDTURN", null, "", null, game.getPlayer1().getPlayerId(), game.getGameId()));
        mockMvc.perform(post("/auth/action")

                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(actionViewModelJSon)
                .cookie(new Cookie("accessToken", accessToken))
        ).andExpect(status().isOk());
    }

    @Test
    public void postActionBuildShip_validAction_fireBaseCalled() throws Exception {
        String accessToken = loginAndRetrieveAccessToken();
        GameActivePlayerWrapper gameActivePlayerWrapper = createAGame(accessToken);
        GameViewModel game = gameActivePlayerWrapper.getGame();
        ColonyViewModel colonyViewModel = game.getPlayer1().getColonies().get(0);

        IFirebaseUtil mockFireBaseUtil = mock(IFirebaseUtil.class);
        IViewModelConverter mockViewModelConverter = mock(IViewModelConverter.class);
        IGameService mockGameService = mock(IGameService.class);
        GameViewModel gameViewModel = new GameViewModel();
        gameViewModel.setName("mockGame");
        stub(mockViewModelConverter.convertGameToViewModel(null)).toReturn(gameViewModel);

        ActionController actionController = new ActionController(mockGameService, mockViewModelConverter, mockFireBaseUtil);

        MockMvc standAloneMockMVC = MockMvcBuilders.standaloneSetup(actionController).build();
        String actionViewModelJSon = objectMapper.writeValueAsString(new ActionViewModel("BUILDSHIP", null, "", colonyViewModel, game.getPlayer1().getPlayerId(), game.getGameId()));

        standAloneMockMVC.perform(post("/auth/action")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(actionViewModelJSon)).andExpect(status().isOk());
//        verify(mockFireBaseUtil, VerificationModeFactory.times(1)).setValue(any(String.class), any(Object.class));
        verify(mockGameService, VerificationModeFactory.times(1)).buildShip(colonyViewModel.getColonyId());
    }


}
