package be.kdg.spacecrack.controllers;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.services.IAuthorizationService;
import be.kdg.spacecrack.services.IMailService;
import be.kdg.spacecrack.viewmodels.EmailAdressViewModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping(value = "/auth/invitation")
public class MailController{
    @Autowired
    private IMailService mailService;
    @Autowired
    private IAuthorizationService authorizationService;

    public MailController() {
    }

    public MailController(IMailService mailService, IAuthorizationService authorizationService) {
        this.mailService = mailService;
        this.authorizationService = authorizationService;
    }

    @RequestMapping(method = RequestMethod.POST)
    public @ResponseBody void sendInvitation(@CookieValue("accessToken") String accessTokenValue, @RequestBody EmailAdressViewModel receiverMail)
    {
        User user = authorizationService.getUserByAccessTokenValue(accessTokenValue);
        mailService.sendInvite(user, receiverMail.getEmailAddress());
    }


}
