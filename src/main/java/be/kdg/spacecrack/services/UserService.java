package be.kdg.spacecrack.services;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.Exceptions.SpaceCrackAlreadyExistsException;
import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.Exceptions.SpaceCrackUnexpectedException;
import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.Profile;
import be.kdg.spacecrack.model.User;
import be.kdg.spacecrack.repositories.IProfileRepository;
import be.kdg.spacecrack.repositories.IUserRepository;
import be.kdg.spacecrack.utilities.ITokenStringGenerator;
import be.kdg.spacecrack.utilities.TokenStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.List;
import java.util.Random;

@Component("userService")
@Transactional
public class UserService implements IUserService {
    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private IProfileRepository profileRepository;
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private ITokenStringGenerator tokenStringGenerator;

    public UserService() {
    }

    public UserService(IUserRepository userRepository, IProfileRepository profileRepository, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.mailSender = mailSender;
        tokenStringGenerator = new TokenStringGenerator();
    }

    public UserService(IUserRepository userRepository, IProfileRepository profileRepository, JavaMailSender mailSender, ITokenStringGenerator tokenStringGenerator) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.mailSender = mailSender;
        this.tokenStringGenerator = tokenStringGenerator;
    }

    @Override
    public User getUserByAccessToken(AccessToken accessToken) throws Exception {
        return userRepository.getUserByAccessToken(accessToken);
    }

    @Override
    public User getUserByUsername(String username) throws Exception {
        return userRepository.findUserByUsername(username);
    }

    @Override
    public void registerUser(String username, String password, String email) {
        User userByUsername = userRepository.findUserByUsername(username);
        if (userByUsername != null) {
            throw new SpaceCrackAlreadyExistsException();
        }

        User userByEmail = userRepository.getUserByEmail(email);
        if (userByEmail != null) {
            throw new SpaceCrackAlreadyExistsException();
        }

        User user = new User(username, password, email, false);
        Profile profile = new Profile();
        profile.setUser(user);
        user.setProfile(profile);
        user.setVerificationToken(tokenStringGenerator.generateTokenString(6));
        userRepository.save(user);
        MimeMessage mimeMessage = new MimeMessage(Session.getDefaultInstance(System.getProperties()));
        try {
            mimeMessage.setContent(
                    "<p>" +
                            "Dear " + username + ", " +
                            "</p>" +
                            "<p>" +
                            "This is your verification code: " + user.getVerificationToken() +
                            "</p>" +
                            "<p>Press this link to confirm your registration to Spacecrack : " +
                            "</p>" +
                            "<p>" +
                            "<a href=\"http://localhost:8081/#/verify\">Verify your registration here</a>" +
                            "</p>" +
                            "<p></p>" +
                            "<p>Have fun playing spacecrack!" +
                            "</p>" +
                            "<p>Yours sincerely, " +
                            "</p>" +
                            "<p>GroupG" +
                            "</p>",
                    "text/html");
            mimeMessage.setRecipients(Message.RecipientType.TO, email);
            mimeMessage.setSubject("SpaceCrack registration confirmation");

        } catch (MessagingException e) {
            throw new SpaceCrackUnexpectedException("Something bad happened");
        }
        mailSender.send(mimeMessage);
    }

    @Override
    public void registerFacebookUser(String username, String password, String email)  {

        User userByUsername = userRepository.findUserByUsername(username);
        if(userByUsername != null)
        {
            throw new SpaceCrackAlreadyExistsException();
        }

        User userByEmail = userRepository.getUserByEmail(email);
        if(userByEmail != null)
        {
            throw new SpaceCrackAlreadyExistsException();
        }
        User user = new User(username, password, email, true);

        Profile profile = new Profile();


        if(user.getProfile() == null){
            profile.setUser(user);
            user.setProfile(profile);
            profileRepository.save(profile);
            userRepository.save(user);

        }
        userRepository.save(user);
    }

    @Override
    public void updateUser(User user) {
        userRepository.save(user);
    }

    @Override
    public List<User> getUsersByString(String username) throws Exception {
        return userRepository.findUsersByUsernamePart(username);
    }

    @Override
    public List<User> getUsersByEmail(String email) throws Exception {
        return userRepository.findUsersByEmailPart(email);
    }

    @Override
    public User getRandomUser(int userId) throws Exception {
        User foundUser;
        List<User> users = userRepository.getLoggedInUsers();
        do {
            Random random = new Random();
            foundUser = users.get(random.nextInt(users.size()));
        } while (foundUser.getUserId() == userId);
        return foundUser;
    }

    @Override
    public void verifyUser(String tokenValue) {

        User user = userRepository.findUserByVerificationTokenValue(tokenValue);
        if (user == null) {
            throw new SpaceCrackNotAcceptableException("invalid verificationToken");
        }
        user.setVerified(true);
        userRepository.save(user);
    }
}
