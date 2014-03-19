package be.kdg.spacecrack.unittests;

import be.kdg.spacecrack.Exceptions.SpaceCrackUnauthorizedException;
import be.kdg.spacecrack.Exceptions.SpaceCrackUnexpectedException;
import be.kdg.spacecrack.controllers.TokenController;
import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.User;
import be.kdg.spacecrack.repositories.IUserRepository;
import be.kdg.spacecrack.repositories.TokenRepository;
import be.kdg.spacecrack.repositories.UserRepository;
import be.kdg.spacecrack.services.AuthorizationService;
import be.kdg.spacecrack.utilities.ITokenStringGenerator;
import be.kdg.spacecrack.utilities.TokenStringGenerator;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertEquals;


/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
public class TokenControllerTests extends BaseUnitTest{
    private TokenController tokenController;
    private TokenStringGenerator fixedSeedGenerator;
    private User testUser;

    @Before
    public void setUp() throws Exception {
        fixedSeedGenerator = new TokenStringGenerator(1234);
        TokenRepository tokenRepository = new TokenRepository(sessionFactory);
        UserRepository userRepository = new UserRepository(sessionFactory);
        tokenController = new TokenController(new AuthorizationService(tokenRepository,  userRepository, fixedSeedGenerator));

        Session session = sessionFactory.getCurrentSession();

        testUser = new User("testUsername2", "testPassword2", "testEmail2",  true);
        session.saveOrUpdate(testUser);
    }

    @Test(expected = SpaceCrackUnauthorizedException.class)
    @Transactional
    public void testRequestAccessToken_InvalidUser_UserNotFoundException() {
        String name = "badUser";
        String pw = "badPw";
        String email = "badEmail";
        User user = new User(name, pw, email, true);
        tokenController.login(user);
    }

    @Test
    @Transactional
    public void testRequestAccessToken_ValidUser_Ok() {
        ITokenStringGenerator mockTokenGenerator = Mockito.mock(ITokenStringGenerator.class);
        TokenRepository tokenRepository = new TokenRepository(sessionFactory);
        UserRepository userRepository = new UserRepository(sessionFactory);
        TokenController tokenControllerWithMockedGenerator = new TokenController(new AuthorizationService(tokenRepository, userRepository, mockTokenGenerator));
        String email = "testEmail2";
        String name ="testUsername2";
        String pw = "testPassword2";
        User user = new User(name, pw, email, true);
        String expectedTokenValue = "testtokenvalue1234";
        Mockito.stub(mockTokenGenerator.generateTokenString()).toReturn(expectedTokenValue);
        AccessToken token = tokenControllerWithMockedGenerator.login(user);


        assertEquals("Token value should be testtokenvalue1234", expectedTokenValue, token.getValue() );
    }

    @Test
    @Transactional
    public void testRequestAccessToken_UnverifiedUser_NotAuthorized() {
        ITokenStringGenerator mockTokenGenerator = Mockito.mock(ITokenStringGenerator.class);
        TokenRepository tokenRepository = new TokenRepository(sessionFactory);
        UserRepository userRepository = new UserRepository(sessionFactory);
        TokenController tokenControllerWithMockedGenerator = new TokenController(new AuthorizationService(tokenRepository, userRepository, mockTokenGenerator));
        String email = "testEmail2";
        String name ="testUsername2";
        String pw = "testPassword2";
        User user = new User(name, pw, email, true);
        String expectedTokenValue = "testtokenvalue1234";
        Mockito.stub(mockTokenGenerator.generateTokenString()).toReturn(expectedTokenValue);
        AccessToken token = tokenControllerWithMockedGenerator.login(user);


        assertEquals("Token value should be testtokenvalue1234", expectedTokenValue, token.getValue() );
    }

    @Test(expected = SpaceCrackUnexpectedException.class)
    @Transactional
    public void testgetUser() throws Exception {
        IUserRepository userRepository = Mockito.mock(IUserRepository.class);
        User user = new User("testUsername2", "testPassword2", "testEmail2", true);
        Mockito.stub(userRepository.getUser(user)).toThrow(new SpaceCrackUnexpectedException("UnexpectedException"));
        TokenRepository tokenRepository = new TokenRepository();
        TokenController tokenController1 = new TokenController(new AuthorizationService(tokenRepository, userRepository, fixedSeedGenerator));
        tokenController1.login(user);
    }
}
