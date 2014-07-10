package be.kdg.spacecrack.model;

//import org.codehaus.jackson.annotate.JsonIgnore;
//import org.codehaus.jackson.annotate.JsonProperty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.*;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Entity
@Audited
@Table(name = "T_Planet")
public class Planet {
    @GeneratedValue
    @Id
    private int planetId;

    @Column(unique = true)
    private String name;

    @Column
    private int x;

    @Column
    private int y;

    @JsonIgnore
    @OneToMany(fetch=FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<PlanetConnection> planetConnections;

    @OneToMany
    private Set<Colony> colonies;

    @OneToMany
    private Set<Ship> ships;

    public Planet(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
        planetConnections = new HashSet<PlanetConnection>();
    }

    public Planet() {
        planetConnections = new HashSet<PlanetConnection>();
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getPlanetId() {
        return planetId;
    }

    public void setPlanetId(int planetId) {
        this.planetId = planetId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setPlanetConnections(Set<PlanetConnection> planetConnections) {
        this.planetConnections = planetConnections;
    }

    public Set<PlanetConnection> getPlanetConnections() {
        return planetConnections;
    }

    public void addConnection(PlanetConnection planetConnection) {
        planetConnections.add(planetConnection);
    }

    @JsonProperty("connectedPlanets")
    public List<Planet> getConnectedPlanetWraps() {
        List<Planet> connectedPlanetWraps= new ArrayList<Planet>();

        for (PlanetConnection planetConnection: planetConnections) {
            Planet p = planetConnection.getChildPlanet();
            connectedPlanetWraps.add(new Planet(p.name, p.x, p.y));
        }

        return connectedPlanetWraps;
    }

    public String getName() {
        return name;
    }

    public void removeConnectionToPlanet(Planet planet) {
        Iterator iterator = planetConnections.iterator();
        while(iterator.hasNext()) {
            PlanetConnection connection = (PlanetConnection) iterator.next();
            if(connection.getChildPlanet() == planet) {
                iterator.remove();
            }
        }
    }

    public PlanetConnection getConnectionToPlanet(Planet planet) {
        Iterator iterator = planetConnections.iterator();
        while(iterator.hasNext()) {
            PlanetConnection connection = (PlanetConnection) iterator.next();
            if(connection.getChildPlanet() == planet) {
                return connection;
            }
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Planet)) return false;

        Planet planet = (Planet) o;

        if (!name.equals(planet.name)) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
