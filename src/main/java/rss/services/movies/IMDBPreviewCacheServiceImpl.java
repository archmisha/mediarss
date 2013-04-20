package rss.services.movies;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.entities.Movie;
import rss.services.PageDownloader;
import rss.services.log.LogService;
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

	public static final String IMDB_CSS_URL_PREFIX = "http://z-ecx.images-amazon.com/images/G/01/imdb/css/collections/";
	@Autowired
	private LogService logService;

	@Autowired
	private PageDownloader pageDownloader;

	private static final int MAX_MOVIE_PREVIEWS_CACHE = 20;

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
	public String getImdbPreviewPage(Movie movie) {
		String page = moviePreviewPages.get(movie.getId());
		if (page != null) {
			logService.debug(getClass(), "IMDB page for movie " + movie.getName() + " was found in cache");
			return page;
		}

		try {
			DurationMeter durationMeter = new DurationMeter();
			page = pageDownloader.downloadPage(movie.getImdbUrl());
			page = cleanImdbPage(movie.getName(), page);
			durationMeter.stop();
			logService.debug(getClass(), "IMDB page download for movie " + movie.getName() + " took " + durationMeter.getDuration() + " millis");
		} catch (Exception e) {
			page = null;
			logService.error(getClass(), e.getMessage(), e);
		}

		moviePreviewPages.put(movie.getId(), page);

		return page;
	}

	@Override
	public String getImdbCSS(String cssFileName) {
		String css = movieCss.get(cssFileName);
		if (css != null) {
			return css;
		}

		try {
			DurationMeter durationMeter = new DurationMeter();
			css = pageDownloader.downloadPage(IMDB_CSS_URL_PREFIX + cssFileName);
			css = cleanCSSFile(cssFileName, css);
			durationMeter.stop();
			logService.debug(getClass(), "IMDB CSS file '" + cssFileName + "' download took " + durationMeter.getDuration() + " millis");
		} catch (Exception e) {
			css = null;
			logService.error(getClass(), e.getMessage(), e);
		}

		movieCss.put(cssFileName, css);
		logService.info(getClass(), "Inserting a new CSS file '" + cssFileName + "' into the cache (total size: " + movieCss.size() + ")");

		return css;
	}

	private String cleanCSSFile(String cssFileName, String css) {
		DurationMeter durationMeter = new DurationMeter();

		css = css.replaceAll("images/G/01/imdb/images/pro_meter_badge/starmeter_wrap_gradient-996803324._V_.png", "../../../../../../images/imdb/starmeter_wrap_gradient-996803324._V_.png");
		css = css.replaceAll("images/G/01/imdb/images/pro_meter_badge/starmeter_icons-366893210._V_.png", "../../../../../../images/imdb/starmeter_icons-366893210._V_.png");
		css = css.replaceAll("images/G/01/imdb/images/title/titlePageSprite-2288315300._V_.png", "../../../../../../images/imdb/titlePageSprite-2288315300._V_.png");
		css = css.replaceAll("images/G/01/imdb/images/rating/rating-list/sprite-1445387679._V_.png", "../../../../../../../images/imdb/sprite-1445387679._V_.png");
		css = css.replaceAll("images/G/01/imdb/images/tn15/starstiny-3317719365._V_.png", "../../../../../../images/imdb/starstiny-3317719365._V_.png");

		durationMeter.stop();
		logService.debug(getClass(), "Cleaning IMDB css file '" + cssFileName + "' took " + durationMeter.getDuration() + " millis");
		return css;
	}

	private String cleanImdbPage(String name, String page) {
		DurationMeter durationMeter = new DurationMeter();
		Document doc = Jsoup.parse(page);
		doc.select("#maindetails_sidebar_bottom").remove();
		doc.select("#nb20").remove();
		doc.select("#titleRecs").remove();
		doc.select("#titleBoardsTeaser").remove();
		doc.select("div.article.contribute").remove();
		doc.select("div.watch-bar").remove();
		doc.select("#title_footer_links").remove();
		doc.select("div.message_box").remove();
		doc.select("#titleDidYouKnow").remove();
		doc.select("#footer").remove();
		doc.select("#root").removeAttr("id");
		doc.select("script").remove();
		doc.select("iframe").remove();
		doc.select("link[type!=text/css").remove();
		doc.select("#bottom_ad_wrapper").remove();
		doc.select("#pagecontent").removeAttr("id"); // got the style of the top line
		doc.select(".rightcornerlink").remove();
		doc.select("div#content-2-wide").removeAttr("id");
		doc.select("body").removeAttr("id");
		doc.select("br.clear").remove();
		doc.select("#content-1").removeAttr("id");
		doc.head().append("<style>html {min-width:100px;} body {margin:0px; padding:0px;}</style>");

		String html = doc.html();
//		html = html.replace("http://z-ecx.images-amazon.com/images/G/01/imdb/css/collections/title-2354501989._V370594279_.css", "../../../style/imdb/title-2354501989._V370594279_.css");
		html = html.replaceFirst(IMDB_CSS_URL_PREFIX, "../../../rest/movies/imdb/css/");
		html = html.replace("http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V397576332_.png", "../../images/imdb/name-2138558783._V397576332_.png");
		html = html.replace("http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/unknown-1394846836._V394978422_.png", "../../images/imdb/unknown-1394846836._V394978422_.png");
		html = html.replace("http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/no-video-slate-856072904._V396341087_.png", "../../images/imdb/no-video-slate-856072904._V396341087_.png");

		durationMeter.stop();
		logService.debug(getClass(), "Cleaning IMDB page for movie " + name + " took " + durationMeter.getDuration() + " millis");
		return html;
	}
}
