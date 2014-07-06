package be.kdg.spacecrack.model;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

//import org.codehaus.jackson.annotate.JsonIgnore;

import org.hibernate.annotations.*;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;

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

    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_planetId", nullable = false)
    private Game_Planet game_planet;

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

    @Deprecated
    public void setPlanet(Planet planet) {
        setGame_planet(player.getGame().getGame_PlanetByPlanet(planet));
    }
}
