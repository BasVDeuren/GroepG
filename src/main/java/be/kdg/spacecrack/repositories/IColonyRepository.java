package be.kdg.spacecrack.repositories;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Colony;
import be.kdg.spacecrack.model.Game;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IColonyRepository extends JpaRepository<Colony, Integer> {

    @Query("select c from Colony c where c.player.game = ?1")
    List<Colony> findColoniesByGame(Game game);

}
