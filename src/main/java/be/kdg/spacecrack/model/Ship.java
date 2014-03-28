package be.kdg.spacecrack.model;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

//import org.codehaus.jackson.annotate.JsonIgnore;

import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Audited
@Table(name = "T_Ship")
public class Ship extends Piece {
    @Id
    @GeneratedValue
    private int shipId;

    @ManyToOne(fetch = FetchType.EAGER )
    @JoinColumn(name = "playerId")
    private Player player;

    @ManyToOne(fetch= FetchType.EAGER)
    @JoinColumn(name = "planetId")
    private Planet planet;


    @Version
    private int versionNumber;

    public int getShipId() {
        return shipId;
    }

    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    public Ship() {}

    public Ship(Planet planet) {
        this.planet = planet;
    }

    public Player getPlayer() {
        return player;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    public Planet getPlanet() {
        return planet;
    }

    public void setPlayer(Player player) {
        this.player = player;
        player.internalAddShip(this);
    }

    protected void internalSetPlayer(Player player) {
        this.player = player;
    }
}
