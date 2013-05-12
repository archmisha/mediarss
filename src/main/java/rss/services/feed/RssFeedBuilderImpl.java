package rss.services.feed;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.Subtitles;
import rss.entities.Torrent;
import rss.services.SettingsService;
import rss.services.subtitles.SubtitlesService;
import rss.services.subtitles.SubtitlesTrackerService;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 17:02
 */
@Service
public class RssFeedBuilderImpl implements RssFeedBuilder {

	private static Log log = LogFactory.getLog(RssFeedBuilderImpl.class);

	public static final Pattern MAGNET_LINK_PATTERN = Pattern.compile("(magnet:\\?xt=urn:btih:[^&]+)(&dn=([^&]+))?");

	@Autowired
	private SubtitlesTrackerService subtitlesTrackerService;

	@Override
	public String build(String feedTitle, String feedDescription, Collection<? extends Torrent> torrentEntries, Collection<Subtitles> subtitles) {
		StringBuilder sb = new StringBuilder();
		sb.append("<rss version=\"2.0\">\n");
		sb.append("  <channel>\n");
		sb.append("    <title>").append(feedTitle).append("</title>\n");
//				"<link>http://showrss.karmorra.info/</link>\n" +
		sb.append("    <ttl>30</ttl>\n");
		sb.append("    <description>").append(feedDescription).append("</description>\n");

		// sorting so that on refresh the order remains - and easier to look at it
		List<Torrent> torrentEntriesSorted = new ArrayList<>(torrentEntries);
		Collections.sort(torrentEntriesSorted, new Comparator<Torrent>() {
			@Override
			public int compare(Torrent o1, Torrent o2) {
				return o1.getTitle().compareTo(o2.getTitle());
			}
		});

		// not multi threaded
		SimpleDateFormat RFC822_DATE_FORMAT = new SimpleDateFormat("EEE', 'dd' 'MMM' 'yyyy' 'HH:mm:ss' 'Z", Locale.US);
		for (Torrent torrent : torrentEntriesSorted) {
			String title = torrent.getTitle();
			try {
				sb.append("    <item>\n");
				sb.append("      <title><![CDATA[").append(title).append("]]></title>\n");
				sb.append("      <link><![CDATA[").append(prepareLink(torrent)).append("]]></link>\n");
				sb.append("      <guid isPermaLink=\"true\">").append(prepareGuid(torrent.getUrl())).append("</guid>\n");
				sb.append("      <pubDate>").append(RFC822_DATE_FORMAT.format(torrent.getDateUploaded())).append("</pubDate>\n");
				sb.append("    </item>\n");
			} catch (UnsupportedEncodingException e) {
				log.error("Failed encoding url for '" + title + "': " + e.getMessage(), e);
			}
		}

		for (Subtitles subtitle : subtitles) {
			try {
				com.turn.ttorrent.common.Torrent torrent = subtitlesTrackerService.toTorrent(subtitle);
				String magnetLink = "magnet:?xt=urn:btih:" + torrent.getHexInfoHash() + "&dn=" + URLEncoder.encode(subtitle.getName(), "UTF-8") +
									"&tr=" +
									URLEncoder.encode(subtitlesTrackerService.getTrackerAnnounceUrl(), "UTF-8");
//									URLEncoder.encode(torrent.getAnnounceList().get(0).get(0).toASCIIString(), "UTF-8");//"udp%3A%2F%2F" + trackerHostName + "%3A" + trackerPort;
				//"&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A80&tr=udp%3A%2F%2Ftracker.publicbt.com%3A80&tr=udp%3A%2F%2Ftracker.istole.it%3A6969&tr=udp%3A%2F%2Ftracker.ccc.de%3A80";

				sb.append("    <item>\n");
				sb.append("      <title><![CDATA[").append(subtitle.getName()).append("]]></title>\n");
				sb.append("      <link><![CDATA[").append(magnetLink).append("]]></link>\n");
				sb.append("      <guid isPermaLink=\"true\">").append(prepareGuid(magnetLink)).append("</guid>\n");
//				sb.append("      <pubDate>").append(RFC822_DATE_FORMAT.format(torrent.getDateUploaded())).append("</pubDate>\n");
				sb.append("    </item>\n");
			} catch (Exception e) {
				log.error("Failed generating rss for '" + subtitle.getName() + "': " + e.getMessage(), e);
			}
		}

		sb.append("  </channel>\n");
		sb.append("</rss>");
		return sb.toString();
	}

	// CData is needed for firefox to display the rss page propertly
	// magnet:?xt=urn:btih:618c6cdb6420204a95a8c7a030696d13cbfa648b&dn=2+Broke+Girls+S02E09+720p+HDTV+X264-DIMENSION+%5BPublicHD%5D
	private String prepareLink(Torrent torrent) throws UnsupportedEncodingException {
		String url = torrent.getUrl();

		// some movies don't contain display name in the url for some reason
		if (!url.contains("&dn=")) {
			int idx = url.indexOf("&tr=");
			url = url.substring(0, idx) + "&dn=" + URLEncoder.encode(torrent.getTitle(), "UTF-8") + url.substring(idx, url.length());
		}

		return url;
		/*log.debug("prepareLink - " + url);
		Matcher matcher = MAGNET_LINK_PATTERN.matcher(url);

        StringBuilder link = new StringBuilder();
        link.append("<![CDATA[").append(matcher.group(1));

        // if have &dn= part in the url
        if (matcher.groupCount() > 1) {
            link.append("&dn=").append(matcher.group(3));
        }

        link.append("]]>");
        return link.toString();*/
	}

	// removing the display name &dn=... - also for firefox
	private String prepareGuid(String url) {
		String guid = url;
		int i = url.indexOf("&");
		if (i > -1) {
			guid = url.substring(0, i);
		}
		return guid;
	}
}
