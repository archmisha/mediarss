package rss.ems.dao;

import java.util.Collection;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:29
 */
public interface Dao<T> {

	T merge(T t);

	void persist(T t);

	T find(Long primaryKey);

	Collection<T> find(Collection<Long> ids);

	List<T> findAll();

	void delete(T t);
}
