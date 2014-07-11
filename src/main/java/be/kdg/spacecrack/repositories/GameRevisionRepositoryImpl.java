package be.kdg.spacecrack.repositories;/* Git $Id$
 *
 * Project Application Development
 * Karel de Grote-Hogeschool
 * 2013-2014
 *
 */

import be.kdg.spacecrack.model.game.Game;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.persistence.EntityManagerFactory;
import java.util.ArrayList;
import java.util.List;

@Component(value = "gameRevisionRepository")
public class GameRevisionRepositoryImpl implements GameRevisionRepository {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Override
    public Game getGameRevision(Number number, int gameId) {
        AuditReader reader = AuditReaderFactory.get(entityManagerFactory.createEntityManager());
        return reader.find(Game.class, gameId, number);
    }

    @Override
    public List<Integer> getRevisionNumbers(int gameId) {
        AuditReader reader = AuditReaderFactory.get(entityManagerFactory.createEntityManager());
        List<Number> revisions = reader.getRevisions(Game.class, gameId);
        List<Integer> revisionsIntegers = new ArrayList<>();
        for (Number revisionNumbers : revisions) {
            revisionsIntegers.add((Integer) revisionNumbers);
        }

        return revisionsIntegers;
    }

}
