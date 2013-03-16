package rss.services;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.dao.UserDao;
import rss.dao.UserTorrentDao;
import rss.entities.Subtitles;
import rss.entities.Torrent;
import rss.entities.User;
import rss.entities.UserTorrent;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 25/11/12
 * Time: 17:26
 */
@Service("moviesRssFeedGeneratorImpl")
public class MoviesRssFeedGeneratorImpl implements RssFeedGenerator {

    private static Log log = LogFactory.getLog(MoviesRssFeedGeneratorImpl.class);

    @Autowired
    private UserDao userDao;

    @Autowired
    private RssFeedBuilder rssFeedBuilder;

	@Autowired
	private UserTorrentDao userTorrentDao;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public String generateFeed(User user) {
        long from = System.currentTimeMillis();

        Date downloadDate = new Date();
        user.setLastMoviesFeedGenerated(downloadDate);
        userDao.merge(user);

        Collection<Torrent> torrentEntries = new ArrayList<>();
        int backlogDays = 7;
        for (UserTorrent userTorrent : userTorrentDao.findUserMoviesForUserFeed(getUploadedDate(backlogDays), user)) {
            userTorrent.setDownloadDate(downloadDate);
            torrentEntries.add(userTorrent.getTorrent());
        }

        String rssFeed = rssFeedBuilder.build("Movies RSS personalized feed",
                "RSS feed of movies selected by the user in the past " + backlogDays + " days", torrentEntries, Collections.<Subtitles>emptyList());

        log.info(String.format("Generated movies feed for " + user + " (%d millis)", System.currentTimeMillis() - from));
        return rssFeed;
    }

    private Date getUploadedDate(int backlogDays) {
        Calendar c = Calendar.getInstance();
        c.setTime(new Date());
        c.add(Calendar.DAY_OF_MONTH, -backlogDays);
        return c.getTime();
    }
}
