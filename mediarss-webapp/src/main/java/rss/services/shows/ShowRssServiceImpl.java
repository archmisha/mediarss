package rss.services.shows;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.services.PageDownloader;
import rss.services.requests.episodes.EpisodeRequest;
import rss.util.DurationMeter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Pattern;

/*
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 17:38
 */
@Service
public class ShowRssServiceImpl implements ShowRssService {

    private static Logger log = LoggerFactory.getLogger(ShowRssServiceImpl.class);

    public static final String SHOWRSS_HOSTNAME = "showrss.karmorra.info";
    public static final Pattern SHOW_ENTRY = Pattern.compile("<div class=\"showentry\">.*?\">([^>]*?)</a></div>");

    @Autowired
    private PageDownloader pageDownloader;

    @Override
    public boolean validateCredentials(String username, String password) {
//		List<Cookie> cookies = loginToShowRss(username, password);
//		for (Cookie cookie : cookies) {
//			if (cookie.getName().equalsIgnoreCase("session")) {
//				return true;
//			}
//		}

        return false;
    }

    @Override
    public Collection<String> getShows(String username, String password) {
        log.info("Retrieving Show list from " + SHOWRSS_HOSTNAME);
        DurationMeter durationMeter = new DurationMeter();
        Set<String> shows = new HashSet<>();

//		String page = getUserHomePage(username, password);
//		if (page == null) {
//			return shows;
//		}

//		page = page.substring(page.indexOf("<div id=\"shows_block\""));
//		Matcher matcher = SHOW_ENTRY.matcher(page);
//		while (matcher.find()) {
//			shows.add(matcher.group(1));
//		}

        log.info(String.format("Retrieving Show list from " + SHOWRSS_HOSTNAME + " took %d ms", durationMeter.getDuration()));
        return shows;
    }

	/*@Override
    public Set<EpisodeRequest> getTVShowsEpisodes(String username, String password) {
		log.info("Retrieving TV Shows from " + SHOWRSS_HOSTNAME);
		DurationMeter durationMeter = new DurationMeter();
		Set<EpisodeRequest> tvShowEpisodes = new HashSet<>();

		String page = getUserHomePage(username, password);
		if (page == null) {
			return tvShowEpisodes;
		}

		int idx = page.indexOf("<div id=\"show_timeline\">");
		int idxEnd = page.indexOf("<div id=\"shows_block\"");
		idx = page.indexOf("<div class=\"showentry\">", idx);
		// <a href="http://showrss.karmorra.info/r/a169b9a5c7879a06206834b928a1384c.torrent" class="std"><strong>Hunted</strong> 1x08</a></div>
		while (idx > -1 && idx < idxEnd) {
			String tvShowNamePrefix = "<strong>";
			idx = page.indexOf(tvShowNamePrefix, idx) + tvShowNamePrefix.length();
			String tvShowNameSuffix = "</strong>";
			int idx2 = page.indexOf(tvShowNameSuffix, idx);
			String tvShowName = page.substring(idx, idx2);
			tvShowName = tvShowName.trim();

			EpisodeRequest episodeRequest = new EpisodeRequest(tvShowName, MediaQuality.HD720P);

			// possible options: </strong> 2008 5x07</a>, </strong> 2x07 720p</a>, </strong> 2x07</a>, </strong>2x07</a>
			// todo: currently ignoring the year - like 2008. should add it to the tv show name?
			// split by space and locate the part containing only the x character or digits
			idx = idx2 + tvShowNameSuffix.length();
			String tmp = page.substring(idx, page.indexOf("</a>", idx));
			for (String s : tmp.split(" ")) {
				Pattern p = Pattern.compile("\\d+x\\d+");
				Matcher matcher = p.matcher(s);
				if (matcher.matches()) {
					parseEpisodeNumber(episodeRequest, s);
					break;
				}
			}

			// todo: skipping episodes like that: Flashpoint S5 Special - season and episode are not parsed
			if (!tvShowEpisodes.contains(episodeRequest) && episodeRequest.getTorrent() > 0 && episodeRequest.getSeason() > 0) {
				tvShowEpisodes.add(episodeRequest);
			}

			idx = page.indexOf("<div class=\"showentry\">", idx);
		}

		log.info(String.format("Retrieving TV Shows from " + SHOWRSS_HOSTNAME + " took %d ms", durationMeter.getDuration()));
		return tvShowEpisodes;
	}*/

//	private String getUserHomePage(String username, String password) {
//		List<Cookie> cookies = loginToShowRss(username, password);
//		if (cookies == null) {
//			return null;
//		}
//
//		StringBuilder cookiesSB = new StringBuilder();
//		for (Cookie cookie : cookies) {
//			cookiesSB.append(cookie.getName()).append("=").append(cookie.getValue()).append(";");
//		}
//		log.info("Logged in to " + SHOWRSS_HOSTNAME + " as " + username + " cookies: " + cookiesSB.toString());
//
//		String showsUrl = "http://" + SHOWRSS_HOSTNAME + "/?cs=shows";
//		String page = pageDownloader.downloadPage(showsUrl, CollectionUtils.toMap("Cookie", cookiesSB.toString()));
//		return page;
//	}

//	private List<Cookie> loginToShowRss(String username, String password) {
//		String loginUrl = "http://" + SHOWRSS_HOSTNAME + "/?cs=login";
//		return pageDownloader.sendPostRequest(loginUrl, CollectionUtils.toMap("username", username, "password", password));
//	}

    private void parseEpisodeNumber(EpisodeRequest episodeRequest, String episodeNumber) {
        String[] arr = episodeNumber.split("x");
        int season = Integer.parseInt(arr[0].trim());
        int episode = Integer.parseInt(arr[1].trim());
        episodeRequest.setSeason(season);
//		episodeRequest.setEpisode(episode);
    }
}
