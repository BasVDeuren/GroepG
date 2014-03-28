package be.kdg.spacecrack.model;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Audited
@Table(name = "T_Game")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY )
    private int gameId;

    @Column
    private String name;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "game")
    private List<Player> players = new ArrayList<Player>();

    @Version
    @Column
    private int versionNumber;
    @Column
    private int actionNumber;

    @Column
    private int loserPlayerId;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Game() {
       players = new ArrayList<Player>();
    }

    public int getGameId() {
        return gameId;
    }

    public void setGameId(int gameId) {
        this.gameId = gameId;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void addPlayer(Player player) {
        players.add(player);
        player.internalSetGame(this);
    }

    protected void internalAddPlayer(Player player) {
        players.add(player);
    }

    public int getLoserPlayerId() {
        return loserPlayerId;
    }

    public void setLoserPlayerId(int winnerPlayerId) {
        this.loserPlayerId = winnerPlayerId;
    }

    public int getActionNumber() {
        return actionNumber;
    }

    public void setActionNumber(int actionNumber) {
        this.actionNumber = actionNumber;
    }


    public void incrementActionNumber() {

        actionNumber++;
    }
}
