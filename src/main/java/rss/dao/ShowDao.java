package rss.dao;

import rss.entities.Show;

import java.util.Collection;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface ShowDao extends Dao<Show> {

    Show findByName(String name);

	Collection<Show> findNotEnded();
}
