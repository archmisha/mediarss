package com.turn.ttorrent.tracker;

import com.turn.ttorrent.common.Torrent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.services.SettingsService;
import rss.services.UrlService;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Copyright (C) 2011-2012 Turn, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * BitTorrent tracker.
 *
 * <p>
 * The tracker usually listens on port 6969 (the standard BitTorrent tracker
 * port). Torrents must be registered directly to this tracker with the
 * {@link #announce(com.turn.ttorrent.tracker.TrackedTorrent torrent)}</code> method.
 * </p>
 *
 * @author mpetazzoni
 */

/**
 * User: dikmanm
 * Date: 30/01/13 20:43
 */
@Service
public class EmbeddedTracker {

	private static final Logger logger = LoggerFactory.getLogger(EmbeddedTracker.class);

	/**
	 * Request path handled by the tracker announce request handler.
	 */
	public static final String ANNOUNCE_URL = "announce";

	/**
	 * Default rss name and version announced by the tracker.
	 */
	public static final String DEFAULT_VERSION_STRING = "BitTorrent Tracker (ttorrent)";

	@Autowired
	private UrlService urlService;

	@Autowired
	private SettingsService settingsService;

	private final TrackerService trackerService;

	/**
	 * The in-memory repository of torrents tracked.
	 */
	private final ConcurrentMap<String, TrackedTorrent> torrents;

	private Thread collector;
	private boolean stop;

	public EmbeddedTracker() {
		this.torrents = new ConcurrentHashMap<>();
		this.trackerService = new TrackerService(DEFAULT_VERSION_STRING, this.torrents);
	}

	@PostConstruct
	private void start() {
		logger.info("Starting BitTorrent tracker on {}...", getAnnounceUrl());

		if (this.collector == null || !this.collector.isAlive()) {
			this.collector = new PeerCollectorThread();
			this.collector.setName("peer-collector");
			this.collector.start();
		}
	}

	/**
	 * Returns the full announce URL served by this tracker.
	 * <p/>
	 * <p>
	 * This has the form http://host:port/announce.
	 * </p>
	 */
	public URL getAnnounceUrl() {
		try {
			String trackerUrl = settingsService.getTrackerUrl();
			if (StringUtils.isBlank(trackerUrl)) {
				trackerUrl = urlService.getApplicationUrl();
			}
			if (!trackerUrl.endsWith("/")) {
				trackerUrl += "/";
			}
			return new URI(trackerUrl + ANNOUNCE_URL).toURL();
		} catch (MalformedURLException | URISyntaxException mue) {
			logger.error("Could not build tracker URL: {}!", mue, mue);
		}

		return null;
	}

	/**
	 * Stop the tracker.
	 * <p/>
	 * <p>
	 * This effectively closes the listening HTTP connection to terminate
	 * the service, and interrupts the peer collector thread as well.
	 * </p>
	 */
	@PreDestroy
	private void stop() {
		this.stop = true;

		if (this.collector != null && this.collector.isAlive()) {
			this.collector.interrupt();
			logger.info("Peer collection terminated.");
		}
	}

	public void handleAnnounceRequest(HttpServletRequest request, HttpServletResponse response) {
		this.trackerService.handle(new HttpServletRequestWrapper(request), new HttpServletResponseWrapper(response));
	}

	/**
	 * Announce a new torrent on this tracker.
	 * <p/>
	 * <p>
	 * The fact that torrents must be announced here first makes this tracker a
	 * closed BitTorrent tracker: it will only accept clients for torrents it
	 * knows about, and this list of torrents is managed by the program
	 * instrumenting this Tracker class.
	 * </p>
	 *
	 * @param torrent The Torrent object to start tracking.
	 * @return The torrent object for this torrent on this tracker. This may be
	 *         different from the supplied Torrent object if the tracker already
	 *         contained a torrent with the same hash.
	 */
	public synchronized TrackedTorrent announce(TrackedTorrent torrent) {
		TrackedTorrent existing = this.torrents.get(torrent.getHexInfoHash());

		if (existing != null) {
			logger.warn("Tracker already announced torrent for '{}' " +
						"with hash {}.", existing.getName(), existing.getHexInfoHash());
			return existing;
		}

		this.torrents.put(torrent.getHexInfoHash(), torrent);
		logger.info("Registered new torrent for '{}' with hash {}.", torrent.getName(), torrent.getHexInfoHash());
		return torrent;
	}

	/**
	 * Stop announcing the given torrent.
	 *
	 * @param torrent The Torrent object to stop tracking.
	 */
	public synchronized void remove(Torrent torrent) {
		if (torrent == null) {
			return;
		}

		this.torrents.remove(torrent.getHexInfoHash());
	}

	/**
	 * Stop announcing the given torrent after a delay.
	 *
	 * @param torrent The Torrent object to stop tracking.
	 * @param delay   The delay, in milliseconds, before removing the torrent.
	 */
	public synchronized void remove(Torrent torrent, long delay) {
		if (torrent == null) {
			return;
		}

		new Timer().schedule(new TorrentRemoveTimer(this, torrent), delay);
	}

	/**
	 * Timer task for removing a torrent from a tracker.
	 * <p/>
	 * <p>
	 * This task can be used to stop announcing a torrent after a certain delay
	 * through a Timer.
	 * </p>
	 */
	private static class TorrentRemoveTimer extends TimerTask {

		private EmbeddedTracker tracker;
		private Torrent torrent;

		TorrentRemoveTimer(EmbeddedTracker tracker, Torrent torrent) {
			this.tracker = tracker;
			this.torrent = torrent;
		}

		@Override
		public void run() {
			this.tracker.remove(torrent);
		}
	}

	/**
	 * The unfresh peer collector thread.
	 * <p/>
	 * <p>
	 * Every PEER_COLLECTION_FREQUENCY_SECONDS, this thread will collect
	 * unfresh peers from all announced torrents.
	 * </p>
	 */
	private class PeerCollectorThread extends Thread {

		private static final int PEER_COLLECTION_FREQUENCY_SECONDS = 15;

		@Override
		public void run() {
			logger.info("Starting tracker peer collection for tracker at {}...",
					getAnnounceUrl());

			while (!stop) {
				for (TrackedTorrent torrent : torrents.values()) {
					torrent.collectUnfreshPeers();
				}

				try {
					Thread.sleep(PeerCollectorThread
										 .PEER_COLLECTION_FREQUENCY_SECONDS * 1000);
				} catch (InterruptedException ie) {
					// Ignore
				}
			}
		}
	}
}
