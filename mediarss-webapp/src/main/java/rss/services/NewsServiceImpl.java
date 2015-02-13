package rss.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.dao.NewsDao;
import rss.entities.News;
import rss.entities.User;
import rss.util.DateUtils;

import java.util.Collection;
import java.util.Date;

/**
 * User: dikmanm
 * Date: 13/02/2015 12:41
 */
@Service
public class NewsServiceImpl implements NewsService {

    @Autowired
    private NewsDao newsDao;

    @Override
    public Collection<News> getNews(User user) {
        Date newsDismiss = user.getNewsDismiss() == null ? DateUtils.getPastDate(9999) : user.getNewsDismiss();
        return newsDao.getNews(newsDismiss);
    }

    @Override
    public void createNews(News news) {
        newsDao.persist(news);
    }

    @Override
    public void dismissNews(User user) {
        user.setNewsDismiss(new Date());
    }
}
