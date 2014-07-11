package be.kdg.spacecrack.model.authentication;

//import org.codehaus.jackson.annotate.JsonManagedReference;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.io.Serializable;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Entity
@Table(name = "T_User")
public class User implements Serializable {
    @Id
    @GeneratedValue
    private int userId;

    @Column(unique = true)
    private String username;

    @Column
    private String password;

    @Column(unique = true)
    private String email;

    @Column
    private boolean verified;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "accesstokenid", nullable = true)
    @JsonManagedReference
    private AccessToken token;


    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name= "profileId", nullable = true)
    @JsonManagedReference
    private Profile profile;
    private String verificationToken;

    public User() {}


    public User(String username, String pw, String email, boolean verified) {
        this.username = username;
        this.password = pw;
        this.email = email;
        this.verified = verified;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public AccessToken getToken() {
        return token;
    }

    public String getEmail() {
        return email;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setToken(AccessToken token) {
        this.token = token;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public String getVerificationToken() {
        return verificationToken;
    }

    public void setVerificationToken(String verificationToken) {
        this.verificationToken = verificationToken;
    }
}
