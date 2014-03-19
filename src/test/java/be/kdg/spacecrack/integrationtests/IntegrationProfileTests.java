package be.kdg.spacecrack.integrationtests;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.viewmodels.ProfileWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import javax.servlet.http.Cookie;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

//import org.codehaus.jackson.map.ObjectMapper;

public class IntegrationProfileTests extends BaseFilteredIntegrationTests {


    @Before
    public void setUp() throws Exception {

    }

    @Test
    public void testUpdateEditProfile_ValidProfile_statusOk() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        String accessToken = loginAndRetrieveAccessToken();

        ProfileWrapper profile = new ProfileWrapper("firstname", "lastname", "email", "12-01-2013", "image");

        String profileJson = objectMapper.writeValueAsString(profile);

        MockHttpServletRequestBuilder postRequestBuilder = post("/auth/profile");
        mockMvc.perform(postRequestBuilder
                .contentType(MediaType.APPLICATION_JSON)
                .content(profileJson)
                .cookie(new Cookie("accessToken",  accessToken ))).andExpect(status().isOk());
    }
}
