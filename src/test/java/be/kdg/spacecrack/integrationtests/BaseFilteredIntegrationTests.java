package be.kdg.spacecrack.integrationtests;

import be.kdg.spacecrack.controllers.*;
import be.kdg.spacecrack.filters.TokenHandlerInterceptor;
import be.kdg.spacecrack.model.authentication.AccessToken;
import be.kdg.spacecrack.model.authentication.Profile;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.model.game.GameFactory;
import be.kdg.spacecrack.repositories.*;
import be.kdg.spacecrack.services.*;

import be.kdg.spacecrack.utilities.IFirebaseUtil;
import be.kdg.spacecrack.utilities.ITokenStringGenerator;
import be.kdg.spacecrack.utilities.TokenStringGenerator;
import be.kdg.spacecrack.utilities.ViewModelConverter;
import be.kdg.spacecrack.validators.BeanValidator;
import be.kdg.spacecrack.viewmodels.GameActivePlayerWrapper;
import be.kdg.spacecrack.viewmodels.GameParameters;
import be.kdg.spacecrack.viewmodels.ProfileWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.test.web.servlet.setup.StandaloneMockMvcBuilder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.method.annotation.ExceptionHandlerMethodResolver;
import org.springframework.web.servlet.mvc.method.annotation.ExceptionHandlerExceptionResolver;
import org.springframework.web.servlet.mvc.method.annotation.ServletInvocableHandlerMethod;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import java.lang.reflect.Method;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@ContextConfiguration(locations = {"file:src/main/webapp/WEB-INF/mvc-dispatcher-servlet.xml", "file:src/test/resources/application-context.xml"})
@WebAppConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
@Transactional
public abstract class BaseFilteredIntegrationTests {
    protected static ObjectMapper objectMapper;
    protected static MockMvc mockMvc;
    protected static GameController baseGameController;
    protected static StandaloneMockMvcBuilder mvcBuilderWithoutGlobalExceptionHandler;

    @Autowired
    protected ServletContext servletContext;
    protected WebApplicationContext ctx;
    @Autowired
    protected IColonyRepository colonyRepository;
    @Autowired
    protected IGameRepository gameRepository;
    @Autowired
    protected IPlanetRepository planetRepository;

    private JavaMailSender mockMailSender;
    @Autowired
    protected IPlayerRepository playerRepository;
    @Autowired
    protected IProfileRepository profileRepository;
    @Autowired
    protected IShipRepository shipRepository;
    @Autowired
    protected ITokenRepository tokenRepository;
    @Autowired
    protected IUserRepository userRepository;
    @Autowired
    protected PlanetConnectionRepository planetConnectionRepository;
    @Autowired
    protected GameRevisionRepository gameRevisionRepository;
    @Autowired
    protected IMapFactory mapFactory;
    @Autowired
    protected GameFactory gameFactory;

    @Before
    public void setupMockMVC() throws Exception {
        ctx = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        if (mvcBuilderWithoutGlobalExceptionHandler == null) {

            IMapFactory mapFactory = new MapFactory(planetRepository, planetConnectionRepository);
            ITokenStringGenerator tokenStringGenerator = new TokenStringGenerator();
            ViewModelConverter viewModelConverter = new ViewModelConverter();
            IFirebaseUtil firebaseUtil = mock(IFirebaseUtil.class);
            GameSynchronizer gameSynchronizer = new GameSynchronizer(viewModelConverter, firebaseUtil, gameRepository);


            IGameService gameService = new GameService(planetRepository, colonyRepository, shipRepository, playerRepository, gameRepository, viewModelConverter, gameSynchronizer, gameRevisionRepository, mapFactory, gameFactory);
            IAuthorizationService authorizationService = new AuthorizationService(tokenRepository, userRepository, tokenStringGenerator);

            mockMailSender = mock(JavaMailSender.class);
            IUserService userService = new UserService(userRepository, profileRepository, mockMailSender);
            IProfileService profileService = new ProfileService(profileRepository, userRepository);

            ActionController actionController = new ActionController(gameService, viewModelConverter, firebaseUtil);
            baseGameController = new GameController(authorizationService, gameService, profileService, viewModelConverter, firebaseUtil);
            MapController mapController = new MapController(mapFactory);
            ProfileController profileController = new ProfileController(profileService, userService, authorizationService);
            TokenController tokenController = new TokenController(authorizationService);
            UserController userController = new UserController(userService, authorizationService);
            BeanValidator validator = new BeanValidator();

            ReplayController replayController = new ReplayController(gameService);
            mvcBuilderWithoutGlobalExceptionHandler = MockMvcBuilders.standaloneSetup(actionController, baseGameController, mapController, profileController, tokenController, userController, replayController);
            TokenHandlerInterceptor tokenHandlerInterceptor = new TokenHandlerInterceptor(authorizationService);
            mvcBuilderWithoutGlobalExceptionHandler.setValidator(validator).addInterceptors(tokenHandlerInterceptor);
            mockMvc = mvcBuilderWithoutGlobalExceptionHandler.build();
        }

        if (objectMapper == null) {
            objectMapper = new ObjectMapper();
        }
    }

    protected ExceptionHandlerExceptionResolver getGlobalExceptionHandler() {
        ExceptionHandlerExceptionResolver exceptionResolver = new ExceptionHandlerExceptionResolver() {
            protected ServletInvocableHandlerMethod getExceptionHandlerMethod(HandlerMethod handlerMethod, Exception exception) {
                Method method = new ExceptionHandlerMethodResolver(GlobalExceptionHandler.class).resolveMethod(exception);
                return new ServletInvocableHandlerMethod(new GlobalExceptionHandler(), method);
            }
        };
        exceptionResolver.afterPropertiesSet();
        return exceptionResolver;
    }

    protected String loginAndRetrieveAccessToken() throws Exception {
        String username = "test";
        String password = "test";
        String email = "test@gmail.com";

        return loginExistingUserAndRetrieveAccessToken(username, password, email);
    }

    private String loginExistingUserAndRetrieveAccessToken(String username, String password, String email) throws Exception {
        String md5HashedPassword = getMD5HashedPassword(password);
        User testUser = new User(username, md5HashedPassword, email, true);
        String userJson = objectMapper.writeValueAsString(testUser);

        MockHttpServletRequestBuilder requestBuilder = post("/accesstokens");
        String accessTokenJson = mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson)
                .accept(MediaType.APPLICATION_JSON)).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        AccessToken accessToken = objectMapper.readValue(accessTokenJson, AccessToken.class);

        return "%22" + accessToken.getValue() + "%22";
    }

    protected Profile createOpponent() throws Exception {
        ProfileWrapper profileWrapper = new ProfileWrapper("opponentname", "opponentlastname", "opponentemail@gmail.com", "12-07-1992", "image");
        String profileWrapperJson = objectMapper.writeValueAsString(profileWrapper);
        String opponentAccessToken = logOpponentIn();

        mockMvc.perform(post("/auth/profile")
                .contentType(MediaType.APPLICATION_JSON)
                .content(profileWrapperJson)
                .cookie(new Cookie("accessToken", opponentAccessToken)));

        String profileJson = mockMvc.perform(get("/auth/profile")
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", opponentAccessToken))).andReturn().getResponse().getContentAsString();

        Profile profile = objectMapper.readValue(profileJson, Profile.class);

        return profile;
    }

    private String logOpponentIn() throws Exception {

        String accessToken = loginExistingUserAndRetrieveAccessToken("opponentTest", "test", "opponentje@gmail.com");
        return accessToken;
    }

    protected GameActivePlayerWrapper createAGame(String accessToken) throws Exception {
        Profile opponentProfile = createOpponent();

        GameParameters gameParameters = new GameParameters("SpacecrackGame1", opponentProfile.getProfileId());
        String gameParametersJson = objectMapper.writeValueAsString(gameParameters);

        String gameIdJson = mockMvc.perform(post("/auth/game")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(gameParametersJson)
                .cookie(new Cookie("accessToken", accessToken))).andReturn().getResponse().getContentAsString();
        System.out.println(gameIdJson);
        int gameId = objectMapper.readValue(gameIdJson, Integer.class);

        String gameJson = mockMvc.perform(get("/auth/game/specificGame/" + gameId)
                .accept(MediaType.APPLICATION_JSON)
                .cookie(new Cookie("accessToken", accessToken))).andReturn().getResponse().getContentAsString();

        return objectMapper.readValue(gameJson, GameActivePlayerWrapper.class);
    }

    private String getMD5HashedPassword(String testPassword) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] byteData = md5.digest(testPassword.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }
}
