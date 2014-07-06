package be.kdg.spacecrack.repositories;

import be.kdg.spacecrack.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Repository
public interface ITokenRepository extends JpaRepository<AccessToken, Integer> {

    @Query("Select accessToken from AccessToken accessToken where accessToken.value = ?1")
    AccessToken getAccessTokenByValue(String value);

;
}
