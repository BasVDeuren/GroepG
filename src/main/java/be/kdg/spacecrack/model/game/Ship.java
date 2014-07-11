package be.kdg.spacecrack.model.game;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */


import be.kdg.spacecrack.Exceptions.SpaceCrackNotAcceptableException;
import be.kdg.spacecrack.services.GameService;
import be.kdg.spacecrack.services.IGameService;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.List;
import java.util.Optional;

@Entity
@Audited
@Table(name = "T_Ship")
public class Ship extends Piece {
    @Id
    @GeneratedValue
    private int shipId;

    @Cascade(CascadeType.SAVE_UPDATE)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "playerId")
    private Player player;


    @Version
    private int versionNumber;

    public Ship() {
    }

    public Ship(Game_Planet game_planet) {
        setGame_planet(game_planet);
    }

    public void move(Planet destinationPlanet) {


        if(!destinationPlanet.isConnectedTo(game_planet.getPlanet())){
            throw new SpaceCrackNotAcceptableException("Invalid move, the planets are not connected!");
        }

        Game game = player.getGame();
        player.setCommandPoints(player.getCommandPoints() - GameService.MOVESHIPCOMMANDPOINTSCOST);
        List<Colony> colonies = game.getColonies();

        Optional<Colony> colonyOnPlanet = colonies.stream().filter(c -> c.isOnPlanet(destinationPlanet)).findFirst();
        if (colonyOnPlanet.isPresent()) {
            Colony colony = colonyOnPlanet.get();
            if (colony.getPlayer() == player) {
                moveToExistingColony(colony);
            } else {
                attackEnemyColony(colony);
            }
        } else {
            moveAndColonize(game.getGame_PlanetByPlanet(destinationPlanet));
        }

        game.checkLost();
    }

    private void attackEnemyColony(Colony colony) {

        Game_Planet game_planet = colony.getGame_planet();
        Ship enemyShip = game_planet.getShip();


        if (enemyShip != null) {
            Piece winner = fightPiece(enemyShip);
            if (winner == this) {
                attackEnemyColony(colony);
            }
            return;
        }


        Piece winner = fightPiece(colony);
        if (winner == this) {
            moveAndColonize(game_planet);
        }
    }

    private void moveToExistingColony(Colony colony) {
        Planet planet = colony.getGame_planet().getPlanet();
        List<Ship> ships = player.getShips();
        Optional<Ship> shipToMergeWith = ships.stream().filter(s -> s.isOnPlanet(planet)).findFirst();

        if (!shipToMergeWith.isPresent()) {
            game_planet = colony.getGame_planet();
        } else {
            merge(shipToMergeWith.get());
        }
    }

    private void moveAndColonize(Game_Planet destinationPlanet) {
        if (player.getCommandPoints() < IGameService.CREATECOLONYCOST) {
            throw new SpaceCrackNotAcceptableException("Insufficient CommandPoints");
        }

        player.colonizePlanet(destinationPlanet);
        game_planet.internalSetShip(null);
        setGame_planet(destinationPlanet);
        player.setCommandPoints(player.getCommandPoints() - IGameService.CREATECOLONYCOST);
    }

    private void merge(Ship shipToMergeWith) {
        strength = shipToMergeWith.strength + strength;
        shipToMergeWith.kill();
    }


    public int getShipId() {
        return shipId;
    }

    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        player.internalAddShip(this);
    }

    protected void internalSetPlayer(Player player) {
        this.player = player;
    }

    public Game_Planet getGame_planet() {
        return game_planet;
    }

    public void setGame_planet(Game_Planet game_planet) {
        game_planet.internalSetShip(this);
        this.game_planet = game_planet;
    }

    public void internalSetGame_Planet(Game_Planet game_planet) {
        this.game_planet = game_planet;
    }

    @Deprecated
    public void setPlanet(Planet planet) {
        setGame_planet(player.getGame().getGame_PlanetByPlanet(planet));
    }

    protected void kill() {
        game_planet.removeShip();
        player.removeShip(this);
    }
}
