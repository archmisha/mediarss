package rss.services.shows;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.entities.Episode;
import rss.entities.Show;
import rss.services.PageDownloader;
import rss.services.SettingsService;
import rss.services.log.LogService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 24/12/12
 * Time: 23:53
 */
@Service("tVComServiceImpl")
public class TVComServiceImpl implements ShowsProvider {

	private static final String SEARCH_URL = "http://www.tv.com/search?q=%s";
//	public static final Pattern EPISODES_IN_SEASON_PATTERN = Pattern.compile("class=\"filter \" data-season=\"\\d+\".*?<strong>Season (\\d+)</strong> \\((\\d+)\\)", Pattern.DOTALL);
	public static final Pattern EPISODE_SCHEDULE_PATTERN = Pattern.compile("(<a class=\"season_name[^>]+>.*?Season (\\d+).*?</a>)|(<div class=\"ep_info\">\\s*Episode (\\d+)\\s*</div>.*?<div class=\"date\">(.*?)</div>)", Pattern.DOTALL);
	public static final Pattern IS_ENDED_PATTERN = Pattern.compile("<div class=\"tagline\">.*? \\(ended \\d+\\)\\s*</div>", Pattern.DOTALL);
	public static final Pattern IS_ENDED_SEARCH_PATTERN = Pattern.compile("<div class=\"airtime\">.*? \\(ended \\d+\\)\\s*</div>", Pattern.DOTALL);
	public static final String SHOWS_LIST_PAGED_URL = "http://www.tv.com/shows/page%d/";
	public static final Pattern SHOW_SEARCH_RESULTS = Pattern.compile("<li class=\"result show\">.*?<div class=\"info\">\\s*<h4><a href=\"(.*?)\">(.*?)</a></h4>\\s*(<div class=\"tagline\">.*?</div>)", Pattern.DOTALL);
	public static final Pattern SHOW_EPISODES = Pattern.compile("<h1 itemprop=\"name\">(.*?)</h1>.*?(<div class=\"tagline\">.*?</div>)", Pattern.DOTALL);

	/*
	<h4>
	<a href="/shows/greys-anatomy/">Grey&#39;s Anatomy</a>
	</h4>
	*/
	public static final Pattern PATTERN = Pattern.compile("<h4>.*?<a href=\"/shows/(.*?)/\">(.*?)</a>.*?</h4>\\s*(<div[^<]+</div>)", Pattern.DOTALL);

	public static final Pattern STRIP_BRACES_PATTERN = Pattern.compile("(.*?)\\(.*?\\)");

	@Autowired
	protected LogService log;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private SettingsService settingsService;

	@Override
	public Show search(String name) {
		try {
			// transform 'The Office (US)' into 'The Office'
			Matcher stripBracesMatcher = STRIP_BRACES_PATTERN.matcher(name);
			if (stripBracesMatcher.find()) {
				String newName = stripBracesMatcher.group(1).trim();
				log.info(getClass(), "Stripping '" + name + "' into '" + newName + "' before searching in tv.com");
				name = newName;
			}

			String page = pageDownloader.downloadPage(String.format(SEARCH_URL, URLEncoder.encode(name, "UTF-8")));
			Matcher matcher = SHOW_SEARCH_RESULTS.matcher(page);

			// take the first result
			if (matcher.find()) {
				String showName = matcher.group(2);
				String tvComUrl = "http://www.tv.com" + matcher.group(1) + "episodes/";
				Matcher isEndedMatcher = IS_ENDED_PATTERN.matcher(matcher.group(3));
				boolean ended = isEndedMatcher.find();

				return createShow(showName, tvComUrl, ended);
			}
			return null;
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
	}

	public Show downloadShowByUrl(String url) {
		String page = pageDownloader.downloadPage(url);
		Matcher matcher = SHOW_EPISODES.matcher(page);
		if (!matcher.find()) {
			return null;
		}
		String name = matcher.group(1);
		if (StringUtils.isBlank(name)) {
			return null;
		}

		Matcher isEndedMatcher = IS_ENDED_PATTERN.matcher(matcher.group(2));
		boolean ended = isEndedMatcher.find();

		return createShow(name, url, ended);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Collection<Episode> downloadInfo(Show show) {
		Collection<Episode> newEpisodes = new ArrayList<>();
		try {
			EpisodesMapper episodesMapper = new EpisodesMapper(show.getEpisodes());

			String page;
			try {
				page = pageDownloader.downloadPage(show.getTvComUrl());
			} catch (Exception e) {
				throw new RuntimeException("Show '" + show + "' got invalid tv.com url " + show.getTvComUrl() + ": " + e.getMessage(), e);
			}

			Matcher isEndedMatcher = IS_ENDED_PATTERN.matcher(page);
			show.setEnded(isEndedMatcher.find());

			Matcher matcher = EPISODE_SCHEDULE_PATTERN.matcher(page);
			int season = -1;
			while (matcher.find()) {
				if (matcher.group(2) != null) {
					season = Integer.parseInt(matcher.group(2));
				} else {
					int episodeNum = Integer.parseInt(matcher.group(4));
					// weird, but in greys anatomy season 8 episode 19 there was no air date
					Date airDate = null;
					if (matcher.group(5).trim().length() > 0) {
						// cant be static const - not multi-threaded
						SimpleDateFormat AIR_DATE_FORMAT = new SimpleDateFormat("M/dd/yy");
						airDate = AIR_DATE_FORMAT.parse(matcher.group(5));
					}

					Episode episode = episodesMapper.get(season, episodeNum);
					if (episode == null) {
						episode = new Episode(season, episodeNum);
						// for some reason same episode appears twice sometimes in the page,
						// so saving it in the mapper to avoid persisting twice
						episodesMapper.add(episode);
						newEpisodes.add(episode);
					}
					episode.setAirDate(airDate);
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		return newEpisodes;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Collection<Show> downloadShowList() {
		Collection<Show> result = new ArrayList<>();
		int pagesToDownload = settingsService.getTVComPagesToDownload();
		for (int i = 0; i < pagesToDownload; ++i) {
			log.info(getClass(), "downloadShowList - page=" + (i+1));
			String page = pageDownloader.downloadPage(String.format(SHOWS_LIST_PAGED_URL, i + 1));
			Matcher matcher = PATTERN.matcher(page);
			while (matcher.find()) {
				String urlPart = matcher.group(1);
				String showName = matcher.group(2);
				showName = StringEscapeUtils.unescapeHtml4(showName);
				String tvComUrl = "http://www.tv.com/shows/" + urlPart + "/episodes/";
				Matcher isEndedMatcher = IS_ENDED_SEARCH_PATTERN.matcher(matcher.group(3));
				boolean ended = isEndedMatcher.find();

				result.add(createShow(showName, tvComUrl, ended));
			}
		}
		return result;
	}

	@Override
	public Collection<Episode> downloadSchedule() {
		throw new UnsupportedOperationException("TVCom service can't download schedule for all shows");
	}

	private Show createShow(String name, String url, boolean ended) {
		Show show = new Show();
		show.setName(name);
		show.setTvComUrl(url);
		show.setEnded(ended);
		return show;
	}
}