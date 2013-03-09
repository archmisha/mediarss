package rss.services.movies;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.services.PageDownloader;
import rss.services.SearchResult;
import rss.services.downloader.MovieRequest;
import rss.services.searchers.TorrentSearcher;
import rss.entities.Movie;
import rss.entities.Torrent;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 19:17
 */
@Service("publichdSearcher")
public class PublichdSearcher implements TorrentSearcher<MovieRequest, Movie> {

    private static Log log = LogFactory.getLog(PublichdSearcher.class);

    public static final String NAME = "publichd.se";
    public static final String PUBLICHD_TORRENT_URL = "http://" + NAME + "/index.php?page=torrent-details&id=";
    public static final Pattern PATTERN = Pattern.compile("<tag:torrents\\[\\].download /><a href=\".*?\">(.*?)<a href=(.*?)>.*AddDate</b></td>.*?>(.*?)</td>.*?seeds: (\\d+)", Pattern.DOTALL);
    public static final Pattern IMDB_URL_PATTERN = Pattern.compile("www.imdb.com/title/(.*?)[\"/<]");

	@Autowired
	private PageDownloader pageDownloader;

    @Override
    public SearchResult<Movie> search(MovieRequest movieRequest) {
        String url = PUBLICHD_TORRENT_URL + movieRequest.getHash();
        String page = pageDownloader.downloadPage(url);
        if (page == null) {
            log.error("Failed downloading page of " + movieRequest.toString() + " at " + url);
            return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
        }

        Matcher matcher = PATTERN.matcher(page);
        if (!matcher.find()) {
            if (!page.contains("Bad ID!")) { // in that case just id not found - not a parsing problem
                log.error("Failed parsing page of " + movieRequest.toString() + ": " + page);
            }
            return new SearchResult<>(SearchResult.SearchStatus.NOT_FOUND);
        }

        String title = matcher.group(1).trim(); // sometimes comes with line break at the end - ruins log
        String link = matcher.group(2);
        String uploadDataString = matcher.group(3);
        int seeders = Integer.parseInt(matcher.group(4));

        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date uploadDate = null;
        try {
            uploadDate = formatter.parse(uploadDataString);
        } catch (ParseException e) {
            log.error("Failed parsing date '" + uploadDataString + "': " + e.getMessage(), e);
        }

        String imdbUrl = null;
        matcher = IMDB_URL_PATTERN.matcher(page);
        if (matcher.find()) {
            imdbUrl = "http://www.imdb.com/title/" + matcher.group(1);
        }

        Torrent movieTorrent = new Torrent(title, link, uploadDate, seeders);
        SearchResult<Movie> searchResult = new SearchResult<>(movieTorrent, NAME);
        searchResult.getMetaData().setImdbUrl(imdbUrl);
        return searchResult;
    }

    @Override
    public String getName() {
        return NAME;
    }
}
