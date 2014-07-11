package be.kdg.spacecrack.model.game;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;

@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table
public class PlanetConnection {
    @Id
    @GeneratedValue
    private int planetConnectionId;

    @ManyToOne
    @JoinColumn(name="parentPlanetId")
    private Planet parentPlanet;

    @ManyToOne
    @JoinColumn(name="childPlanetId")
    private Planet childPlanet;

    public PlanetConnection() {}

    public PlanetConnection(Planet parentPlanet, Planet childPlanet) {
        this.parentPlanet = parentPlanet;
        this.childPlanet = childPlanet;
    }

    public int getPlanetConnectionId() {
        return planetConnectionId;
    }

    public void setPlanetConnectionId(int planetConnectionId) {
        this.planetConnectionId = planetConnectionId;
    }

    public Planet getParentPlanet() {
        return parentPlanet;
    }

    public void setParentPlanet(Planet parentPlanet) {
        this.parentPlanet = parentPlanet;
    }

    public Planet getChildPlanet() {
        return childPlanet;
    }

    public void setChildPlanet(Planet childPlanet) {
        this.childPlanet = childPlanet;
    }

    @Override
    public String toString() {
        return "PlanetConnection{" +
                "planetConnectionId=" + planetConnectionId +
                ", parentPlanet=" + parentPlanet +
                ", childPlanet=" + childPlanet +
                '}';
    }
}
