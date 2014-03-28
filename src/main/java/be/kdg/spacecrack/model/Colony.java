package be.kdg.spacecrack.model;/* Git $Id
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
@Table(name = "T_Colony", uniqueConstraints = {@UniqueConstraint(name = "colony_game_planetUnique", columnNames = {"colonyId","planetId", "playerId"})})
public class Colony extends Piece {
    @Id
    @GeneratedValue
    private int colonyId;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "planetId")
    private Planet planet;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "playerId")
    private Player player;

    @Version
    private int versionNumber;

    public Colony() {}

    public Colony(Planet planet, Player player, int strenght) {
        this.planet = planet;
        setPlayer(player);
        setStrength(strenght);
    }

    public Colony(Planet planet) {
        this.planet = planet;
    }

    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    public int getColonyId() {
        return colonyId;
    }

    public void setColonyId(int colonyId) {
        this.colonyId = colonyId;
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
}
