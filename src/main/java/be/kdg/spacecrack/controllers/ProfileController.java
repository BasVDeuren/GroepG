package be.kdg.spacecrack.controllers;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.authentication.Profile;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.services.IAuthorizationService;
import be.kdg.spacecrack.services.IProfileService;
import be.kdg.spacecrack.services.IUserService;
import be.kdg.spacecrack.viewmodels.ProfileWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
@RequestMapping("/auth/profile")
public class ProfileController {
    @Autowired
    IUserService userService;

    @Autowired
    IAuthorizationService tokenService;

    @Autowired
    IProfileService profileService;

    public ProfileController() {}

    public ProfileController(IProfileService profileService, IUserService userService, IAuthorizationService tokenService){
        this.profileService = profileService;
        this.userService = userService;
        this.tokenService = tokenService;
    }

    @RequestMapping(method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public void updateProfile(@RequestBody ProfileWrapper profileWrapper, @CookieValue("accessToken") String accessTokenValue) throws Exception {
        User user = tokenService.getUserByAccessTokenValue(accessTokenValue);
        Date date = new SimpleDateFormat("dd-MM-yyyy").parse(profileWrapper.getDayOfBirth());

        Profile profile = user.getProfile();
        profile.setFirstname(profileWrapper.getFirstname());
        profile.setLastname(profileWrapper.getLastname());
        profile.setDayOfBirth(date);
        profile.setImage(profileWrapper.getImage());

        profileService.editProfile(profile);
    }

    @RequestMapping(method = RequestMethod.GET)
    @ResponseBody
    public Profile getProfileByAccesToken(@CookieValue("accessToken") String accessTokenValue) throws Exception {
        User user = tokenService.getUserByAccessTokenValue(accessTokenValue);
        return user.getProfile();
    }
}
