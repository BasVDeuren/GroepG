package be.kdg.spacecrack.repositories;

import be.kdg.spacecrack.model.authentication.AccessToken;
import be.kdg.spacecrack.model.authentication.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Repository
public interface IUserRepository extends JpaRepository<User, Integer> {
    @Query("select u from User u where u.email = ?1 and u.password =?2")
    User getUser(String email, String password);

    @Query("select u from User u where u.username = ?1")
    User findUserByUsername(String username);

    @Query("select u from User u where u.token = ?1")
    User getUserByAccessToken(AccessToken accessToken);

    @Query("select u from User u where u.username LIKE CONCAT(?1,'%')")
    List<User> findUsersByUsernamePart(String usernamePart);

    @Query("select u from User u where u.email LIKE CONCAT(?1, '%')")
    List<User> findUsersByEmailPart(String emailPart);

    @Query("select u from User u where u.token IS NOT NULL")
    List<User> getLoggedInUsers();

    @Query("select u from User u where u.email = ?1")
    User getUserByEmail(String email);

    //Todo: Service logic in repository
    @Query("select u from User u where u.verificationToken = ?1 and u.verified = false")
    User findUserByVerificationTokenValue(String tokenValue);
}
