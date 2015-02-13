package rss.dao;

import rss.entities.News;

import java.util.Collection;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 10/02/2015 13:42
 */
public interface NewsDao extends Dao<News> {
    Collection<News> getNews(Date createdFrom);
}
