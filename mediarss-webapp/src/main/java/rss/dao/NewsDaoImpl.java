package rss.dao;

import org.springframework.stereotype.Repository;
import rss.entities.News;

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
    public Collection<News> getNews(Date createdFrom) {
        Map<String, Object> params = new HashMap<>(1);
        params.put("created", createdFrom);
        return super.findByNamedQueryAndNamedParams("News.findByCreated", params);
    }
}
