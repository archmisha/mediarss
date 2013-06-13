package rss.services.movies;

import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import rss.MediaRSSException;
import rss.dao.ImageDao;
import rss.entities.Image;
import rss.services.PageDownloader;
import rss.services.log.LogService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
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

	// must add the $, otherwise would find the year as 2 in '2 fast 2 furious (2003)'
	public static final Pattern NAME_YEAR_PATTERN = Pattern.compile("\\(?[^\\d]*(\\d+)[^\\d]*\\)?$");
	public static final Pattern COMING_SOON_PATTERN = Pattern.compile("<div class=\"showtime\">.*?<h2>Coming Soon</h2>", Pattern.MULTILINE | Pattern.DOTALL);
	// itemprop="description" blocks from finding other Not yet released string afterwards in the page
	public static final Pattern NOT_YET_RELEASED_PATTERN = Pattern.compile("<div class=\"rating-ineligible\">.*?Not yet released.*?</div>.*?itemprop=\"description\"", Pattern.MULTILINE | Pattern.DOTALL);
	public static final Pattern VIEWERS_PATTERN = Pattern.compile("<span itemprop=\"ratingCount\">([^<]*)</span>");
	public static final Pattern OLD_YEAR_PATTERN = Pattern.compile("<meta name=\"title\" content=\"(.*?) - IMDb\" />");
	public static final Pattern STORY_LINE_PATTERN = Pattern.compile("id=\"titleStoryLine\"");
	public static final Pattern PEOPLE_IMAGES_PATTERN = Pattern.compile("<td class=\"primary_photo\">.*?loadlate=\"([^\"]+)\"", Pattern.MULTILINE | Pattern.DOTALL);
	public static final Pattern MAIN_IMAGE_PATTERN = Pattern.compile("id=\"img_primary\".*?src=\"([^\"]+)\"", Pattern.MULTILINE | Pattern.DOTALL);

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private LogService logService;

	@Autowired
	private ImageDao imageDao;

	@Autowired
	private IMDBPreviewCacheService imdbPreviewCacheService;

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
		String partialPage;
		try {
			// imdb pages are large, downloading until a regular expression is satisfied and that chunk is returned
			partialPage = pageDownloader.downloadPageUntilFound(imdbUrl, STORY_LINE_PATTERN);
		} catch (Exception e) {
			// usually it is HTTP/1.1 404 Not Found
			if (!e.getMessage().contains("404 Not Found")) {
				logService.error(getClass(), "Failed downloading IMDB page " + imdbUrl + ": " + e.getMessage(), e);
			}
			return IMDBParseResult.createNotFound(imdbUrl);
		}

		try {
			// check for old year
			// <meta name="title" content="The Prestige (2006) - IMDb" />
			Matcher oldYearMatcher = OLD_YEAR_PATTERN.matcher(partialPage);
			oldYearMatcher.find();
			String name = oldYearMatcher.group(1);
			name = StringEscapeUtils.unescapeHtml4(name);

			logService.debug(getClass(), String.format("Downloading title for movie '%s' took %d millis", name, (System.currentTimeMillis() - from)));

			Matcher comingSoonMatcher = COMING_SOON_PATTERN.matcher(partialPage);
			boolean isComingSoon = comingSoonMatcher.find();

			if (!isComingSoon) {
				Matcher notYetReleasedMatcher = NOT_YET_RELEASED_PATTERN.matcher(partialPage);
				isComingSoon = notYetReleasedMatcher.find();
			}

			int viewers = -1;
			// if not yet released, no point parsing for viewers
			if (!isComingSoon) {
				Matcher viewersMatcher = VIEWERS_PATTERN.matcher(partialPage);
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
			partialPage = imdbPreviewCacheService.cleanImdbPage(name, partialPage);
			downloadImages(partialPage, imdbUrl, imagesAsync);

			return IMDBParseResult.createFound(imdbUrl, name, parseMovieYear(name), isComingSoon, viewers);
		} catch (Exception e) {
			// for any reason, regexp might fail or something else
			logService.error(getClass(), "Failed downloading IMDB page " + imdbUrl + ": " + e.getMessage(), e);
			return IMDBParseResult.createNotFound(imdbUrl);
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
		return getImage(imageFileName, IMDBPreviewCacheServiceImpl.IMDB_DEFAULT_PERSON_IMAGE);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public InputStream getMovieImage(String imageFileName) {
		return getImage(imageFileName, IMDBPreviewCacheServiceImpl.IMDB_AUTO_COMPLETE_DEFAULT_MOVIE_IMAGE);
	}

	private InputStream getImage(final String imageFileName, String defaultImage) {
		// remove the imdb url prefix, if exists. and also the rest call prefix - depends on where the call came from we have different prefixes
		String imdbImageUrl = StringUtils.replace(imageFileName, IMDBPreviewCacheServiceImpl.IMDB_IMAGE_URL_PREFIX, "");
		imdbImageUrl = StringUtils.replace(imdbImageUrl, IMDBPreviewCacheServiceImpl.REST_PERSON_IMAGE_URL_PREFIX, "");
		imdbImageUrl = StringUtils.replace(imdbImageUrl, IMDBPreviewCacheServiceImpl.REST_MOVIE_IMAGE_URL_PREFIX, "");
		try {
			InputStream imageInputStream;
			try {
				// first check if it is a no-person-image
				if (imdbImageUrl.equals(defaultImage)) {
					imageInputStream = new ClassPathResource(defaultImage, this.getClass().getClassLoader()).getInputStream();
				} else {
					Image image = imageDao.find(imdbImageUrl);
					if (image == null) {
						image = new Image(imdbImageUrl, pageDownloader.downloadData(IMDBPreviewCacheServiceImpl.IMDB_IMAGE_URL_PREFIX + imdbImageUrl));
						imageDao.persist(image);
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

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Collection<IMDBAutoCompleteItem> search(String query) {
		Collection<IMDBAutoCompleteItem> results = new ArrayList<>();

		query = StringUtils.replace(query.trim().toLowerCase(), " ", "_");
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
						image = StringUtils.replace(image, IMDBPreviewCacheServiceImpl.IMDB_IMAGE_URL_PREFIX, IMDBPreviewCacheServiceImpl.REST_MOVIE_IMAGE_URL_PREFIX);
						// pre-download images
						// too slow...
//						getMovieImage(image);
					} else {
						image = IMDBPreviewCacheServiceImpl.IMDB_AUTO_COMPLETE_DEFAULT_MOVIE_IMAGE;
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
}
