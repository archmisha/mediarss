package rss.movies.imdb;

import org.apache.commons.collections4.IteratorUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.MediaRSSException;
import rss.PageDownloader;
import rss.log.LogService;
import rss.movies.images.Image;
import rss.movies.images.ImageService;
import rss.util.DurationMeter;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 21/04/13 21:54
 */
@Service
public class IMDBServiceImpl implements IMDBService {

	public static final String IMDB_URL = "http://www.imdb.com/title/";
	public static final String IMDB_CSS_URL_PREFIX = "http://z-ecx.images-amazon.com/images/G/01/imdb/css/collections/";
	public static final String IMDB_CSS_URL_PREFIX2 = "http://ia.media-imdb.com/images/G/01/imdb/css/collections/";
	public static final String IMDB_IMAGE_URL_PREFIX = "http://ia.media-imdb.com/images/M/";
	public static final String REST_PERSON_IMAGE_URL_PREFIX = "../../../rest/movies/imdb/person-image/";
	public static final String REST_MOVIE_IMAGE_URL_PREFIX = "../../../rest/movies/imdb/movie-image/";
	public static final String IMDB_DEFAULT_PERSON_IMAGE = "../../images/imdb/person-no-image.png";
	public static final String IMDB_AUTO_COMPLETE_DEFAULT_MOVIE_IMAGE = "../../images/imdb/film-40x54.png";

	// must add the $, otherwise would find the year as 2 in '2 fast 2 furious (2003)'
	public static final Pattern NAME_YEAR_PATTERN = Pattern.compile("\\(?[^\\d]*(\\d+)[^\\d]*\\)?$");
	public static final Pattern COMING_SOON_PATTERN = Pattern.compile("<div class=\"showtime\">.*?<h2>Coming Soon</h2>", Pattern.MULTILINE | Pattern.DOTALL);
	// itemprop="description" blocks from finding other Not yet released string afterwards in the page
	public static final Pattern NOT_YET_RELEASED_PATTERN = Pattern.compile("<div class=\"rating-ineligible\">.*?Not yet released.*?</div>.*?itemprop=\"description\"", Pattern.MULTILINE | Pattern.DOTALL);
	public static final Pattern VIEWERS_PATTERN = Pattern.compile("<span itemprop=\"ratingCount\">([^<]*)</span>");
	public static final Pattern OLD_YEAR_PATTERN = Pattern.compile("<meta name=\"title\" content=\"(.*?) - IMDb\" />");
	//	public static final Pattern STORY_LINE_PATTERN = Pattern.compile("id=\"titleStoryLine\"");
	public static final Pattern PEOPLE_IMAGES_PATTERN = Pattern.compile("<td class=\"primary_photo\">.*?loadlate=\"([^\"]+)\"", Pattern.MULTILINE | Pattern.DOTALL);
	public static final Pattern MAIN_IMAGE_PATTERN = Pattern.compile("id=\"img_primary\".*?src=\"([^\"]+)\"", Pattern.MULTILINE | Pattern.DOTALL);
	public static final Pattern RELEASE_DATE_PATTERN = Pattern.compile("<meta itemprop=\"datePublished\" content=\"(.*)\" />");

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private LogService logService;

	@Autowired
	private ImageService imageService;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public IMDBParseResult downloadMovieFromIMDBAndImagesAsync(String imdbUrl) {
		return downloadMovieFromIMDB(imdbUrl, true);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public IMDBParseResult downloadMovieFromIMDB(String imdbUrl) {
		return downloadMovieFromIMDB(imdbUrl, false);
	}

	private IMDBParseResult downloadMovieFromIMDB(String imdbUrl, boolean imagesAsync) {
		long from = System.currentTimeMillis();
		String page = null;
		try {
			page = pageDownloader.downloadPage(imdbUrl);

			// check for old year
			// <meta name="title" content="The Prestige (2006) - IMDb" />
			Matcher oldYearMatcher = OLD_YEAR_PATTERN.matcher(page);
			oldYearMatcher.find();
			String name = oldYearMatcher.group(1);
			name = StringEscapeUtils.unescapeHtml4(name);

			logService.debug(getClass(), String.format("Downloading title for movie '%s' took %d ms", name, (System.currentTimeMillis() - from)));

			// check for release date
			Date releaseDate = null;
			// <meta itemprop="datePublished" content="1999-03-31">
			Matcher releaseDateMatcher = RELEASE_DATE_PATTERN.matcher(page);
			if (releaseDateMatcher.find()) {
				String releaseDateStr = releaseDateMatcher.group(1);

				try {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
					releaseDate = sdf.parse(releaseDateStr);
				} catch (ParseException e) {
					SimpleDateFormat sdf = new SimpleDateFormat("yyyy");
					releaseDate = sdf.parse(releaseDateStr);
				}
			}

			Matcher comingSoonMatcher = COMING_SOON_PATTERN.matcher(page);
			boolean isComingSoon = comingSoonMatcher.find();

			if (!isComingSoon) {
				Matcher notYetReleasedMatcher = NOT_YET_RELEASED_PATTERN.matcher(page);
				isComingSoon = notYetReleasedMatcher.find();
			}

			int viewers = -1;
			// if not yet released, no point parsing for viewers
			if (!isComingSoon) {
				Matcher viewersMatcher = VIEWERS_PATTERN.matcher(page);
				if (!viewersMatcher.find()) {
					// not printing the partial page in purpose, it just spams the log file
					//				logService.warn(getClass(), "Failed retrieving number of viewers for '" + name + "': " + partialPage);
					logService.warn(getClass(), "Failed retrieving number of viewers for '" + name + "'");
				} else {
					String viewersStr = viewersMatcher.group(1);
					viewersStr = viewersStr.replaceAll(",", "");
					viewers = Integer.parseInt(viewersStr);
				}
			}

			// clean the imdb page before parsing for images, in order to avoid images like ad.doubleclick and so on
			page = cleanImdbPage(name, page);
			downloadImages(page, imdbUrl, imagesAsync);

			return IMDBParseResult.createFound(name, parseMovieYear(name), isComingSoon, viewers, releaseDate, page);
		} catch (Exception e) {
			// for any reason, regexp might fail or something else
			// usually it is HTTP/1.1 404 Not Found
			if (!e.getMessage().contains("404 Not Found")) {
				logService.error(getClass(), "Failed downloading or parsing IMDB page " + imdbUrl + ": " + e.getMessage() + ". Page: " + page, e);
			}

			return IMDBParseResult.createNotFound();
		}
	}

	private void downloadImages(final String page, final String imdbUrl, boolean imagesAsync) {
		if (imagesAsync) {
			final Class clazz = getClass();
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					try {
						transactionTemplate.execute(new TransactionCallbackWithoutResult() {
							@Override
							protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
								downloadImages(page, imdbUrl);
							}
						});
					} catch (Exception e) {
						logService.error(clazz, "Failed downloading images for movie: '" + imdbUrl + "': " + e.getMessage(), e);
					}
				}
			});
			executorService.shutdown();
		} else {
			downloadImages(page, imdbUrl);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	// no need a transaction here, each getPersonImage has its own transaction
	public void downloadImages(String page, String imdbUrl) {
		Matcher matcher = PEOPLE_IMAGES_PATTERN.matcher(page);
		while (matcher.find()) {
			String imageUrl = matcher.group(1);
			getPersonImage(imageUrl);
		}

		// main image
		matcher = MAIN_IMAGE_PATTERN.matcher(page);
		if (matcher.find()) {
			String imageUrl = matcher.group(1);
			getPersonImage(imageUrl);
		} else {
			logService.warn(getClass(), "Failed parsing main image of movie: " + imdbUrl);
		}
	}


	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public InputStream getPersonImage(String imageFileName) {
		return getImage(imageFileName, IMDB_DEFAULT_PERSON_IMAGE);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public InputStream getMovieImage(String imageFileName) {
		return getImage(imageFileName, IMDB_AUTO_COMPLETE_DEFAULT_MOVIE_IMAGE);
	}

	private InputStream getImage(final String imageFileName, String defaultImage) {
		// remove the imdb url prefix, if exists. and also the rest call prefix - depends on where the call came from we have different prefixes
		String imdbImageUrl = StringUtils.replace(imageFileName, IMDB_IMAGE_URL_PREFIX, "");
		imdbImageUrl = StringUtils.replace(imdbImageUrl, REST_PERSON_IMAGE_URL_PREFIX, "");
		imdbImageUrl = StringUtils.replace(imdbImageUrl, REST_MOVIE_IMAGE_URL_PREFIX, "");
		try {
			InputStream imageInputStream;
			try {
				// first check if it is a no-person-image
				if (imdbImageUrl.equals(defaultImage)) {
					imageInputStream = new ClassPathResource(defaultImage, this.getClass().getClassLoader()).getInputStream();
				} else {
					Image image = imageService.getImage(imdbImageUrl);
					if (image == null) {
						image = new Image(imdbImageUrl, pageDownloader.downloadData(IMDB_IMAGE_URL_PREFIX + imdbImageUrl));
						imageService.saveImage(image);
						logService.info(getClass(), "Storing a new image into the DB: " + imdbImageUrl);
					}

					imageInputStream = new ByteArrayInputStream(image.getData());
				}
			} catch (Exception e) {
				logService.error(getClass(), String.format("Failed fetching image %s: %s. Using default: %s-no-image", imdbImageUrl, e.getMessage(), defaultImage), e);
				imageInputStream = new ClassPathResource(defaultImage, this.getClass().getClassLoader()).getInputStream();
			}
			return imageInputStream;
		} catch (Exception e) {
			throw new MediaRSSException("Failed downloading IMDB image " + imdbImageUrl + ": " + e.getMessage(), e);
		}
	}

	private int parseMovieYear(String name) {
		Matcher matcher = NAME_YEAR_PATTERN.matcher(name);
		if (!matcher.find()) {
			throw new MediaRSSException("Failed parsing movie year for movie '" + name + "'");
		}

		return Integer.parseInt(matcher.group(1));
	}

	private static final Pattern NORMALIZE_TO_NOTHING_PATTERN = Pattern.compile("[:\\.']");
	private static final Pattern NORMALIZE_SPACES_PATTERN = Pattern.compile("\\s+");

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Collection<IMDBAutoCompleteItem> search(String query) {
		Collection<IMDBAutoCompleteItem> results = new ArrayList<>();

		query = query.trim().toLowerCase();
		query = NORMALIZE_TO_NOTHING_PATTERN.matcher(query).replaceAll("");
		query = NORMALIZE_SPACES_PATTERN.matcher(query).replaceAll(" ");
		query = StringUtils.replace(query, " ", "_");
		boolean retry = true;
		while (retry) {
			try {
				String page = pageDownloader.downloadPage("http://sg.media-imdb.com/suggests/" + query.charAt(0) + "/" + query + ".json");
				page = page.substring(page.indexOf("\"d\":") + "\"d\":".length(), page.length() - 2);

				ObjectMapper mapper = new ObjectMapper();
				mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
				for (JsonNode jsonNode : IteratorUtils.toList(mapper.readTree(page).getElements())) {
					String id = jsonNode.get("id").getTextValue();
					// skip non movie results
					if (!id.startsWith("tt")) {
						continue;
					}
					int year = -1;
					if (jsonNode.get("y") != null) {
						year = jsonNode.get("y").getIntValue();
					}
					String name = jsonNode.get("l").getTextValue();
					String image;
					JsonNode imageNode = jsonNode.get("i");
					if (imageNode != null) {
						image = imageNode.get(0).getTextValue();
						image = StringUtils.replace(image, IMDB_IMAGE_URL_PREFIX, REST_MOVIE_IMAGE_URL_PREFIX);
						// pre-download images
						// too slow...
//						getMovieImage(image);
					} else {
						image = IMDB_AUTO_COMPLETE_DEFAULT_MOVIE_IMAGE;
					}
					results.add(new IMDBAutoCompleteItem(name, id, year, image));
					retry = false;
				}
			} catch (Exception e) {
				// imdb returns 403 when the search produces no results - then should retry with 1 character less
				// if query length already 1 there is nothing to retry for
				if (e.getMessage() != null && e.getMessage().contains("403 Forbidden")) {
					if (query.length() > 1) {
						query = query.substring(0, query.length() - 1);
					}
					// don't log if the problem was 403 from IMDB
				} else {
					logService.error(getClass(), "Error searching for: " + query + ": " + e.getMessage(), e);
					retry = false;
				}
			}
		}

		return results;
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
												 "#overview-bottom", "#maindetails_sidebar_top", ".yn", /*".user-comments .see-more",*/ ".see-more",
												 ".pro-title-link.text-center"};

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


		doc.head().append("<style>" +
						  "html {min-width:100px;} body {margin:0px; padding:0px;} .article.title-overview .star-box.giga-star {padding-bottom:0px; } " +
						  ".giga-star.star-box .star-box-details { margin-top:10px; } " +
						  "@media screen and (max-width: 480px) {" +
						  "  #img_primary img {" +
						  "      width: 100px;" +
						  "		 height: auto;" +
						  "  }" +
						  "  .star-box-giga-star {" +
						  "      display: none;" +
						  "  }" +
						  "  .giga-star.star-box .star-box-details {" +
						  "      margin-top: 0px;" +
						  "      float: none;" +
						  "      width: auto;" +
						  "      margin-right: 0px;" +
						  "      line-height: inherit;" +
						  "  }" +
						  "  div.infobar {" +
						  "    margin-bottom: 4px;" +
						  "  }" +
						  "  table#title-overview-widget-layout td#overview-top .txt-block," +
						  "  table#title-overview-widget-layout td#overview-top p {" +
						  "    margin-left: -105px;" +
						  "  }" +
						  "}</style>");


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
		html = StringUtils.replace(html, IMDB_CSS_URL_PREFIX2, "../../../rest/movies/imdb/css/");
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
