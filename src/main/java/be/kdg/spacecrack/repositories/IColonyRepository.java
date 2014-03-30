package be.kdg.spacecrack.repositories;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Colony;
import be.kdg.spacecrack.model.Game;

import java.util.List;

public interface IColonyRepository {


    List<Colony> getColoniesByGame(Game game);

    Colony getColonyById(Integer colonyId);


    void delete(Colony colony);
}
