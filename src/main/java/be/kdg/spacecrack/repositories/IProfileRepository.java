package be.kdg.spacecrack.repositories;

import be.kdg.spacecrack.model.authentication.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Repository
public interface IProfileRepository extends JpaRepository<Profile, Integer>{


}
