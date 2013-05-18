package rss.services.searchers;

import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import org.apache.commons.collections.IteratorUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rss.entities.*;
import rss.services.PageDownloader;
import rss.services.log.LogService;
import rss.services.matching.MatchCandidate;
import rss.services.matching.MatchingUtils;
import rss.services.requests.subtitles.*;
import rss.services.subtitles.SubtitleLanguage;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * User: dikmanm
 * Date: 11/05/13 17:31
 */
@Service
public class SubCenterSubtitlesSearcher implements Searcher<SubtitlesRequest> {

	public static final String NAME = "www.subscenter.org";
	public static final String SEARCH_URL = "http://" + NAME + "/he/subtitle/search/?q=";
	public static final String SHOW_ENTRY_URL = "http://" + NAME + "/%s/%d/%d";
	public static final String MOVIE_ENTRY_URL = "http://" + NAME + "/%s";
	public static final String DATA_URL = "http://" + NAME + "/he/subtitle/download/%s/%d/?v=%s&key=%s";
	private static final Pattern ENTRY_FOUND_PATTERN = Pattern.compile("subtitles_info");

	@Autowired
	private PageDownloader pageDownloader;

	@Autowired
	private LogService logService;

	@Override
	public String getName() {
		return NAME;
	}

	@Override
	public SearchResult search(SubtitlesRequest subtitlesRequest) {
		try {
			// first find the url for the show, if don't have it yet
			if (subtitlesRequest instanceof SubtitlesEpisodeRequest) {
				SubtitlesEpisodeRequest ser = (SubtitlesEpisodeRequest) subtitlesRequest;
				Show show = ser.getShow();

				if (show.getSubCenterUrl() == null && show.getSubCenterUrlScanDate() == null) {
					Pair<String, String> pair = getSubCenterShowUrl(subtitlesRequest.getName(), true);
					String subCenterShowUrl = pair.getValue();

					show.setSubCenterUrl(subCenterShowUrl);
					show.setSubCenterUrlScanDate(new Date());
					logService.info(getClass(), "SubCenter show url: " + subCenterShowUrl);

					// cant parse the entry page here, cuz should add season and episode to the url
				}

				if (show.getSubCenterUrl() == null) {
					return SearchResult.createNotFound();
				}

				if (subtitlesRequest instanceof SubtitlesSingleEpisodeRequest) {
					SubtitlesSingleEpisodeRequest sser = (SubtitlesSingleEpisodeRequest) subtitlesRequest;
					String entryUrl = String.format(SHOW_ENTRY_URL, show.getSubCenterUrl(), ser.getSeason(), sser.getEpisode());
					String page = pageDownloader.downloadPageUntilFound(entryUrl, ENTRY_FOUND_PATTERN);
					return downloadSubtitles(subtitlesRequest, page);
				}
			} else if (subtitlesRequest instanceof SubtitlesMovieRequest) {
				SubtitlesMovieRequest smr = (SubtitlesMovieRequest) subtitlesRequest;
				Movie movie = smr.getMovie();

				if (movie.getSubCenterUrl() == null && movie.getSubCenterUrlScanDate() == null) {
					String name = subtitlesRequest.getName();
					// strip the year brackets of the movie
					int idx = name.indexOf('(');
					if (idx > -1) {
						name = name.substring(0, idx).trim();
					}

					Pair<String, String> pair = getSubCenterShowUrl(name, false);
					String subCenterShowUrl = pair.getValue();

					movie.setSubCenterUrl(subCenterShowUrl);
					movie.setSubCenterUrlScanDate(new Date());
					logService.info(getClass(), "SubCenter show url: " + subCenterShowUrl);

					// if also got already the entry page, can parse it right now
					if (pair.getKey() != null) {
						downloadSubtitles(subtitlesRequest, pair.getKey());
					}
				}

				if (movie.getSubCenterUrl() == null) {
					return SearchResult.createNotFound();
				}

				String entryUrl = String.format(MOVIE_ENTRY_URL, movie.getSubCenterUrl());
				String page = pageDownloader.downloadPageUntilFound(entryUrl, ENTRY_FOUND_PATTERN);
				return downloadSubtitles(subtitlesRequest, page);
			}
		} catch (Exception e) {
			logService.error(getClass(), e.getMessage(), e);
		}
		return SearchResult.createNotFound();
	}

	private SearchResult downloadSubtitles(SubtitlesRequest subtitlesRequest, String page) throws Exception {
		SearchResult searchResult = new SearchResult(NAME);
		for (SubtitleLanguage subtitleLanguage : subtitlesRequest.getLanguages()) {
			SubCenterSubtitles foundSubtitles = parseEntryPage(subtitlesRequest, page, subtitleLanguage);

			// for the best result only, download the actual subtitles
			byte[] data = pageDownloader.downloadData(String.format(DATA_URL, toSubCenterLanguages(subtitleLanguage),
					foundSubtitles.getId(), URLEncoder.encode(foundSubtitles.getName(), "UTF-8"), foundSubtitles.getKey()));

			Subtitles subtitles;
			if (subtitlesRequest instanceof SubtitlesMovieRequest) {
				subtitles = new Subtitles();
			} else if (subtitlesRequest instanceof SubtitlesSingleEpisodeRequest) {
				SubtitlesSingleEpisodeRequest sser = (SubtitlesSingleEpisodeRequest) subtitlesRequest;
				subtitles = new SingleEpisodeSubtitles(sser.getSeason(), sser.getEpisode());
			} else if (subtitlesRequest instanceof SubtitlesDoubleEpisodeRequest) {
				SubtitlesDoubleEpisodeRequest sder = (SubtitlesDoubleEpisodeRequest) subtitlesRequest;
				subtitles = new DoubleEpisodeSubtitles(sder.getSeason(), sder.getEpisode1(), sder.getEpisode2());
			} else {
				throw new InvalidParameterException("Unsupported request type: " + subtitlesRequest.getClass());
			}
			subtitles.setLanguage(foundSubtitles.getLanguage());
			subtitles.setFileName(foundSubtitles.getName());
			subtitles.setExternalId(String.valueOf(foundSubtitles.getId()));
			subtitles.getTorrentIds().add(subtitlesRequest.getTorrent().getId());
			subtitles.setDateUploaded(foundSubtitles.getDateUploaded());
			subtitles.setData(data);

			logService.info(getClass(), "FINAL: '" + foundSubtitles + "' for '" + subtitlesRequest.getTorrent().getTitle() + "'");
			searchResult.addDownloadable(subtitles);
		}
		return searchResult;
	}

	private SubCenterSubtitles parseEntryPage(SubtitlesRequest mediaRequest, String page, SubtitleLanguage subtitleLanguage) throws Exception {
		// parse subtitles found on page
		int idx = page.indexOf("subtitles_groups = ") + "subtitles_groups = ".length();
		String subsInfo = page.substring(idx, page.indexOf("subtitles_info", idx)).trim();
		ObjectMapper mapper = new ObjectMapper();
		JsonNode jsonNode = mapper.readTree(subsInfo);

		// for the same subtitles name, pick the one with the most downloads
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy ,HH:mm");
		Map<String, SubCenterSubtitles> results = new HashMap<>();
		for (JsonNode node1 : IteratorUtils.toList(jsonNode.get(toSubCenterLanguages(subtitleLanguage)).getElements())) {
			for (JsonNode node2 : IteratorUtils.toList(node1.getElements())) {
				for (JsonNode node3 : IteratorUtils.toList(node2.getElements())) {
					long id = node3.get("id").getIntValue();
					String key = node3.get("key").getTextValue();
					String name = node3.get("subtitle_version").getTextValue();
					int downloaded = node3.get("downloaded").getIntValue();
					Date dateUploaded = DATE_FORMAT.parse(node3.get("created_on").getTextValue());

					if (!name.endsWith(".srt")) {
						// zip suffix is not ended to the subtitles, it is default
						name += ".zip";
					}

					// remove file suffixes
					String nameWithoutSuffix = name;
					for (String suffix : Arrays.asList(".zip", ".srt")) {
						if (name.endsWith(suffix)) {
							nameWithoutSuffix = name.substring(0, name.length() - suffix.length());
							break;
						}
					}

					// take the best downloaded value
					if (!results.containsKey(nameWithoutSuffix) || results.get(nameWithoutSuffix).getDownloaded() < downloaded) {
						results.put(nameWithoutSuffix, new SubCenterSubtitles(id, name, key, downloaded, SubtitleLanguage.HEBREW, dateUploaded));
					}
				}
			}
		}

		String[] keywords = new String[]{"dvdrip"};
		Map<String, Boolean> keywordMatches = new HashMap<>();
		for (String keyword : keywords) {
			keywordMatches.put(keyword, mediaRequest.getTorrent().getTitle().toLowerCase().contains("dvdrip"));
		}

		for (Map.Entry<String, SubCenterSubtitles> entry : new ArrayList<>(results.entrySet())) {
			String name = entry.getKey().toLowerCase();

			// determine quality
			MediaQuality quality = SearcherUtils.findQuality(name);
			if (quality != mediaRequest.getTorrent().getQuality()) {
				logService.info(getClass(), "Skipping " + name + " - quality doesn't match");
				results.remove(entry.getKey());
			} else {
				// for those keywords that are true - exist in the torrent, need to verify they are in the subtitles
				for (Map.Entry<String, Boolean> entry2 : keywordMatches.entrySet()) {
					if (entry2.getValue() && !name.contains(entry2.getKey())) {
						logService.info(getClass(), "Skipping " + name + " - keyword " + entry2.getKey() + " doesn't match");
						results.remove(entry.getKey());
					}
				}
			}
		}

		logService.info(getClass(), "left with: " + StringUtils.join(results.values(), ", "));
//		ArrayList<SubCenterSubtitles> list = new ArrayList<>(results.values());
//		Collections.sort(list, new Comparator<SubCenterSubtitles>() {
//			@Override
//			public int compare(SubCenterSubtitles o1, SubCenterSubtitles o2) {
//				return Ints.compare(o2.getDownloaded(), o1.getDownloaded());
//			}
//		});
//		SubCenterSubtitles bestResult = list.get(0);

		// using the entries and not the values, cuz in the key of the map there is the names without the .srt / .zip suffix which is better for matching
		SubCenterSubtitles bestResult = MatchingUtils.filterByLevenshteinDistance(mediaRequest.getTorrent().getTitle(), Collections2.transform(results.entrySet(),
				new Function<Map.Entry<String, SubCenterSubtitles>, MatchCandidate>() {
					@Override
					public MatchCandidate apply(final Map.Entry<String, SubCenterSubtitles> entry) {
						return new MatchCandidate() {
							@Override
							public String getText() {
								return entry.getKey();
							}

							@SuppressWarnings({"unchecked"})
							@Override
							public <T> T getObject() {
								return (T) entry.getValue();
							}
						};
					}
				}), logService).getObject();

		return bestResult;
	}

	private Pair<String, String> getSubCenterShowUrl(final String name, boolean isShow) {
		try {
			String encodedName = StringUtils.replace(name, "'", "");
			encodedName = URLEncoder.encode(encodedName, "UTF-8");
			Pair<String, String> pair = pageDownloader.downloadPageWithRedirect(SEARCH_URL + encodedName);
			String page = pair.getKey();
			// if got redirect url, then found the match and also downloaded the entry page
			if (pair.getValue() != null) {
				String newValue = pair.getValue().substring(pair.getValue().indexOf(NAME) + NAME.length());
				newValue = StringUtils.strip(newValue, "/");
				return new ImmutablePair<>(pair.getKey(), newValue);
			}
			Document doc = Jsoup.parse(page);

			// figure out how many pages there are, default is 1
			int pages = 1;
			Elements select = doc.select(".minibuttonpage");
			if (select.size() > 0) {
				String pagesPart = select.get(0).children().get(0).html();
				//		System.out.println(pagesPart.split(" ")[5]);
				pages = Integer.parseInt(pagesPart.split(" ")[5]);
			}

			List<SubCenterSearchResult> searchResults = new ArrayList<>();
			searchResults.addAll(parseSearchResultsPage(doc));
			for (int i = 1; i <= pages; ++i) {
				page = pageDownloader.downloadPage(SEARCH_URL + encodedName + "&page=" + i);
				doc = Jsoup.parse(page);
				searchResults.addAll(parseSearchResultsPage(doc));
			}

			for (SubCenterSearchResult searchResult : new ArrayList<>(searchResults)) {
				if ((isShow && !searchResult.isShow()) ||
					(!isShow && searchResult.isShow())) {
					searchResults.remove(searchResult);
				}
			}

			if (searchResults.isEmpty()) {
				return new ImmutablePair<>(null, null);
			}

			SubCenterSearchResult subCenterSearchResult = MatchingUtils.filterByLevenshteinDistance(name, Collections2.transform(searchResults,
					new Function<SubCenterSearchResult, MatchCandidate>() {
						@Override
						public MatchCandidate apply(final SubCenterSearchResult subCenterSearchResult) {
							return new MatchCandidate() {
								@Override
								public String getText() {
									return subCenterSearchResult.getName();
								}

								@SuppressWarnings("unchecked")
								@Override
								public <T> T getObject() {
									return (T) subCenterSearchResult;
								}
							};
						}
					}), logService).getObject();

			if (subCenterSearchResult != null) {
				logService.info(getClass(), "Filtered candidates: " + subCenterSearchResult);
				return new ImmutablePair<>(null, subCenterSearchResult.getSubCenterUrl());
			}
		} catch (UnsupportedEncodingException e) {
			logService.error(getClass(), "Failed downloading SubCenter url for '" + name + "':" + e.getMessage(), e);
		}

		return new ImmutablePair<>(null, null);
	}

	private Collection<SubCenterSearchResult> parseSearchResultsPage(Document doc) {
		Collection<SubCenterSearchResult> results = new ArrayList<>();
		for (Element element : doc.select(".generalWindowRight a")) {
			String href = element.attr("href");
			if (href.endsWith("/")) {
				href = href.substring(0, href.length() - 1);
			}
			boolean isShow = href.contains("/series/");
			String showName = href.substring(href.lastIndexOf("/") + 1);
//			name = ShowServiceImpl.normalize(name);
			SubCenterSearchResult result = new SubCenterSearchResult(href, isShow, showName);
			results.add(result);
		}

		return results;
	}

	private String toSubCenterLanguages(SubtitleLanguage language) {
		switch (language) {
			case HEBREW:
				return "he";
			case ENGLISH:
				return "en";
			default:
				throw new InvalidParameterException("SubCenter doesn't support language: " + language);
		}
	}

	private class SubCenterSearchResult {
		private String subCenterUrl;
		private boolean isShow;
		private String name;

		public SubCenterSearchResult(String subCenterUrl, boolean isShow, String name) {
			this.subCenterUrl = subCenterUrl;
			this.isShow = isShow;
			this.name = name;
		}

		private String getSubCenterUrl() {
			return subCenterUrl;
		}

		private boolean isShow() {
			return isShow;
		}

		private String getName() {
			return name;
		}

		@Override
		public String toString() {
			return "SubCenterSearchResult{" +
				   "subCenterUrl='" + subCenterUrl + '\'' +
				   ", isShow=" + isShow +
				   ", name='" + name + '\'' +
				   '}';
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SubCenterSearchResult that = (SubCenterSearchResult) o;

			if (subCenterUrl != null ? !subCenterUrl.equals(that.subCenterUrl) : that.subCenterUrl != null)
				return false;

			return true;
		}

		@Override
		public int hashCode() {
			return subCenterUrl != null ? subCenterUrl.hashCode() : 0;
		}
	}

	private class SubCenterSubtitles {
		private long id;
		private String name;
		private String key;
		private int downloaded;
		private SubtitleLanguage language;
		private Date dateUploaded;

		public SubCenterSubtitles(long id, String name, String key, int downloaded, SubtitleLanguage language, Date dateUploaded) {
			this.id = id;
			this.name = name;
			this.key = key;
			this.downloaded = downloaded;
			this.language = language;
			this.dateUploaded = dateUploaded;
		}

		private String getName() {
			return name;
		}

		private String getKey() {
			return key;
		}

		@Override
		public boolean equals(Object o) {
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			SubCenterSubtitles that = (SubCenterSubtitles) o;

			if (!key.equals(that.key)) return false;

			return true;
		}

		@Override
		public int hashCode() {
			return key.hashCode();
		}

		public int getDownloaded() {
			return downloaded;
		}

		private Date getDateUploaded() {
			return dateUploaded;
		}

		@Override
		public String toString() {
			return "name='" + name + '\'' + ", downloaded=" + downloaded;
		}

		public SubtitleLanguage getLanguage() {
			return language;
		}

		public long getId() {
			return id;
		}
	}
}
