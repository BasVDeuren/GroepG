package be.kdg.spacecrack.integrationtests;

import be.kdg.spacecrack.controllers.UserController;
import be.kdg.spacecrack.model.User;
import be.kdg.spacecrack.repositories.IUserRepository;
import be.kdg.spacecrack.repositories.ProfileRepository;
import be.kdg.spacecrack.repositories.TokenRepository;
import be.kdg.spacecrack.repositories.UserRepository;
import be.kdg.spacecrack.services.AuthorizationService;
import be.kdg.spacecrack.services.UserService;
import be.kdg.spacecrack.utilities.ITokenStringGenerator;
import be.kdg.spacecrack.utilities.TokenStringGenerator;
import be.kdg.spacecrack.viewmodels.UserViewModel;
import be.kdg.spacecrack.viewmodels.VerificationTokenViewModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.hibernate.Session;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.mail.Message;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.Cookie;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//import org.codehaus.jackson.map.ObjectMapper;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
public class IntegrationUserTests extends BaseFilteredIntegrationTests {
    private UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        userRepository = new UserRepository(sessionFactory);
    }

    @Test
    public void testEditUser_validEditedUser_StatusOk() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Session session = sessionFactory.getCurrentSession();

        User testUser = new User("usernameTest²", "password", "email", true);
        session.saveOrUpdate(testUser);

        String userjson = objectMapper.writeValueAsString(testUser);

        MockHttpServletRequestBuilder requestBuilder = post("/accesstokens");
        mockMvc.perform(requestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(userjson)
                .accept(MediaType.APPLICATION_JSON))
                .andReturn();

        String userMapperJsonValid = objectMapper.writeValueAsString(new UserViewModel("usernameTest", "password", "password", "newEmail"));

        MockHttpServletRequestBuilder putRequestBuilder = post("/auth/user");
        String tokenOfEditedUser = userRepository.getUserByUsername(testUser.getUsername()).getToken().getValue();
        mockMvc.perform(putRequestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(userMapperJsonValid)
                .cookie(new Cookie("accessToken", "%22" + tokenOfEditedUser + "%22"))
        ).andExpect(status().isOk());
    }


    @Test
    public void testRegisterUser_NewUser_VerificationTokenMailSent() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String expectedEmail = "email@gmail.com";
        String validUserWrapperJson = objectMapper.writeValueAsString(new UserViewModel("usernameTest", "password", "password", expectedEmail));
        MockHttpServletRequestBuilder postRequestBuilder = post("/user");
        IUserRepository mockUserRepository = mock(IUserRepository.class);
        JavaMailSender mockMailSender = mock(JavaMailSender.class);

        ITokenStringGenerator mockTokenStringGenerator = mock(ITokenStringGenerator.class);
        MockMvc mockMvcStandalone = MockMvcBuilders.standaloneSetup(new UserController(new UserService(mockUserRepository, new ProfileRepository(sessionFactory), mockMailSender, mockTokenStringGenerator), new AuthorizationService(new TokenRepository(sessionFactory), mockUserRepository, new TokenStringGenerator()))).build();
        String testToken = "testToken123";
        stub(mockTokenStringGenerator.generateTokenString()).toReturn(testToken);

        mockMvcStandalone.perform(postRequestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(validUserWrapperJson))
                .andExpect(status().isOk());
        ArgumentCaptor<MimeMessage> mimeMessageArgumentCaptor = ArgumentCaptor.forClass(MimeMessage.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository, VerificationModeFactory.times(1)).createUser(userArgumentCaptor.capture());
        User userArgument = userArgumentCaptor.getValue();

        assertEquals("User shouldn't be verified yet", userArgument.isVerified(), false);
        assertEquals("User should have verificationToken", testToken ,userArgument.getVerificationToken());
        verify(mockMailSender, VerificationModeFactory.times(1)).send(mimeMessageArgumentCaptor.capture());
        MimeMessage mimeMessage = mimeMessageArgumentCaptor.getValue();

        String content = mimeMessage.getContent().toString();

        assertEquals("Expect 'To' to be equal to the registered emailadress ", expectedEmail, mimeMessage.getRecipients(Message.RecipientType.TO)[0].toString());
        assertEquals("Expect Subject to be equal to 'SpaceCrack registration confirmation' ", "SpaceCrack registration confirmation", mimeMessage.getSubject());
        assertEquals("Expect mail to start with 'dear usernameTest,' ", "<p>Dear usernameTest,", content.substring(0, 21));
        String verificationCodeString = "verification code: ";
        int beginIndex = content.indexOf(verificationCodeString) + verificationCodeString.length();
        assertEquals("Expect mail to contain token testToken123", testToken, content.substring(beginIndex, beginIndex + testToken.length()) );

    }

    @Test
    public void testRegisterUser_SameUserTwice_Conflict() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String validUserWrapperJson = objectMapper.writeValueAsString(new UserViewModel("usernameTest", "password", "password", "email@gmail.com"));
        MockHttpServletRequestBuilder postRequestBuilder = post("/user");
        mockMvc.perform(postRequestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(validUserWrapperJson));

        mockMvc.perform(postRequestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(validUserWrapperJson))
                .andExpect(status().isConflict());
    }

    @Test
    public void testRegisterUser_Badrepeat_NotAcceptable() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String invalidUserWrapper = objectMapper.writeValueAsString(new UserViewModel("username", "password", "badpassword", "email@gmail.com"));
        MockHttpServletRequestBuilder postRequestBuilder = post("/user");
        MockMvc mockMvcWithoutGlobalExceptionHandler = mvcBuilderWithoutGlobalExceptionHandler.build();

        mockMvcWithoutGlobalExceptionHandler.perform(postRequestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(invalidUserWrapper))
                .andExpect(status().isNotAcceptable());
    }

    @Test
    public void testGetUser_validToken_User() throws Exception {

        String accessToken = loginAndRetrieveAccessToken();
        MockHttpServletRequestBuilder getUserRequestBuilder = get("/auth/user")
                .cookie(new Cookie("accessToken", accessToken));

        mockMvc.perform(getUserRequestBuilder).andExpect(status().isOk());
    }

    @Test
    public void testGetUser_InvalidToken_SpaceCrackUnauthorisedException() throws Exception {
        MockHttpServletRequestBuilder getUserRequestBuilder = get("/auth/user")
                .cookie(new Cookie("accessToken", "invalidToken"));

        mockMvc.perform(getUserRequestBuilder).andExpect(status().isUnauthorized());
    }

    @Test
    public void verifyRegistration_validToken_registrationVerified() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String expectedEmail = "email@gmail.com";
        String validUserWrapperJson = objectMapper.writeValueAsString(new UserViewModel("usernameTest", "password", "password", expectedEmail));
        MockHttpServletRequestBuilder postRequestBuilder = post("/user");
        IUserRepository mockUserRepository = mock(IUserRepository.class);
        JavaMailSender mockMailSender = mock(JavaMailSender.class);

        ITokenStringGenerator mockTokenStringGenerator = mock(ITokenStringGenerator.class);
        MockMvc mockMvcStandalone = MockMvcBuilders.standaloneSetup(new UserController(new UserService(mockUserRepository, new ProfileRepository(sessionFactory), mockMailSender, mockTokenStringGenerator), new AuthorizationService(new TokenRepository(sessionFactory), mockUserRepository, new TokenStringGenerator()))).build();
        String testToken = "testToken123";
        stub(mockTokenStringGenerator.generateTokenString()).toReturn(testToken);
        stub(mockUserRepository.findUserByVerificationTokenValue(testToken)).toReturn(new User());

         mockMvcStandalone.perform(postRequestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(validUserWrapperJson))
                .andExpect(status().isOk());
        VerificationTokenViewModel verificationTokenViewModel = new VerificationTokenViewModel();
        verificationTokenViewModel.setTokenValue("testToken123");
        String verificationTokenViewModelJSon = objectMapper.writeValueAsString(verificationTokenViewModel);
        mockMvcStandalone.perform(post("/verifiedUser/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(verificationTokenViewModelJSon))
                .andExpect(status().isOk());
        ArgumentCaptor<User>  userArgumentCaptor = ArgumentCaptor.forClass(User.class);
        verify(mockUserRepository, VerificationModeFactory.times(1)).updateUser(userArgumentCaptor.capture());
        assertTrue("The user should be verified",userArgumentCaptor.getValue().isVerified());



    }

    @Test
    public void verifyRegistration_invalidToken_NotAcceptable() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String expectedEmail = "email@gmail.com";
        String validUserWrapperJson = objectMapper.writeValueAsString(new UserViewModel("usernameTest", "password", "password", expectedEmail));
        MockHttpServletRequestBuilder postRequestBuilder = post("/user");
        IUserRepository mockUserRepository = mock(IUserRepository.class);
        JavaMailSender mockMailSender = mock(JavaMailSender.class);

        ITokenStringGenerator mockTokenStringGenerator = mock(ITokenStringGenerator.class);
        MockMvc mockMvcStandalone = MockMvcBuilders.standaloneSetup(new UserController(new UserService(mockUserRepository, new ProfileRepository(sessionFactory), mockMailSender, mockTokenStringGenerator), new AuthorizationService(new TokenRepository(sessionFactory), mockUserRepository, new TokenStringGenerator()))).build();
        String testToken = "testToken123";
        stub(mockTokenStringGenerator.generateTokenString()).toReturn(testToken);
        String invalidtoken = "invalidtoken";
        stub(mockUserRepository.findUserByVerificationTokenValue(invalidtoken)).toReturn(null);

        mockMvcStandalone.perform(postRequestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content(validUserWrapperJson))
                .andExpect(status().isOk());
        VerificationTokenViewModel verificationTokenViewModel = new VerificationTokenViewModel();

        verificationTokenViewModel.setTokenValue(invalidtoken);
        String verificationTokenViewModelJSon = objectMapper.writeValueAsString(verificationTokenViewModel);
        mockMvcStandalone.perform(post("/verifiedUser/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(verificationTokenViewModelJSon))
                .andExpect(status().isNotAcceptable());



    }
}
