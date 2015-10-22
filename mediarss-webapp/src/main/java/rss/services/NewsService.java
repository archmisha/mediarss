package rss.services;

import rss.entities.News;
import rss.user.User;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 13/02/2015 12:41
 */
public interface NewsService {
    Collection<News> getNews(User user);

    void createNews(News news);

    void dismissNews(User user);
}
