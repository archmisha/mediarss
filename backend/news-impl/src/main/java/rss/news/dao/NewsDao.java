package rss.news.dao;

import rss.ems.dao.Dao;
import rss.news.News;

import java.util.Collection;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 10/02/2015 13:42
 */
public interface NewsDao extends Dao<News> {
    Collection<News> getNews(Date createdFrom);
}
