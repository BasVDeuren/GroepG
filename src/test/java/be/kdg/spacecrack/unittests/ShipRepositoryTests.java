package be.kdg.spacecrack.unittests;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.game.Game;
import be.kdg.spacecrack.model.game.Game_Planet;
import be.kdg.spacecrack.model.game.Player;
import be.kdg.spacecrack.model.game.Ship;
import be.kdg.spacecrack.repositories.IGameRepository;
import be.kdg.spacecrack.repositories.IShipRepository;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.Assert.assertNotNull;


public class ShipRepositoryTests extends BaseUnitTest {
    @Autowired
    private IShipRepository shipRepository;
    @Autowired
    private IGameRepository gameRepository;

    @Before
    public void setUp() throws Exception {

    }

    @Test
    @Transactional
    public void saveGameWithPlayerAndShip_valid_shipandplayerCreatedIn1Query() {
        //region Arrange
        Ship ship = new Ship();
        Game game = new Game();
        Game_Planet game_planet = new Game_Planet();
        game.addGame_Planet(game_planet);
        ship.setGame_planet(game_planet);
        Player player = new Player();

        //Create Player and add to game

        game.getPlayers().add(player);
        player.setGame(game);

        //Create Ship and add to player
        player.addShip(ship);
        //Create the game in the database
        int gameId = gameRepository.save(game).getId();
        Game gameDb = gameRepository.findOne(gameId);
        //endregion

        //region Act
         Ship result = shipRepository.findOne(gameDb.getPlayers().get(0).getShips().get(0).getShipId());
        //endregion

        //Assert
        assertNotNull(result);
        assertNotNull(result.getPlayer());
    }
}
