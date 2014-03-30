package be.kdg.spacecrack.model;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

//import org.codehaus.jackson.annotate.JsonIgnore;

import org.hibernate.annotations.*;
import org.hibernate.annotations.CascadeType;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

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


    @Cascade(CascadeType.SAVE_UPDATE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_planetId", nullable = false)
    private Game_Planet game_planet;

    @Version
    private int versionNumber;

    public Ship() {}

    public Ship(Game_Planet game_planet) {
        setGame_planet(game_planet);
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
}
