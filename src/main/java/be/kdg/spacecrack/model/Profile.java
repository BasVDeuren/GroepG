package be.kdg.spacecrack.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Type;
import org.hibernate.envers.Audited;
import org.hibernate.envers.RelationTargetAuditMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Entity
@Audited(targetAuditMode = RelationTargetAuditMode.NOT_AUDITED)
@Table(name = "T_Profile")
public class Profile {
    @Id
    @GeneratedValue
    private int profileId;

    @Column
    private String firstname;

    @Column
    private String lastname;

    @Column
    private Date dayOfBirth;

    @Column
    @Type(type="text")
    private String image;


    @Cascade(value = CascadeType.ALL)
    @OneToOne(mappedBy = "profile")
    @JsonBackReference
    private User user;

    @OneToMany(fetch = FetchType.EAGER,mappedBy = "profile")
    @JsonIgnore
    private List<Player> players = new ArrayList<Player>();


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Profile() {
    }

    public Profile(String firstname, String lastname,Date dayOfBirth, String image) {

        this.firstname = firstname;
        this.lastname = lastname;
        this.dayOfBirth = dayOfBirth;
        this.image = image;
        players = new ArrayList<Player>();
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }


    public Date getDayOfBirth() {
        return dayOfBirth;
    }

    public void setDayOfBirth(Date dayOfBirth) {
        this.dayOfBirth = dayOfBirth;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }



    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }


    public List<Player> getPlayers() {
        return players;
    }

    public void addPlayer(Player player) {

        players.add(player);
        player.internalSetProfile(this);
    }

    public void setPlayers(List<Player> players) {
        this.players = players;
    }

    public void internalAddPlayer(Player player) {
        players.add(player);
    }
}
