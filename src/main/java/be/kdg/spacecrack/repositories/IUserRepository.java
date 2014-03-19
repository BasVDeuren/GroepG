package be.kdg.spacecrack.repositories;

import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.User;

import java.util.List;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
public interface IUserRepository {

    User getUser(User user);

    @Deprecated
    User addUser(String username, String password, String email);

    User getUserByUsername(String username);

    void updateUser(User user);

    void createUser(User user);

    User getUserByAccessToken(AccessToken accessToken);

    List<User> findUsersByUsernamePart(String username);

    List<User> findUsersByEmailPart(String username) ;

    List<User> getLoggedInUsers();


    User getUserByEmail(String email);

    User findUserByVerificationTokenValue(String tokenValue);
}
