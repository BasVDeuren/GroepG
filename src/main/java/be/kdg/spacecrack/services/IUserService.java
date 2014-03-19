package be.kdg.spacecrack.services;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.User;

import java.util.List;

public interface IUserService {
    public User getUserByAccessToken(AccessToken accessToken) throws Exception;

    User getUserByUsername(String username) throws Exception;

    void registerUser(String username, String password, String email) throws Exception;

    void updateUser(User user);

    List<User> getUsersByString(String username) throws Exception;

    List<User> getUsersByEmail(String tom) throws Exception;

    User getRandomUser(int UserId) throws Exception;

    void verifyUser(String tokenValue);
}
