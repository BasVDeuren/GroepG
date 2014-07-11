package be.kdg.spacecrack.model.game;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import org.hibernate.annotations.Cascade;
import org.hibernate.envers.Audited;

import javax.persistence.*;

@Audited
@MappedSuperclass
public abstract class Piece {
    @Cascade(org.hibernate.annotations.CascadeType.SAVE_UPDATE)
    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "game_planetId")
    protected Game_Planet game_planet;
    @Column
    protected int strength;

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }


    public boolean isOnPlanet(Planet destinationPlanet) {
        return destinationPlanet.equals(game_planet.getPlanet());
    }

    protected abstract void kill() ;


    protected Piece fightPiece( Piece enemyPiece) {
        int strengthDifference = strength - enemyPiece.strength;
        if (strengthDifference < 0) {
            kill();
            enemyPiece.strength =-strengthDifference;
            return enemyPiece;
        } else if (strengthDifference > 0) {
            enemyPiece.kill();
            strength = strengthDifference;
            return this;
        } else {
            kill();
            enemyPiece.kill();
            return null;
        }
    }
}
