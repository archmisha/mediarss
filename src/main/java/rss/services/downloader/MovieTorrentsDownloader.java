package rss.services.downloader;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.dao.*;
import rss.entities.*;
import rss.services.movies.IMDBParseResult;
import rss.services.movies.IMDBService;
import rss.services.requests.movies.MovieRequest;
import rss.services.searchers.MovieSearcher;
import rss.services.searchers.SearchResult;
import rss.services.subtitles.SubtitleLanguage;
import rss.util.CollectionUtils;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 03/12/12
 * Time: 09:10
 */
@Service
public class MovieTorrentsDownloader extends MoviesDownloader {

	@Autowired
	private MovieSearcher movieSearcher;

	@Override
	protected SearchResult downloadTorrent(MovieRequest movieRequest) {
		return movieSearcher.search(movieRequest);
	}
}
