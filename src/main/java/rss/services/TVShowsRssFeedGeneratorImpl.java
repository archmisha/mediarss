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
import rss.services.log.LogService;
import rss.util.CollectionUtils;
import rss.util.DateUtils;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 25/11/12
 * Time: 17:26
 */
@Service("tVShowsRssFeedGeneratorImpl")
public class TVShowsRssFeedGeneratorImpl implements RssFeedGenerator {

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

	@Autowired
	private LogService logService;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public String generateFeed(User user) {
		long from = System.currentTimeMillis();

		final Date downloadDate = new Date();

		// extract torrent entries - take best by quality
		Set<Torrent> torrentEntries = new HashSet<>();
		for (Episode episode : userDao.getEpisodesToDownload(user)) {
			Map<MediaQuality, List<Torrent>> qualityMap = new HashMap<>();
			for (Long torrentId : episode.getTorrentIds()) {
				Torrent torrent = torrentDao.find(torrentId);
				CollectionUtils.safeListPut(qualityMap, torrent.getQuality(), torrent);
			}

			for (MediaQuality quality : Arrays.asList(MediaQuality.HD1080P, MediaQuality.HD720P, MediaQuality.NORMAL)) {
				if (qualityMap.containsKey(quality)) {
					torrentEntries.addAll(qualityMap.get(quality));
					break;
				}
			}
		}

		// also add user episodes
		// add everything added since last feed generated with 7 days buffer
		int backlogDays = 7;
		for (UserTorrent userTorrent : userTorrentDao.findEpisodesAddedSince(DateUtils.getPastDate(user.getLastShowsFeedGenerated(), backlogDays), user)) {
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

		user.setLastShowsFeedGenerated(downloadDate);
		userDao.merge(user);

		logService.info(getClass(), String.format("Generated shows feed for %s (%d millis)", user, System.currentTimeMillis() - from));
		return rssFeed;
	}
}
