package be.kdg.spacecrack.model.authentication;

import be.kdg.spacecrack.model.game.Player;
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

    @Cascade(value = CascadeType.SAVE_UPDATE)
    @OneToOne(mappedBy = "profile")
    @JsonBackReference
    private User user;

    @OneToMany(fetch = FetchType.LAZY,mappedBy = "profile")
    @JsonIgnore
    private List<Player> players = new ArrayList<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;

    }

    public Profile() {}

    public Profile(String firstname, String lastname,Date dayOfBirth, String image) {

        this.firstname = firstname;
        this.lastname = lastname;
        this.dayOfBirth = dayOfBirth;
        this.image = image;
        players = new ArrayList<>();
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Profile)) return false;

        Profile profile = (Profile) o;

        if (dayOfBirth != null ? !dayOfBirth.equals(profile.dayOfBirth) : profile.dayOfBirth != null) return false;
        if (firstname != null ? !firstname.equals(profile.firstname) : profile.firstname != null) return false;
        if (lastname != null ? !lastname.equals(profile.lastname) : profile.lastname != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = firstname != null ? firstname.hashCode() : 0;
        result = 31 * result + (lastname != null ? lastname.hashCode() : 0);
        result = 31 * result + (dayOfBirth != null ? dayOfBirth.hashCode() : 0);
        return result;
    }
}
