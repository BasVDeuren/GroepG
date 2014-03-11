package be.kdg.spacecrack.unittests;

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.Exceptions.SpaceCrackUnauthorizedException;
import be.kdg.spacecrack.controllers.UserController;
import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.User;
import be.kdg.spacecrack.repositories.IUserRepository;
import be.kdg.spacecrack.services.IAuthorizationService;
import be.kdg.spacecrack.services.IUserService;
import be.kdg.spacecrack.services.UserService;
import be.kdg.spacecrack.viewmodels.UserViewModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

//import org.codehaus.jackson.map.ObjectMapper;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Transactional
public class UserControllerTest extends BaseUnitTest {

    private UserController userController;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    private IUserRepository userRepository;

    private IAuthorizationService tokenService;
    private IUserService userService;
    private ObjectMapper objectMapper;

    @Before
    public void setUp() throws Exception {
        userRepository = mock(IUserRepository.class);

        tokenService = mock(IAuthorizationService.class);

        userService = mock(UserService.class);
        userController = new UserController(userService, tokenService);
        objectMapper = new ObjectMapper();
    }

    @Test
    public void RegisterUser_validUser_usercreated() throws Exception {
        AccessToken expected = new AccessToken("test1234");
        stub(tokenService.login(any(User.class))).toReturn(expected);
        UserViewModel userWrapper = new UserViewModel("username", "password", "password", "email");

        AccessToken actual = userController.registerUser(userWrapper);
        assertEquals("Accesstoken should be returned after registering. ", expected, actual);

    }

    @Test(expected = SpaceCrackNotAcceptableException.class)
    public void RegisterUser_BadRepeatPassword_SpaceCrackNotAcceptableException() throws Exception {
        userController.registerUser(new UserViewModel("username", "password", "badRepeat", "email"));
    }




    @Test
    public void editUser_ValidFields_UserEdited() throws Exception {
        //   userController = new UserController(new UserRepository());
        ObjectMapper objectMapper = new ObjectMapper();


        User user = new User("username", "password", "email");
        when(userService.getUserByAccessToken(any(AccessToken.class))).thenReturn(user);

        userController.editUser(new UserViewModel("username", "password", "password", "email"), objectMapper.writeValueAsString(new AccessToken("accesstoken1234")));

        Mockito.verify(userService, VerificationModeFactory.times(1)).updateUser(user);
    }

    @Test
    public void EditUser_BadRepeatPassword_SpaceCrackNotAcceptableException() throws Exception {
        expectedEx.expect(SpaceCrackNotAcceptableException.class);
        expectedEx.expectMessage("Passwords should be the same!");

        ObjectMapper objectMapper = new ObjectMapper();
        User user = new User("username", "password", "email");
        when(userRepository.getUserByAccessToken(any(AccessToken.class))).thenReturn(user);

        userController.registerUser(new UserViewModel("username", "password", "password", "email"));
        userController.editUser(new UserViewModel("username", "newPassword", "newBadRepeatedPassword", "newEmail"), objectMapper.writeValueAsString(new AccessToken("accesstoken1234")));
    }

    @Test
    public void testGetUser_validToken_User() throws Exception {
        User user = new User("username", "password", "email");
        AccessToken accessToken = new AccessToken("accesstoken123");
        user.setToken(accessToken);

        stub(userService.getUserByAccessToken(accessToken)).toReturn(user);
        stub(tokenService.getAccessTokenByValue(accessToken.getValue())).toReturn(accessToken);

        //User actual = userController.getUserByToken(accessToken);
        User actual = userController.getUserByToken(accessToken.getValue());
        //User actual = userController.getUserByToken();

        User expected = user;

        assertEquals("User from usercontroller should be the same as from db", expected, actual);
    }

    @Test
    public void testGetUser_NotInDbToken_SpaceCrackNotAcceptableException() throws Exception {
        expectedEx.expect(SpaceCrackUnauthorizedException.class);

        AccessToken invalidAccessToken = new AccessToken("TokenNotInDb");

        when(userRepository.getUserByAccessToken(any(AccessToken.class))).thenReturn(null);

        //User actual = userController.getUserByToken(invalidAccessToken);
        userController.getUserByToken(invalidAccessToken.getValue());
        //userController.getUserByToken();
    }

    /*@Test
    public void testGetUser_TokenInDbButNotFromLoggedOnUser_SpaceCrackNotAcceptableException() throws Exception {
        expectedEx.expect(SpaceCrackNotAcceptableException.class);
        expectedEx.expectMessage("This request is not acceptable!");

        User notUserThatIsRequesting = new User("badUsername", "badPassword", "badEmail");
        AccessToken invalidAccessToken = new AccessToken("TokenNotWithUser");
        notUserThatIsRequesting.setToken(invalidAccessToken);

        User user = new User("username", "password", "email");
        AccessToken accessToken = new AccessToken("validAccessToken");
        user.setToken(accessToken);

        when(userRepository.getUserByAccessToken(any(AccessToken.class))).thenReturn(notUserThatIsRequesting);

        userController.getUserByToken(invalidAccessToken);
    }*/

    @Test
    public void testGetUsers_validUserName_User() throws Exception {
        User user = new User("Jacky", "password", "email");
        List<User> foundUsers = new ArrayList<User>();
        AccessToken accessToken = new AccessToken("accesstoken123");
        user.setToken(accessToken);
        foundUsers.add(user);
        stub(userService.getUsersByString("Jac")).toReturn(foundUsers);
        //stub(tokenService.getAccessTokenByValue(accessToken.getValue())).toReturn(accessToken);

        //User actual = userController.getUserByToken(accessToken);
        List<User> actualUsers = new ArrayList<User>();
        actualUsers = userController.getUsersByString("Jac");
        //User actual = userController.getUserByToken();

        assertEquals("Users from usercontroller should be the same as from db", foundUsers.get(0), actualUsers.get(0));
    }

    @Test
    public void testGetUsers_validEmail_User() throws Exception {
        User user = new User("Tommy", "password", "tommy@gmail.com");
        List<User> foundUsers = new ArrayList<User>();
        AccessToken accessToken = new AccessToken("accesstoken321");
        user.setToken(accessToken);
        foundUsers.add(user);
        stub(userService.getUsersByEmail("tom")).toReturn(foundUsers);
        List<User> actualUsers = new ArrayList<User>();
        actualUsers = userController.getUsersByEmail("tom");

        assertEquals("Users from usercontroller should be the same as from db", foundUsers.get(0), actualUsers.get(0));
    }

    @Test
    public void testGetUser_UserId_User() throws Exception {
        User user1 = new User("Jacky", "password", "email");
        AccessToken accessToken = new AccessToken("accesstoken123");
        user1.setToken(accessToken);

        User user2 = new User("barry", "password", "email");
        AccessToken accessToken2 = new AccessToken("accesstoken123321");
        user2.setToken(accessToken2);

        stub(userService.getRandomUser(user1.getUserId())).toReturn(user2);

        User actualUser = userController.getRandomUser(user1.getUserId());

        assertEquals("Users from usercontroller should be the same as from db", actualUser, user2);
    }

}
