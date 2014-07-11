package be.kdg.spacecrack.model.game;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.authentication.Profile;
import be.kdg.spacecrack.model.game.gameturnstate.GameTurnState;
import be.kdg.spacecrack.services.IGameService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component("gameFactory")
public class HardCodedGameFactory implements GameFactory {
    @Override
    public Game createGame(SpaceCrackMap map, Profile userProfile, Profile opponentProfile, String gameName) {
        Game game = new Game();
        game.setGameTurnState(GameTurnState.NOTURNSENDED);

       List<Planet> planets = map.getPlanets();
        planets.forEach(planet -> {
            Game_Planet game_planet = new Game_Planet(planet);
            game.addGame_Planet(game_planet);
        });

        Player player1 = new Player();
        Player player2 = new Player();

        userProfile.addPlayer(player1);
        opponentProfile.addPlayer(player2);

        player1.setCommandPoints(IGameService.START_COMMAND_POINTS);
        player2.setCommandPoints(IGameService.START_COMMAND_POINTS);

        player1.setCrack(IGameService.PLAYER_START_CRACK);
        player2.setCrack(IGameService.PLAYER_START_CRACK);

        player1.setReadyToPlay(true);
        player2.setReadyToPlay(false);

        player1.setGame(game);
        player2.setGame(game);

        Optional<Game_Planet> game_planetAOptional = game.getGamePlanets().stream().filter(gp -> gp.getPlanet().getName().equals("a")).findFirst();
        Optional<Game_Planet> game_planetA3Optional = game.getGamePlanets().stream().filter(gp -> gp.getPlanet().getName().equals("a3")).findFirst();


        Game_Planet game_planetA = game_planetAOptional.get();
        Ship player1StartingShip = new Ship(game_planetA);
        Game_Planet game_planetA3 = game_planetA3Optional.get();
        Ship player2StartingShip = new Ship(game_planetA3);

        player1StartingShip.setStrength(IGameService.NEW_SHIP_STRENGTH);
        player2StartingShip.setStrength(IGameService.NEW_SHIP_STRENGTH);

        player1StartingShip.setPlayer(player1);
        player2StartingShip.setPlayer(player2);


        Colony player1StartingColony = new Colony(game_planetA);
        Colony player2StartingColony = new Colony(game_planetA3);

        player1StartingColony.setStrength(IGameService.NEW_COLONY_STRENGHT);
        player2StartingColony.setStrength(IGameService.NEW_COLONY_STRENGHT);

        player1StartingColony.setPlayer(player1);
        player2StartingColony.setPlayer(player2);

        game.setName(gameName);
        return game;
    }
}
