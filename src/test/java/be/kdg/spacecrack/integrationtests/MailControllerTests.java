package be.kdg.spacecrack.integrationtests;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.controllers.MailController;
import be.kdg.spacecrack.filters.TokenHandlerInterceptor;
import be.kdg.spacecrack.model.authentication.AccessToken;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.services.IAuthorizationService;
import be.kdg.spacecrack.services.IMailService;
import be.kdg.spacecrack.viewmodels.EmailAdressViewModel;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.Cookie;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class MailControllerTests {

    ObjectMapper objectMapper = new ObjectMapper();
    @Test
    public void SendInvitation_valid_ok() throws Exception {
        IMailService mockMailService = mock(IMailService.class);

        IAuthorizationService mockAuthorizationService = mock(IAuthorizationService.class);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MailController(mockMailService, mockAuthorizationService)).addInterceptors(new TokenHandlerInterceptor(mockAuthorizationService)).build();

        EmailAdressViewModel emailAdressViewModel = new EmailAdressViewModel();
        emailAdressViewModel.setEmailAddress("test@gmail.com");
        String emailAdressViewModelJSon = objectMapper.writeValueAsString(emailAdressViewModel);

        String testToken = "%20testToken%20";

        AccessToken accessToken = new AccessToken("testToken");
        stub(mockAuthorizationService.getAccessTokenByValue(eq("testToken"))).toReturn(accessToken);
        User user = new User();
        user.setUserId(1);

        stub(mockAuthorizationService.getUserByAccessTokenValue(eq("testToken"))).toReturn(user);
        mockMvc.perform(post("/auth/invitation").contentType(MediaType.APPLICATION_JSON).content(emailAdressViewModelJSon).cookie(new Cookie("accessToken", testToken))).andExpect(status().isOk());


        ArgumentCaptor<String> receiveArgumentCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

        verify(mockMailService, VerificationModeFactory.times(1)).sendInvite(userArgumentCaptor.capture(), receiveArgumentCaptor.capture());

        assertEquals("User's id should be 1",user.getUserId(),userArgumentCaptor.getValue().getUserId());
        assertEquals("receiver's email should be test@gmail.com", emailAdressViewModel.getEmailAddress(), receiveArgumentCaptor.getValue());
    }
}
