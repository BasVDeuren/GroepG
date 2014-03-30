package be.kdg.spacecrack.repositories;

import be.kdg.spacecrack.model.AccessToken;
import be.kdg.spacecrack.model.User;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */
@Component("tokenRepository")
public class TokenRepository implements ITokenRepository {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SessionFactory sessionFactory;

    public TokenRepository() {}

    public TokenRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public AccessToken getAccessTokenByValue(String value) {
        Session session = sessionFactory.getCurrentSession();

        @SuppressWarnings("JpaQlInspection") Query q = session.createQuery("from AccessToken a where a.value = :value");
        q.setParameter("value", value);

        AccessToken accessToken = (AccessToken) q.uniqueResult();
        return accessToken;
    }


    @Override
    public void saveAccessToken(User dbUser, AccessToken accessToken) {
        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(accessToken);
        session.saveOrUpdate(dbUser);
    }

    @Override
    public void deleteAccessToken(AccessToken accessToken) {
        Session session = sessionFactory.getCurrentSession();

        @SuppressWarnings("JpaQlInspection") Query q = session.createQuery("from AccessToken a where a.accessTokenId = :id and a.value = :value");
        q.setParameter("id", accessToken.getAccessTokenId());
        q.setParameter("value", accessToken.getValue());
        AccessToken dbAccessToken = (AccessToken) q.uniqueResult();
        if (dbAccessToken != null) {
            dbAccessToken.getUser().setToken(null);
            session.delete(dbAccessToken);
        }
    }
}
