package be.kdg.spacecrack.services;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.Exceptions.SpaceCrackUnauthorizedException;
import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.Profile;
import be.kdg.spacecrack.model.User;
import be.kdg.spacecrack.repositories.ITokenRepository;
import be.kdg.spacecrack.repositories.IUserRepository;
import be.kdg.spacecrack.utilities.ITokenStringGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

@Service("authorizationService")
@Transactional
public class AuthorizationService implements IAuthorizationService {
    @Autowired
    private ITokenRepository tokenRepository;

    @Autowired
    private IUserRepository userRepository;

    @Autowired
    private ITokenStringGenerator tokenStringGenerator;

    public AuthorizationService() {}

    public AuthorizationService(ITokenRepository tokenRepository, IUserRepository userRepository, ITokenStringGenerator tokenStringGenerator) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.tokenStringGenerator = tokenStringGenerator;
    }

    @Override
    public AccessToken getAccessTokenByValue(String accessTokenValue) {
        return tokenRepository.getAccessTokenByValue(accessTokenValue);
    }

    @Override
    public String getMD5HashedPassword(String testPassword) {
        MessageDigest md5;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        byte[] byteData = md5.digest(testPassword.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < byteData.length; i++) {
            sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

    @Override
    public void createTestUser(String testemail, String testUsername, String testPassword,String firstname, String lastname) {
        List<User> list = userRepository.findUsersByEmailPart(testemail);
        if (list.size() < 1) {
            Profile profile = new Profile();
            profile.setFirstname(firstname);
            profile.setLastname(lastname);
            User user = new User(testUsername, testPassword, testemail, true);
            user.setProfile(profile);
            profile.setUser(user);
            userRepository.createUser(user);
        }
    }

    @Override
    public AccessToken login(User user) {
        User dbUser;
        dbUser = userRepository.getUser(user);
        if (dbUser == null || !dbUser.isVerified()) {
            throw new SpaceCrackUnauthorizedException();
        }

        AccessToken accessToken = dbUser.getToken();

        if (accessToken == null) {
            String tokenvalue = tokenStringGenerator.generateTokenString();
            accessToken = new AccessToken(tokenvalue);
            accessToken.setUser(dbUser);
            dbUser.setToken(accessToken);

            tokenRepository.saveAccessToken(dbUser, accessToken);
        }
        return accessToken;
    }



    @Override
    public void logout(String accessTokenValue) {
        AccessToken accessToken = tokenRepository.getAccessTokenByValue(accessTokenValue);
        if(accessToken != null) {
            tokenRepository.deleteAccessToken(accessToken);
        }
    }

    @Override
    public User getUserByAccessTokenValue(String accessTokenValue) {
        return userRepository.getUserByAccessToken(getAccessTokenByValue(accessTokenValue));
    }
}
