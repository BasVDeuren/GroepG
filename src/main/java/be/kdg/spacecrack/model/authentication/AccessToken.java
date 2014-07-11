package be.kdg.spacecrack.model.authentication;


import com.fasterxml.jackson.annotation.JsonBackReference;

import javax.persistence.*;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Entity
@Table(name = "T_AccessToken")
public class AccessToken {
    @GeneratedValue
    @Id
    private int accessTokenId;

    @Column
    private String value;

    /*@OneToOne
    @JoinColumn(name="userId")*/
    @OneToOne(mappedBy = "token")
    @JsonBackReference
    User user;
    public AccessToken() {
    }

    public AccessToken(String value) {
        this.value = value;
    }

    public int getAccessTokenId() {
        return accessTokenId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getValue() {
        return value;
    }

    public void setAccessTokenId(int accessTokenId) {
        this.accessTokenId = accessTokenId;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
