package rss.services.movies;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.MediaRSSException;
import rss.dao.ImageDao;
import rss.entities.Image;
import rss.services.PageDownloader;
import rss.services.log.LogService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
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

	public static final Pattern NAME_YEAR_PATTERN = Pattern.compile("\\(?[^\\d]*(\\d+)[^\\d]*\\)?");
	public static final Pattern COMING_SOON_PATTERN = Pattern.compile("<div class=\"showtime\">.*?<h2>Coming Soon</h2>", Pattern.MULTILINE | Pattern.DOTALL);
	public static final Pattern NOT_YET_RELEASED_PATTERN = Pattern.compile("<div class=\"rating-ineligible\">.*?Not yet released.*?</div>", Pattern.MULTILINE | Pattern.DOTALL);
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

	@Override
	public IMDBParseResult downloadMovieFromIMDBAndImagesAsync(String imdbUrl) {
		return downloadMovieFromIMDB(imdbUrl, true);
	}

	@Override
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

		// check for old year
		// <meta name="title" content="The Prestige (2006) - IMDb" />
		Matcher oldYearMatcher = OLD_YEAR_PATTERN.matcher(partialPage);
		oldYearMatcher.find();
		String name = oldYearMatcher.group(1);
		name = StringEscapeUtils.unescapeHtml4(name);

		logService.info(getClass(), String.format("Downloading title for movie '%s' took %d millis", name, (System.currentTimeMillis() - from)));

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
				logService.warn(getClass(), "Failed retrieving number of viewers for '" + name + "': " + partialPage);
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
	}

	private void downloadImages(final String page, final String imdbUrl, boolean imagesAsync) {
		if (imagesAsync) {
			ExecutorService executorService = Executors.newSingleThreadExecutor();
			executorService.submit(new Runnable() {
				@Override
				public void run() {
					downloadImages(page, imdbUrl);
				}
			});
			executorService.shutdown();
		} else {
			downloadImages(page, imdbUrl);
		}
	}

	@Override
	@Transactional(propagation = Propagation.NOT_SUPPORTED)
	// no need a transaction here, each getImage has its own transaction
	public void downloadImages(String page, String imdbUrl) {
		Matcher matcher = PEOPLE_IMAGES_PATTERN.matcher(page);
		while (matcher.find()) {
			String imageUrl = matcher.group(1);
			getImage(imageUrl);
		}

		// main image
		matcher = MAIN_IMAGE_PATTERN.matcher(page);
		if (matcher.find()) {
			String imageUrl = matcher.group(1);
			getImage(imageUrl);
		} else {
			logService.error(getClass(), "Failed parsing main image of movie: " + imdbUrl);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	// creating a new transaction to store the image quickly and avoid unique index violation due to long transactions
	// need new thread for the new transaction
	public InputStream getImage(final String imageFileName) {
		// remove the imdb url prefix, if exists. and also the rest call prefix - depends on where the call came from we have different prefixes
		String imdbImageUrl = StringUtils.replace(imageFileName, IMDBPreviewCacheServiceImpl.IMDB_IMAGE_URL_PREFIX, "");
		imdbImageUrl = StringUtils.replace(imdbImageUrl, IMDBPreviewCacheServiceImpl.REST_IMAGE_URL_PREFIX, "");
		try {
			InputStream imageInputStream;
			try {
				// first check if it is a no-person-image
				if (imdbImageUrl.equals(IMDBPreviewCacheServiceImpl.IMDB_DEFAULT_PERSON_IMAGE)) {
					imageInputStream = new ClassPathResource(IMDBPreviewCacheServiceImpl.IMDB_DEFAULT_PERSON_IMAGE, this.getClass().getClassLoader()).getInputStream();
				} else {
					Image image = imageDao.find(imdbImageUrl);
					if (image == null) {
						image = new Image(imdbImageUrl, pageDownloader.downloadImage(IMDBPreviewCacheServiceImpl.IMDB_IMAGE_URL_PREFIX + imdbImageUrl));
						imageDao.persist(image);
						logService.debug(getClass(), "Storing a new image into the DB: " + imdbImageUrl);
					}

					imageInputStream = new ByteArrayInputStream(image.getData());
				}
			} catch (Exception e) {
				logService.error(getClass(), String.format("Failed fetching image %s: %s. Using default person-no-image", imdbImageUrl, e.getMessage()), e);
				imageInputStream = new ClassPathResource(IMDBPreviewCacheServiceImpl.IMDB_DEFAULT_PERSON_IMAGE, this.getClass().getClassLoader()).getInputStream();
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
}
