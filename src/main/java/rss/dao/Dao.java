package rss.dao;

import java.util.List;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:29
 */
public interface Dao<T> {

	T merge(T t);

	void persist(T t);

	public T find(Long primaryKey);

	public List<T> findAll();

	void delete(T t);
}
