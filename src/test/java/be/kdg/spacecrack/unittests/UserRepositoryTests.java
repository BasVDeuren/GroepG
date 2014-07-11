package be.kdg.spacecrack.unittests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.authentication.User;
import be.kdg.spacecrack.repositories.IUserRepository;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class UserRepositoryTests  extends BaseUnitTest{
    @Autowired
    protected IUserRepository userRepository;
    @Test
    public void getUserByVerificationToken_validTokenUserNotVerified_user() throws Exception {
        User user = new User("zotten", "beer", "zotten.beer@gmail.com", false);
        String token = "testToken123";
        user.setVerificationToken(token);
        userRepository.save(user);
        User result = userRepository.findUserByVerificationTokenValue(token);
        assertNotNull(result);

    }

    @Test
    public void getUserByVerificationToken_validTokenUserVerified_null() throws Exception {
        User user = new User("zotten", "beer", "zotten.beer@gmail.com", true);
        String token = "testToken123";
        user.setVerificationToken(token);
        userRepository.save(user);
        User result = userRepository.findUserByVerificationTokenValue(token);
        assertEquals(null, result);

    }

    @Test
    public void getUserByVerificationToken_invalidTokenUserVerified_null() throws Exception {
        User user = new User("zotten", "beer", "zotten.beer@gmail.com", true);
        String token = "testToken123";
        user.setVerificationToken(token);
        userRepository.save(user);
        User result = userRepository.findUserByVerificationTokenValue("invalidToken");
        assertEquals(null, result);

    }

    @Test
    public void getUserByVerificationToken_invalidTokenUserNotVerified_null() throws Exception {

        User user = new User("zotten", "beer", "zotten.beer@gmail.com", false);
        String token = "testToken123";
        user.setVerificationToken(token);
        userRepository.save(user);
        User result =userRepository.findUserByVerificationTokenValue("invalidToken");
        assertEquals(null, result);

    }
}
