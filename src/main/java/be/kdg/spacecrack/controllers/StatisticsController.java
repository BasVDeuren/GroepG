package be.kdg.spacecrack.controllers;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.authentication.Profile;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.services.IAuthorizationService;
import be.kdg.spacecrack.services.IStatisticsService;
import be.kdg.spacecrack.viewmodels.StatisticsViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/auth/statistics")
public class StatisticsController {
    @Autowired
    private IAuthorizationService authorizationService;

    @Autowired
    private IStatisticsService statisticsService;

    public StatisticsController() {}

    public StatisticsController(IAuthorizationService authorizationService, IStatisticsService statisticsService)
    {
        this.authorizationService = authorizationService;
        this.statisticsService = statisticsService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public @ResponseBody StatisticsViewModel getStatisticsForProfile(@CookieValue("accessToken") String accessToken)
    {
        User userByAccessTokenValue = authorizationService.getUserByAccessTokenValue(accessToken);
        Profile profile = userByAccessTokenValue.getProfile();
        int profileId = profile.getProfileId();
        return statisticsService.getStatistics(profileId);
    }
}
