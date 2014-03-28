package be.kdg.spacecrack.unittests;

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.Exceptions.SpaceCrackUnauthorizedException;
import be.kdg.spacecrack.controllers.UserController;
import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.User;
import be.kdg.spacecrack.repositories.IUserRepository;
import be.kdg.spacecrack.repositories.ProfileRepository;
import be.kdg.spacecrack.services.IAuthorizationService;
import be.kdg.spacecrack.services.IUserService;
import be.kdg.spacecrack.services.UserService;
import be.kdg.spacecrack.viewmodels.UserViewModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.mail.javamail.JavaMailSender;
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


    private IUserRepository userRepository;
    private IAuthorizationService tokenService;
    private IUserService userService;
    private ObjectMapper objectMapper;
    private UserController userController;

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


        UserViewModel userWrapper = new UserViewModel("username", "password", "password", "email");
        UserController userController1 = new UserController(new UserService(userRepository, new ProfileRepository(sessionFactory), mock(JavaMailSender.class)), tokenService);
         userController1.registerUser(userWrapper);
        verify(userRepository, VerificationModeFactory.times(1)).createUser(any(User.class));
    }

    @Test(expected = SpaceCrackNotAcceptableException.class)
    public void RegisterUser_BadRepeatPassword_SpaceCrackNotAcceptableException() throws Exception {
        userController.registerUser(new UserViewModel("username", "password", "badRepeat", "email"));
    }

    @Test
    public void editUser_ValidFields_UserEdited() throws Exception {
        User user = new User("username", "password", "email", true);
        when(userService.getUserByAccessToken(any(AccessToken.class))).thenReturn(user);

        userController.editUser(new UserViewModel("username", "password", "password", "email"), objectMapper.writeValueAsString(new AccessToken("accesstoken1234")));

        Mockito.verify(userService, VerificationModeFactory.times(1)).updateUser(user);
    }

    @Test(expected = SpaceCrackNotAcceptableException.class)
    public void EditUser_BadRepeatPassword_SpaceCrackNotAcceptableException() throws Exception {

        User user = new User("username", "password", "email", true);
        when(userRepository.getUserByAccessToken(any(AccessToken.class))).thenReturn(user);

        userController.registerUser(new UserViewModel("username", "password", "password", "email"));
        userController.editUser(new UserViewModel("username", "newPassword", "newBadRepeatedPassword", "newEmail"), objectMapper.writeValueAsString(new AccessToken("accesstoken1234")));
    }

    @Test
    public void testGetUser_validToken_User() throws Exception {
        User expextedUser = new User("username", "password", "email", true);
        AccessToken accessToken = new AccessToken("accesstoken123");
        expextedUser.setToken(accessToken);

        stub(userService.getUserByAccessToken(accessToken)).toReturn(expextedUser);
        stub(tokenService.getAccessTokenByValue(accessToken.getValue())).toReturn(accessToken);

        User actual = userController.getUserByToken(accessToken.getValue());

        assertEquals("User from usercontroller should be the same as from db", expextedUser, actual);
    }

    @Test(expected = SpaceCrackUnauthorizedException.class)
    public void testGetUser_NotInDbToken_SpaceCrackNotAcceptableException() throws Exception {


        AccessToken invalidAccessToken = new AccessToken("TokenNotInDb");
        when(userRepository.getUserByAccessToken(any(AccessToken.class))).thenReturn(null);

        userController.getUserByToken(invalidAccessToken.getValue());
    }

    @Test
    public void testGetUsers_validUserName_User() throws Exception {
        User user = new User("Jacky", "password", "email", true);
        List<User> foundUsers = new ArrayList<User>();
        AccessToken accessToken = new AccessToken("accesstoken123");
        user.setToken(accessToken);
        foundUsers.add(user);
        stub(userService.getUsersByString("Jac")).toReturn(foundUsers);

        List<User> actualUsers = new ArrayList<User>();
        actualUsers = userController.getUsersByString("Jac");

        assertEquals("Users from usercontroller should be the same as from db", foundUsers.get(0), actualUsers.get(0));
    }

    @Test
    public void testGetUsers_validEmail_User() throws Exception {
        User user = new User("Tommy", "password", "tommy@gmail.com", true);
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
        User user1 = new User("Jacky", "password", "email", true);
        AccessToken accessToken = new AccessToken("accesstoken123");
        user1.setToken(accessToken);

        User user2 = new User("barry", "password", "email", true);
        AccessToken accessToken2 = new AccessToken("accesstoken123321");
        user2.setToken(accessToken2);

        stub(userService.getRandomUser(user1.getUserId())).toReturn(user2);

        User actualUser = userController.getRandomUser(user1.getUserId());

        assertEquals("Users from usercontroller should be the same as from db", actualUser, user2);
    }
}
