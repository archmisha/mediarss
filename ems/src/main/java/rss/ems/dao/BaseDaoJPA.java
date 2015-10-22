package rss.ems.dao;


import org.hibernate.NonUniqueResultException;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;


/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:29
 */
public abstract class BaseDaoJPA<T> implements Dao<T> {

	@PersistenceContext
	protected EntityManager em;

//	protected Class<T> persistentClass;

//	@SuppressWarnings("unchecked")
//	@PostConstruct
//	protected void detectPersistentClass() {
//		// the following detects the managed entity for this DAO even if the DAO class extends another DAO in which
//		// the actual parametrized type is registered
//		Class<?> c = getClass();
//		while (c != Object.class) {
//			if (c.getGenericSuperclass() instanceof ParameterizedType) {
//				persistentClass = (Class<T>) ((ParameterizedType) c.getGenericSuperclass()).getActualTypeArguments()[0];
//				break;
//			}
//			c = c.getSuperclass();
//		}
//
//		if (persistentClass == null) {
//			throw new IllegalStateException("unable to detect DAO managed entity class");
//		}
//	}

	protected abstract Class<? extends T> getPersistentClass();

	public final int bulkUpdate(final String queryString, final Object... values) {
		Query q = em.createQuery(queryString);
		for (int i = 0; i < values.length; ++i) {
			q.setParameter(i + 1, values[i]);
		}
		return q.executeUpdate();
	}

	@SuppressWarnings("unchecked")
	protected final <E> List<E> find(final String ejbqlString, final Object... values) {
		Query queryObject = em.createQuery(ejbqlString);
		setParameters(queryObject, values);
		return queryObject.getResultList();
	}

	@SuppressWarnings({"unchecked"})
	protected final <E> List<E> findByNativeQuery(Class clazz, final String ejbqlString, final Object... values) {
		Query queryObject = em.createNativeQuery(ejbqlString, clazz);
		setParameters(queryObject, values);
		return queryObject.getResultList();
	}

	private Query setParameters(Query queryObject, final Object... values) {
		if (values != null) {
			for (int i = 0; i < values.length; i++) {
				queryObject.setParameter("p" + i, values[i]);
			}
		}
		return queryObject;
	}

	@Override
	public void persist(T entity) {
		em.persist(entity);
	}

	@Override
	public T merge(T entity) {
		return em.merge(entity);
	}

	@Override
	public final T find(Long primaryKey) {
		if (primaryKey == null) {
			throw new IllegalArgumentException("primaryKey is null");
		}
		return em.find(getPersistentClass(), primaryKey);
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public Collection<T> find(Collection<Long> ids) {
		if (ids.isEmpty()) {
			return Collections.emptyList();
		}
		Query queryObject = em.createQuery("select e from " + getPersistentClass().getName() + " as e where e.id in (:ids)");
		queryObject.setParameter("ids", ids);
		return queryObject.getResultList();
	}

	@SuppressWarnings({"unchecked"})
	@Override
	public final List<T> findAll() {
		Query queryObject = em.createQuery("select e from " + getPersistentClass().getName() + " as e");
		return queryObject.getResultList();
	}

	@Override
	public void delete(T t) {
		em.remove(t);
	}

	@SuppressWarnings({"unchecked"})
	public <E> List<E> findByNamedQueryAndNamedParams(String queryName, Map<String, ?> params) {
		return findByNamedQueryAndNamedParams(queryName, params, -1);
	}

	@SuppressWarnings("unchecked")
	public <E> List<E> findByNamedQueryAndNamedParams(String queryName, Map<String, ?> params, int maxResults) {
		Query queryObject = em.createNamedQuery(queryName);
		if (params != null) {
			for (Map.Entry<String, ?> entry : params.entrySet()) {
				queryObject.setParameter(entry.getKey(), entry.getValue());
			}
		}

		if (maxResults != -1) {
			queryObject.setMaxResults(maxResults);
		}

		return queryObject.getResultList();
	}

	protected final <E> E uniqueResult(List<E> list) throws NonUniqueResultException {
		if (list.isEmpty()) {
			return null;
		}

		E first = list.get(0);
		for (int i = 1; i < list.size(); i++) {
			if (!list.get(i).equals(first)) {
				throw new NonUniqueResultException(list.size());
			}
		}
		return first;
	}

	protected String generateQuestionMarks(int size, int counter) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < size; ++i) {
			sb.append(":p").append(counter++).append(",");
		}
		sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}
}
