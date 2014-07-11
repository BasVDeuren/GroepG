package be.kdg.spacecrack.integrationtests;

import be.kdg.spacecrack.controllers.TokenController;
import be.kdg.spacecrack.model.authentication.AccessToken;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.repositories.ITokenRepository;
import be.kdg.spacecrack.repositories.IUserRepository;
import be.kdg.spacecrack.services.AuthorizationService;
import be.kdg.spacecrack.utilities.TokenStringGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.Cookie;

import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//import org.codehaus.jackson.map.ObjectMapper;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
public class IntegrationTokenFilterTests extends BaseFilteredIntegrationTests {
    MockHttpServletRequestBuilder requestBuilder;
    private User testUser;
    @Autowired
    private ITokenRepository tokenRepository;
    @Autowired
    private IUserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        objectMapper = new ObjectMapper();
        requestBuilder = get("/auth/user");

        testUser = new User("testUsername", "testPassword", "testEmail", true);
        userRepository.save(testUser);
    }

    @Test
    public void getAuthUser_NoToken_Unauthorized() throws Exception {
        mockMvc.perform(requestBuilder)
                .andExpect(status().isUnauthorized());
    }

    @Test
    public void getAuthUser_InvalidToken_Unauthorized() throws Exception {
        String invalidTokenValue = new TokenStringGenerator(1235).generateTokenString(32);
        AccessToken invalidToken = new AccessToken(invalidTokenValue);

        MvcResult mvcResult = mockMvc.perform(requestBuilder
                .cookie(new Cookie("accessToken", invalidToken.getValue())))
                .andExpect(status().isUnauthorized())
                .andReturn();
        MockHttpServletResponse response = mvcResult.getResponse();
        assertEquals("You are unauthorized for this request", response.getErrorMessage());
    }

    @Test
    public void TokenFilter_validToken_OK() throws Exception {
        TokenStringGenerator generator = new TokenStringGenerator(12345);

        TokenController tokenController = new TokenController(new AuthorizationService(tokenRepository, userRepository, generator));
        AccessToken validToken = tokenController.login(testUser);
        mockMvc.perform(requestBuilder.cookie(new Cookie("accessToken", "%22" + validToken.getValue() + "%22"))).andExpect(status().isOk());
    }
}
