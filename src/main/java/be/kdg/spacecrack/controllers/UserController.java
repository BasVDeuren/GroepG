package be.kdg.spacecrack.controllers;

import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.Exceptions.SpaceCrackUnauthorizedException;
import be.kdg.spacecrack.model.authentication.AccessToken;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.services.IAuthorizationService;
import be.kdg.spacecrack.services.IUserService;
import be.kdg.spacecrack.viewmodels.UserViewModel;
import be.kdg.spacecrack.viewmodels.VerificationTokenViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

//import org.codehaus.jackson.JsonParseException;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Controller
public class UserController {

    @Autowired
    private IUserService userService;

    @Autowired
    private IAuthorizationService authorizationService;

    public UserController() {}

    public UserController(IUserService userService, IAuthorizationService authorizationService) {
        this.userService = userService;
        this.authorizationService = authorizationService;
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public void registerUser(@RequestBody @Valid UserViewModel userWrapper) throws Exception {

        if (userWrapper.getPassword().equals(userWrapper.getPasswordRepeated())) {
            userService.registerUser(userWrapper.getUsername(), userWrapper.getPassword(), userWrapper.getEmail());
            } else {
            throw new SpaceCrackNotAcceptableException("Password and repeat password aren't equal");
        }


    }

    @RequestMapping(value = "/fbuser", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public void registerFaceBookUser(@RequestBody @Valid UserViewModel userWrapper) throws Exception {

        if (userWrapper.getPassword().equals(userWrapper.getPasswordRepeated())) {
            userService.registerFacebookUser(userWrapper.getUsername(), userWrapper.getPassword(), userWrapper.getEmail());
        } else {
            throw new SpaceCrackNotAcceptableException("Password and repeat password aren't equal");
        }


    }

    @RequestMapping(value = "/auth/user", method = RequestMethod.POST, consumes = "application/json")
    @ResponseBody
    public void editUser(@RequestBody UserViewModel userWrapper, @CookieValue("accessToken") String accessTokenValue) throws Exception {
        AccessToken accessToken = authorizationService.getAccessTokenByValue(accessTokenValue);
        User user = userService.getUserByAccessToken(accessToken);

        if (userWrapper.getPassword().equals(userWrapper.getPasswordRepeated())) {
            user.setPassword(userWrapper.getPassword());
            user.setEmail(userWrapper.getEmail());
            user.setUsername(userWrapper.getUsername());
            userService.updateUser(user);
        } else {
            throw new SpaceCrackNotAcceptableException("Passwords should be the same!");
        }
    }

    @RequestMapping(value = "/auth/user", method = RequestMethod.GET)
    @ResponseBody
    public User getUserByToken(@CookieValue("accessToken") String cookieAccessTokenvalue) throws Exception {
        User user = userService.getUserByAccessToken(authorizationService.getAccessTokenByValue(cookieAccessTokenvalue));
        if (user == null) {
            throw new SpaceCrackUnauthorizedException();
        }

        return user;
    }

    @RequestMapping(value = "/auth/findusersbyusername/{username}", method = RequestMethod.GET)
    @ResponseBody
    public List<User> getUsersByString(@PathVariable String username) throws Exception {
        return userService.getUsersByString(username);
    }

    @RequestMapping(value = "/auth/findusersbyemail", method = RequestMethod.GET)
    @ResponseBody
    public List<User> getUsersByEmail(@RequestParam(value = "email") String email) throws Exception {
        return userService.getUsersByEmail(email);
    }

    @RequestMapping(value = "/auth/findUserByUserId/{userId}", method = RequestMethod.GET)
    @ResponseBody
    public User getRandomUser(@PathVariable int userId) throws Exception {
        return userService.getRandomUser(userId);
    }

    @RequestMapping(value= "/verifiedUser", method = RequestMethod.POST)
    @ResponseBody
    public void verifyRegistration(@RequestBody VerificationTokenViewModel verificationTokenViewModel){
        userService.verifyUser(verificationTokenViewModel.getTokenValue());

    }

}
