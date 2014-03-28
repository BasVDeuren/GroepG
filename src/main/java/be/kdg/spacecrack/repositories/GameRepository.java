package be.kdg.spacecrack.repositories;/* Git $Id
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.Game;
import be.kdg.spacecrack.model.Profile;
import org.hibernate.LockOptions;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("gameRepository")
public class GameRepository implements IGameRepository {
    @SuppressWarnings("SpringJavaAutowiringInspection")
    @Autowired
    private SessionFactory sessionFactory;

    public GameRepository() {
    }

    public GameRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public int createOrUpdateGame(Game game) {

        Session session = sessionFactory.getCurrentSession();
        session.saveOrUpdate(game);

        return game.getGameId();
    }

    @Override
    public int updateGame(Game game) {
        return createOrUpdateGame(game);
    }

    @Override
    public List<Game> getGamesByProfile(Profile profile) {
        Session session = sessionFactory.getCurrentSession();
        List<Game> games;

        @SuppressWarnings("JpaQlInspection") Query query = session.createQuery("FROM Game game WHERE game.gameId in (SELECT player.game.gameId FROM Player player where player.profile = :profile)");
        query.setParameter("profile", profile);

        games = (List<Game>) query.list();

        return games;
    }

    @Override
    public Game getGameByGameId(int gameId) {
        Session session = sessionFactory.getCurrentSession();
        Game game;

        @SuppressWarnings("JpaQlInspection") Query q = session.createQuery("from Game g where g.gameId = :gameId");
        q.setParameter("gameId", gameId);
        game = (Game) q.uniqueResult();

        return game;
    }


    @Override
    public Game getGameRevision(Number number, int gameId) {
        AuditReader reader = AuditReaderFactory.get(sessionFactory.getCurrentSession());
        return reader.find(Game.class, gameId, number);
    }

    @Override
    public List<Integer> getRevisionNumbers(int gameId) {
        AuditReader reader = AuditReaderFactory.get(sessionFactory.getCurrentSession());
        List<Number> revisions = reader.getRevisions(Game.class, gameId);
        List<Integer> revisionsIntegers = new ArrayList<Integer>();
        for (int i = 0; i < revisions.size(); i++) {
            revisionsIntegers.add((Integer) revisions.get(i));
        }

        return revisionsIntegers;
    }

    @Override
    public Game getGameForUpdate(Game game) {
        Session session = sessionFactory.getCurrentSession();


        @SuppressWarnings("JpaQlInspection") Query q = session.createQuery("from Game g where g.gameId = :gameId");
        q.setParameter("gameId", game.getGameId());
        q.setLockOptions(LockOptions.UPGRADE);
        game = (Game) q.uniqueResult();

        return game;
    }

    @Override
    public boolean updateGameOptimisticConcurrent(Game game, Integer actionNumber) {
        Session session = sessionFactory.getCurrentSession();
        //noinspection JpaQlInspection
        Query query = session.createQuery(" from Game g where g.gameId = :gameId and g.actionNumber = :oldActionNumber  ");
        query.setParameter("oldActionNumber", actionNumber);
        query.setParameter("gameId", game.getGameId());
        List resultObject = query.list();
        Integer result = resultObject.size();
    if(result == 0 )
    {
        return false;
    }else{

        session.update(game);
    }

    return true;
}

    @Override
    public void deleteGame(int gameId) {
        Session session = sessionFactory.getCurrentSession();

        @SuppressWarnings("JpaQlInspection") Query q = session.createQuery("from Game g where g.gameId = :id");
        q.setParameter("id", gameId);
        Game game = (Game) q.uniqueResult();
        session.delete(game);
    }
}
