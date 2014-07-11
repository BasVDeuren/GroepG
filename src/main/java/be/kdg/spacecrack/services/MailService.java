package be.kdg.spacecrack.services;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.authentication.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service("mailService")
public class MailService implements IMailService {
    public static final String SPACECRACKWEBSITEURL = "http://localhost:8081/#/";
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private MailSender mailSender;

    public MailService() {
    }

    public MailService(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendInvite(User sender, String receiver) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(receiver);
        simpleMailMessage.setSubject("Invitation Spacecrack");
        String textToShow = sender.getProfile().getFirstname() + " " + sender.getProfile().getLastname();
        if(textToShow.equals("")){
            textToShow = "with email: " + sender.getEmail();
        }
        String expectedMessage = "Hello there, your friend " + textToShow + " invites you for a game of spacecrack, would you like to register?\n" +
                "This can be done at our website: " + SPACECRACKWEBSITEURL;


        simpleMailMessage.setText(expectedMessage);
        mailSender.send(simpleMailMessage);
    }
}
