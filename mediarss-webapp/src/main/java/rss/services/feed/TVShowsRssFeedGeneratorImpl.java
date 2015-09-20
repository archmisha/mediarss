package rss.services.feed;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.dao.SubtitlesDao;
import rss.dao.TorrentDao;
import rss.dao.UserDao;
import rss.dao.UserTorrentDao;
import rss.entities.Subtitles;
import rss.entities.User;
import rss.entities.UserEpisodeTorrent;
import rss.log.LogService;
import rss.services.user.UserCacheService;
import rss.torrents.MediaQuality;
import rss.torrents.Torrent;
import rss.torrents.UserTorrent;
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
	protected UserCacheService userCacheService;

	@Autowired
	private LogService logService;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public String generateFeed(User user) {
		long from = System.currentTimeMillis();

		final Date downloadDate = new Date();
		Set<Torrent> torrentEntries = getFeedTorrents(user, 7, downloadDate);

		// add subtitles
		Collection<Subtitles> subtitles;
//		if (user.getSubtitles() != null) {
//			subtitles = subtitlesDao.find(torrentEntries, user.getSubtitles());
//		} else {
		subtitles = Collections.emptyList();
//		}

		String rssFeed = rssFeedBuilder.build("TV Shows RSS personalized feed", "TV Shows RSS feed of the shows selected by the user", torrentEntries, subtitles);

		user.setLastShowsFeedGenerated(downloadDate);
		userDao.merge(user);
		userCacheService.invalidateUser(user);

		logService.info(getClass(), String.format("Generated shows feed for %s (%d ms)", user, System.currentTimeMillis() - from));
		return rssFeed;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Set<Torrent> getFeedTorrents(User user) {
		return getFeedTorrents(user, 7, null);
	}

	private Set<Torrent> getFeedTorrents(User user, int backlogDays, Date downloadDate) {
		Set<Torrent> torrentEntries = new HashSet<>();

		// Add user episodes
		// add everything added since last feed generated with 'backlogDays' days buffer
		for (UserTorrent userTorrent : userTorrentDao.findEpisodesAddedSince(user, DateUtils.getPastDate(user.getLastShowsFeedGenerated(), backlogDays))) {
			if (downloadDate != null) {
				userTorrent.setDownloadDate(downloadDate);
			}
			torrentEntries.add(userTorrent.getTorrent());
		}

		// Extract torrent entries - take best by quality
		for (Episode episode : userDao.getEpisodesToDownload(user)) {
			Map<MediaQuality, List<Torrent>> qualityMap = new HashMap<>();
			for (Long torrentId : episode.getTorrentIds()) {
				Torrent torrent = torrentDao.find(torrentId);
				CollectionUtils.safeListPut(qualityMap, torrent.getQuality(), torrent);
			}

			for (MediaQuality quality : Arrays.asList(MediaQuality.HD1080P, MediaQuality.HD720P, MediaQuality.NORMAL)) {
				if (qualityMap.containsKey(quality)) {
					List<Torrent> torrents = qualityMap.get(quality);
					for (Torrent torrent : torrents) {
						if (!torrentEntries.contains(torrent)) {
							UserTorrent userTorrent = userTorrentDao.findUserEpisodeTorrent(user, torrent.getId());
							if (userTorrent == null) {
								userTorrent = new UserEpisodeTorrent();
								userTorrent.setUser(user);
								userTorrent.setTorrent(torrent);
								userTorrent.setAdded(new Date());
								userTorrent.setDownloadDate(downloadDate);
								userTorrentDao.persist(userTorrent);
							}
							torrentEntries.add(torrent);
						}
					}
					break; // first that is found, we quit
				}
			}
		}

		return torrentEntries;
	}
}
