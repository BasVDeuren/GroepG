package be.kdg.spacecrack.unittests;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.authentication.Profile;
import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.services.IMailService;
import be.kdg.spacecrack.services.MailService;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.internal.verification.VerificationModeFactory;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class MailTests {
    @Test
    public void sendMail_validMessage_sendMailHasBeenCalled() throws Exception {
        MailSender mailSender = mock(MailSender.class);
        IMailService mailService = new MailService(mailSender);

        String receiver = "test@mailadres.be";
        User sender = new User("testuser1234", "test", "testuser@gmail.com", true);
        sender.setProfile(new Profile("Alice", "In wonderland", null, null));

       String expectedMessage = "Hello there, your friend Alice In wonderland invites you for a game of spacecrack, would you like to register?\n" +
               "This can be done at our website: http://localhost:8081/#/";

        ArgumentCaptor<SimpleMailMessage> simpleMailMessageArgumentCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        mailService.sendInvite(sender, receiver);
        verify(mailSender, VerificationModeFactory.times(1)).send(simpleMailMessageArgumentCaptor.capture());
        SimpleMailMessage mailMessage = simpleMailMessageArgumentCaptor.getValue();

        String[] to = mailMessage.getTo();
        String message =  mailMessage.getText();
        assertEquals("The mail should be send to test@mailadres.be", receiver, to[0]);

        assertEquals("The contents of the message should be right", expectedMessage, message);
    }
}
