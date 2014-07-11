package be.kdg.spacecrack.integrationtests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.controllers.StatisticsController;
import be.kdg.spacecrack.model.authentication.Profile;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.services.IAuthorizationService;
import be.kdg.spacecrack.services.IStatisticsService;
import be.kdg.spacecrack.viewmodels.StatisticsViewModel;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import javax.servlet.http.Cookie;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.stub;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


public class IntegrationStatisticsTests {

    @Test
    public void getStatistics() throws Exception {
        int profileId = 1;
        IAuthorizationService mockAuthorizationService = mock(IAuthorizationService.class);
        IStatisticsService mockStatisticsService = mock(IStatisticsService.class);

        User user = new User();
        Profile profile = new Profile();
        profile.setProfileId(profileId);
        user.setProfile(profile);
        stub(mockAuthorizationService.getUserByAccessTokenValue("testCookie")).toReturn(user);
        StatisticsViewModel statisticsViewModel = new StatisticsViewModel();
        statisticsViewModel.setWinRatio(0.55);
        statisticsViewModel.setAmountOfGames(5);
        statisticsViewModel.setAverageAmountOfColoniesPerWin(33.3);
        statisticsViewModel.setAverageAmountOfShipsPerWin(20.5);
        stub(mockStatisticsService.getStatistics(profileId)).toReturn(statisticsViewModel);

        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new StatisticsController(mockAuthorizationService, mockStatisticsService)).build();
        mockMvc.perform(get("/auth/statistics")
                .cookie(new Cookie("accessToken","testCookie"))
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.winRatio", CoreMatchers.is(0.55)))
                .andExpect(jsonPath("$.amountOfGames", CoreMatchers.is(5)))
                .andExpect(jsonPath("$.averageAmountOfColoniesPerWin", CoreMatchers.is(33.3)))
                .andExpect(jsonPath("$.averageAmountOfShipsPerWin", CoreMatchers.is(20.5)));
    }
}
