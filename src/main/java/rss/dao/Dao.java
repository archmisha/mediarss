package rss.dao;

import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:29
 */
public interface Dao<T> {

	T merge(T t);

	void persist(T t);

	T find(Long primaryKey);

	Collection<T> find(Set<Long> ids);

	List<T> findAll();

	void delete(T t);
}
