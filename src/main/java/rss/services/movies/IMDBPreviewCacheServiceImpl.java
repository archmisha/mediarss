package rss.services.movies;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.entities.Movie;
import rss.services.PageDownloader;
import rss.services.log.LogService;
import rss.util.DurationMeter;

import java.util.*;

/**
 * User: dikmanm
 * Date: 20/04/13 15:04
 */
@Service
public class IMDBPreviewCacheServiceImpl implements IMDBPreviewCacheService {

	public static final String IMDB_CSS_URL_PREFIX = "http://z-ecx.images-amazon.com/images/G/01/imdb/css/collections/";
	public static final String IMDB_IMAGE_URL_PREFIX = "http://ia.media-imdb.com/images/M/";
	public static final String REST_PERSON_IMAGE_URL_PREFIX = "../../../rest/movies/imdb/person-image/";
	public static final String REST_MOVIE_IMAGE_URL_PREFIX = "../../../rest/movies/imdb/movie-image/";
	public static final String IMDB_DEFAULT_PERSON_IMAGE = "../../images/imdb/person-no-image.png";
	public static final String IMDB_AUTO_COMPLETE_DEFAULT_MOVIE_IMAGE = "../../images/imdb/film-40x54.png";


	@Autowired
	private LogService logService;

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private IMDBService imdbService;

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
	public String getImdbPreviewPage(Movie movie) {
		String page = moviePreviewPages.get(movie.getId());
		if (page != null) {
			logService.debug(getClass(), String.format("IMDB page for movie %s was found in cache", movie.getName()));
			return page;
		}

		try {
			DurationMeter durationMeter = new DurationMeter();
			page = pageDownloader.downloadPage(movie.getImdbUrl());
			page = cleanImdbPage(movie.getName(), page);
			durationMeter.stop();
			logService.debug(getClass(), String.format("IMDB page download for movie %s took %d ms", movie.getName(), durationMeter.getDuration()));
		} catch (Exception e) {
			page = null;
			logService.error(getClass(), e.getMessage(), e);
		}

		moviePreviewPages.put(movie.getId(), page);

		// pre-download movie images, so no concurrency issue arise when the browser tries to fetch the images
		imdbService.downloadImages(page, movie.getImdbUrl());

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

	public String cleanImdbPage(String name, String page) {
		DurationMeter durationMeter = new DurationMeter();
		Document doc = Jsoup.parse(page);

		String[] elementsToRemove = new String[]{"#maindetails_sidebar_bottom", "#nb20", "#titleRecs", ".star-box-rating-widget",
												 "#titleBoardsTeaser", "div.article.contribute", "div.watch-bar",
												 "#title_footer_links", "div.message_box", "#titleDidYouKnow",
												 "#titleAwardsRanks", "#footer", "#titleMediaStrip", "iframe",
												 "link[type!=text/css", "#bottom_ad_wrapper", ".rightcornerlink",
												 "br.clear", "#titleFAQ", "#bottom_ad_wrapper", "#top_ad_wrapper",
												 "script", "noscript", "#boardsTeaser", "#prometer_container",
												 "div[itemprop=keywords]", /*"#titleCast .see-more",*/ /*"#titleStoryLine .see-more",*/
												 "#overview-bottom", "#maindetails_sidebar_top", ".yn", /*".user-comments .see-more",*/ ".see-more"};

		for (String selector : elementsToRemove) {
			for (Element element : doc.select(selector)) {
				removeSiblingHR(element);
				element.remove();
			}
		}

		// remove ids to prevent styles from being applied
		doc.select("#root").removeAttr("id");
		doc.select("#pagecontent").removeAttr("id"); // got the style of the top line
		doc.select("div#content-2-wide").removeAttr("id");
		doc.select("body").removeAttr("id");
		doc.select("#content-1").removeAttr("id");

		// remove stuff inside the details section
		Set<String> detailsHeaderToRemove = new HashSet<>(Arrays.asList("Box Office", "Company Credits", "Production Co:"));
		boolean deleting = false;
		for (Element cur : doc.select("#titleDetails").iterator().next().children()) {
			String tag = cur.tag().getName();
			if (tag.startsWith("h") && !tag.equals("hr")) {
				if (setStartsWith(detailsHeaderToRemove, cur.text())) {
					cur.remove();
					deleting = true;
				} else {
					deleting = false;
				}
			} else if (deleting) {
				cur.remove();
			}
		}

		// smart text-block removal
		Set<String> txtBlocksToRemove = new HashSet<>(Arrays.asList("Taglines:", "Motion Picture Rating", "Parents Guide:", "Certificate:", "Official Sites:"));
		for (Element element : doc.select(".txt-block")) {
			if (!element.children().isEmpty()) {
				if (setStartsWith(txtBlocksToRemove, element.children().get(0).text().trim())) {
					removeSiblingHR(element);
					element.remove();
				}
			}
		}


		doc.head().append("<style>html {min-width:100px;} body {margin:0px; padding:0px;} .article.title-overview .star-box.giga-star {padding-bottom:0px; }" +
						  ".giga-star.star-box .star-box-details { margin-top:10px; }</style>");


		// replace people images
		// <td class="primary_photo"> <a href="/name/nm0479471/?ref_=tt_cl_i2"><img width="32" height="44"
		// loadlate="../../../rest/movies/imdb/main-image/MV5BMTMyNDA0MDI4OV5BMl5BanBnXkFtZTcwMDQzMzEwMw@@._V1_SY44_CR1,0,32,44_.jpg" class="loadlate hidden "
		// src="../../images/imdb/name-2138558783._V397576332_.png" title="Shia LaBeouf" alt="Shia LaBeouf"></a> </td>
		Elements photos = doc.select(".primary_photo img");
		photos.removeAttr("class");
		for (Element photo : photos) {
			// avoiding usage of regex of String.replace method
			String src = StringUtils.replace(photo.attr("loadlate"), IMDB_IMAGE_URL_PREFIX, REST_PERSON_IMAGE_URL_PREFIX);
			if (StringUtils.isBlank(src)) {
				src = IMDB_DEFAULT_PERSON_IMAGE;
			}
			photo.attr("src", src);
		}

		String html = doc.html();
		// replace the url of the main image of the movie
		// avoiding usage of regex of String.replace method
		html = StringUtils.replace(html, IMDB_IMAGE_URL_PREFIX, REST_PERSON_IMAGE_URL_PREFIX);
		html = StringUtils.replace(html, IMDB_CSS_URL_PREFIX, "../../../rest/movies/imdb/css/");
		html = StringUtils.replace(html, "http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/32x44/name-2138558783._V397576332_.png", "../../images/imdb/name-2138558783._V397576332_.png");
		html = StringUtils.replace(html, "http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/unknown-1394846836._V394978422_.png", "../../images/imdb/unknown-1394846836._V394978422_.png");
		html = StringUtils.replace(html, "http://ia.media-imdb.com/images/G/01/imdb/images/nopicture/small/no-video-slate-856072904._V396341087_.png", "../../images/imdb/no-video-slate-856072904._V396341087_.png");

		// replace all the links
		html = StringUtils.replace(html, "<a ", "<span ");
		html = StringUtils.replace(html, "</a>", "</span>");

		durationMeter.stop();
		logService.debug(getClass(), "Cleaning IMDB page for movie " + name + " took " + durationMeter.getDuration() + " ms");
		return html;
	}

	private void removeSiblingHR(Element element) {
		// need to remove hr before or after if exists
		Element sibling = element.nextElementSibling();
		if (sibling != null && sibling.tag().getName().equals("hr")) {
			sibling.remove();
		} else {
			sibling = element.previousElementSibling();
			if (sibling != null && sibling.tag().getName().equals("hr")) {
				sibling.remove();
			}
		}
	}

	private static boolean setStartsWith(Set<String> set, String query) {
		for (String s : set) {
			if (query.startsWith(s)) {
				return true;
			}
		}
		return false;
	}
}
