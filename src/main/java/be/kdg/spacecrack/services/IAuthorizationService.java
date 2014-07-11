package be.kdg.spacecrack.services;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.authentication.AccessToken;
import be.kdg.spacecrack.model.authentication.User;

public interface IAuthorizationService {
    public AccessToken getAccessTokenByValue(String accessTokenValue);


    AccessToken login(User user);

    void logout(String accessTokenValue);

    User getUserByAccessTokenValue(String accessTokenValue);

    String getMD5HashedPassword(String testPassword);

    void createTestUser(String s, String test, String hashedPw, String jack, String black);
}
