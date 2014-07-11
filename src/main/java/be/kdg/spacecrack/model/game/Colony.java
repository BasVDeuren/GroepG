package be.kdg.spacecrack.model.game;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

//import org.codehaus.jackson.annotate.JsonIgnore;

import be.kdg.spacecrack.services.IGameService;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Audited
@Table(name = "T_Colony")
public class Colony extends Piece {
    @Id
    @GeneratedValue
    private int id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "playerId")
    private Player player;

    @Version
    private int versionNumber;

    public Colony() {}

    public Colony(Game_Planet game_planet, Player player, int strenght) {
        setGame_planet(game_planet);
        setPlayer(player);
        setStrength(strenght);
    }

    public Colony(Game_Planet game_planet) {
       setGame_planet(game_planet);
    }

    protected void kill() {
        game_planet.removeColony();
        player.removeColony(this);
    }

    public void buildShip() {
        Ship shipOnPlanet =  game_planet.getShip();

        if (shipOnPlanet == null) {
            Ship ship = new Ship();
            ship.setStrength(IGameService.NEW_SHIP_STRENGTH);
            ship.setPlayer(player);
            ship.setGame_planet(game_planet);
        } else {
            shipOnPlanet.setStrength(shipOnPlanet.getStrength() + IGameService.NEW_SHIP_STRENGTH);
        }

        player.setCommandPoints(player.getCommandPoints() - IGameService.BUILDSHIP_COST);
        player.setCrack(player.getCrack() - IGameService.BUILDSHIP_CRACK_COST);

    }


    public int getId() {
        return id;
    }

    public void setId(int colonyId) {
        this.id = colonyId;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlayer(Player player) {
        this.player = player;
        player.internalAddColony(this);
    }

    protected void internalSetPlayer(Player player){
       this.player = player;
    }

    public Game_Planet getGame_planet() {
        return game_planet;
    }

    public void setGame_planet(Game_Planet game_planet) {
        game_planet.internalSetColony(this);
        this.game_planet = game_planet;
    }

    public void internalSetGame_Planet(Game_Planet game_planet) {
        this.game_planet = game_planet;
    }


    public void setPlanet(Planet planet) {
        setGame_planet(player.getGame().getGame_PlanetByPlanet(planet));
    }


}
