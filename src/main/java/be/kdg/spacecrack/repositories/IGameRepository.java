package be.kdg.spacecrack.repositories;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.model.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IGameRepository extends JpaRepository<Game, Integer> {

    @Query("select game FROM Game game WHERE game.id in (SELECT player.game.id FROM Player player where player.profile = ?1)")
    List<Game> getGamesByProfile(Profile profile);

    @Query("select g from Game g where g.id = ?1 and g.actionNumber = ?2")
    Game getGameOptimisticConcurrent(Integer id, Integer actionNumber);

}
