package rss.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.cache.UserCacheService;
import rss.news.dao.NewsDao;
import rss.user.User;
import rss.user.UserService;
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
    protected UserCacheService userCacheService;
    @Autowired
    private NewsDao newsDao;
    @Autowired
    private UserService userService;

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
    @Transactional(propagation = Propagation.REQUIRED)
    public void dismissNews(User user) {
        user.setNewsDismiss(new Date());
        userService.updateUser(user);
        userCacheService.invalidateUser(user);
    }
}
