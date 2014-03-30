package be.kdg.spacecrack.model;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import org.hibernate.envers.Audited;

import javax.persistence.*;

@Entity
@Table(name = "T_Game_Planet")
@Audited
public class Game_Planet {
    @Id
    @GeneratedValue
    private int game_PlanetId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn( name = "planetId")
    private Planet planet;

    @ManyToOne(cascade = CascadeType.ALL)
    private Game game;


    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER,mappedBy = "game_planet", optional = true)
    private Ship ship;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER,mappedBy = "game_planet", optional = true)
    private Colony colony;

    @Version
    private int versionNumber;

    public Game_Planet() {
    }

    public Game_Planet(Planet planetA) {
        setPlanet(planetA);
    }


    public int getGame_PlanetId() {
        return game_PlanetId;
    }

    public void setGame_PlanetId(int game_PlanetId) {
        this.game_PlanetId = game_PlanetId;
    }

    public Planet getPlanet() {
        return planet;
    }

    public void setPlanet(Planet planet) {
        this.planet = planet;
    }

    public Game getGame() {
        return game;
    }

    public void setGame(Game game) {
        game.internalAddGame_Planet(this);
        this.game = game;
    }

    public Ship getShip() {
        return ship;
    }

    public void setShip(Ship ship) {
        ship.internalSetGame_Planet(this);
        this.ship = ship;
    }

    public Colony getColony() {
        return colony;
    }

    public void setColony(Colony colony) {
        colony.internalSetGame_Planet(this);
        this.colony = colony;
    }

    protected void internalSetColony(Colony colony) {
        this.colony = colony;
    }

    protected void internalSetShip(Ship ship) {
        this.ship = ship;
    }

    protected void internalSetGame(Game game) {
        this.game = game;
    }

    public void removeShip() {
        internalSetShip(null);
    }

    public void removeColony() {
        internalSetColony(null);
    }
}
