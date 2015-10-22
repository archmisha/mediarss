package rss.dao;

import org.springframework.stereotype.Repository;
import rss.ems.dao.BaseDaoJPA;
import rss.entities.News;
import rss.movies.dao.MovieImpl;
import rss.torrents.Movie;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 10/02/2015 15:03
 */
@Repository
public class NewsDaoImpl extends BaseDaoJPA<News> implements NewsDao {

    @Override
    protected Class<? extends News> getPersistentClass() {
        return News.class;
    }

    @Override
    public Collection<News> getNews(Date createdFrom) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("created", createdFrom);
        return super.findByNamedQueryAndNamedParams("News.findByCreated", params);
    }
}
