package rss.services;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.dao.SubtitlesDao;
import rss.dao.TorrentDao;
import rss.dao.UserDao;
import rss.dao.UserTorrentDao;
import rss.entities.*;
import rss.SubtitleLanguage;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 25/11/12
 * Time: 17:26
 */
@Service("tVShowsRssFeedGeneratorImpl")
public class TVShowsRssFeedGeneratorImpl implements RssFeedGenerator {

	private static Log log = LogFactory.getLog(TVShowsRssFeedGeneratorImpl.class);

	@Autowired
	private UserDao userDao;

	@Autowired
	private RssFeedBuilder rssFeedBuilder;

	@Autowired
	private UserTorrentDao userTorrentDao;

	@Autowired
	private TorrentDao torrentDao;

	@Autowired
	private SubtitlesDao subtitlesDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public String generateFeed(User user) {
		log.info("Generating feed");
		long from = System.currentTimeMillis();

		Date downloadDate = new Date();
		user.setLastShowsFeedGenerated(new Date());
		userDao.merge(user);

//        Set<EpisodeRequest> episodesToDownload = showRssService.getTVShowsEpisodes(user.getShowRssUsername(), user.getShowRssPassword());

		Collection<Episode> episodes = userDao.getEpisodesToDownload(user);

		// extract torrent entries - take best by quality
		Set<Torrent> torrentEntries = new HashSet<>();
		for (Episode media : episodes) {
			Map<MediaQuality, Torrent> qualityMap = new HashMap<>();
			for (Long torrentId : media.getTorrentIds()) {
				Torrent torrent = torrentDao.find(torrentId);
				qualityMap.put(torrent.getQuality(), torrent);
			}

			for (MediaQuality quality : Arrays.asList(MediaQuality.HD1080P, MediaQuality.HD720P, MediaQuality.NORMAL)) {
				if (qualityMap.containsKey(quality)) {
					torrentEntries.add(qualityMap.get(quality));
					break;
				}
			}
		}

		// also add user episodes
		int backlogDays = 7;
		for (UserTorrent userTorrent : userTorrentDao.findEpisodesAddedSince(getBacklogDate(backlogDays), user)) {
			userTorrent.setDownloadDate(downloadDate);
			torrentEntries.add(userTorrent.getTorrent());
		}

		// add subtitles
		Collection<Subtitles> subtitles;
		if ( user.getSubtitles() != null ) {
			subtitles = subtitlesDao.find(torrentEntries, user.getSubtitles(), SubtitleLanguage.ENGLISH);
		} else {
			subtitles = Collections.emptyList();
		}


		String rssFeed = rssFeedBuilder.build("TV Shows RSS personalized feed", "TV Shows RSS feed of the shows selected by the user", torrentEntries, subtitles);

		log.info(String.format("Generating feed took %d millis", System.currentTimeMillis() - from));
		return rssFeed;
	}

	private Date getBacklogDate(int backlogDays) {
		Calendar c = Calendar.getInstance();
		c.setTime(new Date());
		c.add(Calendar.DAY_OF_MONTH, -backlogDays);
		return c.getTime();
	}
}
