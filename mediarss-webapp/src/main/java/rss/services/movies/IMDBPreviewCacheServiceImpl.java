package rss.services.movies;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.entities.Movie;
import rss.log.LogService;
import rss.services.PageDownloader;
import rss.util.DurationMeter;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: dikmanm
 * Date: 20/04/13 15:04
 */
@Service
public class IMDBPreviewCacheServiceImpl implements IMDBPreviewCacheService {

	@Autowired
	private LogService logService;

	@Autowired
	private PageDownloader pageDownloader;

	public static final int MAX_MOVIE_PREVIEWS_CACHE = 30;

	// +1 cuz when reaching max will add the new one and then remove the oldest one
	private LinkedHashMap<Long, String> moviePreviewPages = new LinkedHashMap<Long, String>(MAX_MOVIE_PREVIEWS_CACHE + 1, 0.75f, true) {
		private static final long serialVersionUID = -5533112972007175226L;

		@Override
		protected boolean removeEldestEntry(Map.Entry<Long, String> eldest) {
			return size() > MAX_MOVIE_PREVIEWS_CACHE;
		}
	};

	// currently not limiting the number
	private Map<String, String> movieCss = new HashMap<>();

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void addImdbPreview(Movie movie, String page) {
		moviePreviewPages.put(movie.getId(), page);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public String getImdbPreviewPage(Movie movie) {
		return moviePreviewPages.get(movie.getId());
	}

	@Override
	public String getImdbCSS(String cssFileName) {
		String css = movieCss.get(cssFileName);
		if (css != null) {
			return css;
		}

		try {
			DurationMeter durationMeter = new DurationMeter();
			css = pageDownloader.downloadPage(IMDBServiceImpl.IMDB_CSS_URL_PREFIX + cssFileName);
			css = cleanCSSFile(cssFileName, css);
			durationMeter.stop();
			logService.debug(getClass(), String.format("IMDB CSS file '%s' download took %d ms", cssFileName, durationMeter.getDuration()));
		} catch (Exception e) {
			css = null;
			logService.error(getClass(), e.getMessage(), e);
		}

		movieCss.put(cssFileName, css);
		logService.info(getClass(), String.format("Inserting a new CSS file '%s' into the cache (total size: %d)", cssFileName, movieCss.size()));

		return css;
	}

	private String cleanCSSFile(String cssFileName, String css) {
		DurationMeter durationMeter = new DurationMeter();

		css = StringUtils.replace(css, "images/G/01/imdb/images/pro_meter_badge/starmeter_wrap_gradient-996803324._V_.png", "../../../../../../images/imdb/starmeter_wrap_gradient-996803324._V_.png");
		css = StringUtils.replace(css, "images/G/01/imdb/images/pro_meter_badge/starmeter_icons-366893210._V_.png", "../../../../../../images/imdb/starmeter_icons-366893210._V_.png");
		css = StringUtils.replace(css, "images/G/01/imdb/images/title/titlePageSprite-2288315300._V_.png", "../../../../../../images/imdb/titlePageSprite-2288315300._V_.png");
		css = StringUtils.replace(css, "images/G/01/imdb/images/title/titlePageSprite-407338954._V_.png", "../../../../../../images/imdb/titlePageSprite-407338954._V_.png");
		css = StringUtils.replace(css, "images/G/01/imdb/images/rating/rating-list/sprite-1445387679._V_.png", "../../../../../../../images/imdb/sprite-1445387679._V_.png");
		css = StringUtils.replace(css, "images/G/01/imdb/images/tn15/starstiny-3317719365._V_.png", "../../../../../../images/imdb/starstiny-3317719365._V_.png");

		durationMeter.stop();
		logService.debug(getClass(), "Cleaning IMDB css file '" + cssFileName + "' took " + durationMeter.getDuration() + " ms");
		return css;
	}
}
