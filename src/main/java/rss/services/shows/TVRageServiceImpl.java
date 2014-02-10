package rss.services.shows;

import com.thoughtworks.xstream.XStream;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import rss.MediaRSSException;
import rss.RecoverableConnectionException;
import rss.entities.Episode;
import rss.entities.Show;
import rss.services.PageDownloader;
import rss.services.log.LogService;
import rss.util.DurationMeter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * User: dikmanm
 * Date: 25/02/13 09:05
 */
@SuppressWarnings("unchecked")
//@Service("tVRageServiceImpl")
public class TVRageServiceImpl implements ShowsProvider {

	public static final String SERVICES_HOSTNAME = "services.tvrage.com";
	public static final String SHOW_LIST_URL = "http://" + SERVICES_HOSTNAME + "/feeds/show_list.php";
	public static final String SHOW_INFO_URL = "http://" + SERVICES_HOSTNAME + "/feeds/full_show_info.php?sid=";
	public static final String SHOW_SCHEDULE_URL = "http://" + SERVICES_HOSTNAME + "/feeds/fullschedule.php?country=US&24_format=1";

	private static final int RETURNING_STATUS = 1;
	private static final int CANCELED_ENDED_STATUS = 2;
	private static final int TBD_STATUS = 3;
	private static final int IN_DEV_STATUS = 4;
	private static final int NEW_SERIES_STATUS = 7;
	private static final int ENDED2_STATUS = 11;
	private static final int CANCELED_STATUS = 13;
	private static final int ENDED_STATUS = 14;

	private PageDownloader pageDownloader;

	private LogService logService;

	public void setPageDownloader(PageDownloader pageDownloader) {
		this.pageDownloader = pageDownloader;
	}

	public void setLogService(LogService logService) {
		this.logService = logService;
	}

	@Override
	public Show search(String name) {
		throw new UnsupportedOperationException("TVRageServiceImpl doesn't support search");
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Collection<Episode> downloadSchedule() {
		Collection<Episode> result = new ArrayList<>();

		String page = pageDownloader.downloadPage(SHOW_SCHEDULE_URL);

		XStream xstream = new XStream();
		xstream.alias("schedule", List.class);
		xstream.alias("DAY", TVRageDay.class);
		xstream.useAttributeFor(TVRageDay.class, "attr");
		xstream.addImplicitCollection(TVRageDay.class, "times");
		xstream.alias("time", TVRageTime.class);
		xstream.addImplicitCollection(TVRageTime.class, "shows");
		xstream.alias("show", TVRageShowSchedule.class);
		xstream.useAttributeFor(TVRageShowSchedule.class, "name");

		List<TVRageDay> tvRageDays = (List<TVRageDay>) xstream.fromXML(page);

		// date format: 2013-2-25
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
		for (TVRageDay tvRageDay : tvRageDays) {
			try {
				Date airDate = sdf.parse(tvRageDay.getAttr());
				for (TVRageTime tvRageTime : tvRageDay.getTimes()) {
					for (TVRageShowSchedule tvRageShowSchedule : tvRageTime.getShows()) {
						Show show = new Show(tvRageShowSchedule.getName());
						show.setTvRageId(tvRageShowSchedule.getSid());

						String[] arr = tvRageShowSchedule.getEp().split("x");
						// currently skipping 'S01-Special' all around the app
						if (arr.length == 2) {
							int season = Integer.parseInt(arr[0]);
							int episodeNum = Integer.parseInt(arr[1]);
							Episode episode = new Episode(season, episodeNum);
							episode.setAirDate(airDate);
							episode.setShow(show);

							result.add(episode);
						}
					}
				}
			} catch (ParseException e) {
				logService.warn(getClass(), "Failed parsing air date: " + tvRageDay.getAttr() + ": " + e.getMessage(), e);
			}
		}

		return result;
	}

	@Override
	public Collection<Show> downloadShowList() {
		Collection<Show> shows = new ArrayList<>();

		DurationMeter duration = new DurationMeter();
		String page = pageDownloader.downloadPage(SHOW_LIST_URL);
		duration.stop();
		logService.info(getClass(), "Downloading the show list from tvrage.com took " + duration.getDuration() + " ms");

		XStream xstream = new XStream();
		xstream.alias("shows", List.class);
		xstream.alias("show", TVRageShow.class);
		List<TVRageShow> tvRageShows = (List<TVRageShow>) xstream.fromXML(page);
		for (TVRageShow tvRageShow : tvRageShows) {
			// not skipping here TBD status, cuz in between seasons a show might go into that status and then continue
			if (tvRageShow.getStatus() == IN_DEV_STATUS) {
				continue;
			}
			Show show = new Show(tvRageShow.getName());
			show.setTvRageId(tvRageShow.getId());
			show.setEnded(tvRageShow.getStatus() != RETURNING_STATUS &&
						  tvRageShow.getStatus() != NEW_SERIES_STATUS);
			shows.add(show);
		}

		return shows;
	}

	@Override
	public Collection<Episode> downloadSchedule(Show show) {
		try {
			Collection<Episode> episodes = new ArrayList<>();
			String page = pageDownloader.downloadPage(SHOW_INFO_URL + show.getTvRageId());

			XStream xstream = new XStream();
			xstream.alias("Show", TVRageShowInfo.class);
			// suddenly they changed to showinfo...
//			xstream.alias("Showinfo", TVRageShowInfo.class);
			xstream.alias("Episodelist", TVRageEpisodeList.class);
			xstream.alias("Season", TVRageSeason.class);
			xstream.alias("episode", TVRageEpisode.class);
			xstream.alias("genre", String.class);
			xstream.alias("Special", TVRageSpecial.class);
			xstream.alias("aka", String.class);
			xstream.alias("Movie", TVRageMovie.class);
			xstream.addImplicitCollection(TVRageEpisodeList.class, "seasons");
			xstream.addImplicitCollection(TVRageSeason.class, "episodes");
			xstream.addImplicitCollection(TVRageSpecial.class, "episodes");
			xstream.addImplicitCollection(TVRageMovie.class, "episodes");
			xstream.useAttributeFor(TVRageSeason.class, "no");

			TVRageShowInfo tvRageShowInfo = (TVRageShowInfo) xstream.fromXML(page);
			show.setEnded(!StringUtils.isBlank(tvRageShowInfo.getEnded()));

			if (tvRageShowInfo.getEpisodelist() == null) {
				logService.debug(getClass(), "Show '" + show.getName() + "' has no episodes in TVRage!");
			} else {
				List<TVRageSeason> tvRageSeasons = tvRageShowInfo.getEpisodelist().getSeasons();
				for (Object obj : tvRageSeasons) {
					// implicit collection shit - skip movie and special
					if (!(obj instanceof TVRageSeason)) {
						continue;
					}

					TVRageSeason tvRageSeason = (TVRageSeason) obj;
					for (TVRageEpisode tvRageEpisode : tvRageSeason.getEpisodes()) {
						Episode episode = new Episode();
						episode.setSeason(tvRageSeason.getNo());
						episode.setEpisode(Integer.parseInt(tvRageEpisode.getSeasonnum()));
						// 2011-06-23
						if (tvRageEpisode.getAirdate().equals("0000-00-00")) {
							episode.setAirDate(null);
						} else {
							try {
								SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
								episode.setAirDate(sdf.parse(tvRageEpisode.getAirdate()));
							} catch (ParseException e) {
								logService.warn(getClass(), "Failed parsing air date: " + tvRageEpisode.getAirdate() + ": " + e.getMessage(), e);
								episode.setAirDate(null);
							}
						}
						episodes.add(episode);
					}
				}
			}
			return episodes;
		} catch (RecoverableConnectionException e) {
			throw e;
		} catch (Exception e) {
			throw new MediaRSSException("Failed to download schedule for show: " + show, e).withUserMessage("Failed to download schedule for show: " + show.getName());
		}
	}

	private class TVRageShowSchedule {
		private String name;
		private int sid;
		private String network;
		private String title;
		// 01x112
		private String ep;
		private String link;

		public String getName() {
			return name;
		}

		public int getSid() {
			return sid;
		}

		public String getEp() {
			return ep;
		}
	}

	private class TVRageTime {
		private List<TVRageShowSchedule> shows;

		public List<TVRageShowSchedule> getShows() {
			return shows;
		}
	}

	private class TVRageDay {
		private String attr;
		private List<TVRageTime> times;

		public String getAttr() {
			return attr;
		}

		public List<TVRageTime> getTimes() {
			return times;
		}
	}

	private class TVRageShowInfo {
		private int showid;
		private String name;
		private String showname;
		private int totalseasons;
		private int seasons;
		private String showlink;
		private Object started;
		private Object startdate;
		private String ended;
		private String image;
		private String origin_country;
		private String status;
		private String classification;
		private List<String> genres;
		private List<String> akas;
		private int runtime;
		private String network;
		private Object airtime;
		private String airday;
		private String timezone;
		private TVRageEpisodeList Episodelist;

		public TVRageEpisodeList getEpisodelist() {
			return Episodelist;
		}

		public String getEnded() {
			return ended;
		}
	}

	private class TVRageSpecial {
		private List<TVRageEpisode> episodes;
	}

	private class TVRageEpisodeList {
		private List<TVRageSeason> seasons;
		private TVRageMovie movie;
		private TVRageSpecial special;

		public List<TVRageSeason> getSeasons() {
			return seasons;
		}
	}

	private class TVRageMovie {
		private List<TVRageEpisode> episodes;
	}

	private class TVRageSeason {
		private int no;
		private List<TVRageEpisode> episodes;

		public int getNo() {
			return no;
		}

		public void setNo(int no) {
			this.no = no;
		}

		public List<TVRageEpisode> getEpisodes() {
			return episodes;
		}

		public void setEpisodes(List<TVRageEpisode> episodes) {
			this.episodes = episodes;
		}
	}

	private class TVRageEpisode {
		private int epnum;
		private String seasonnum;
		private String prodnum;
		private String airdate;
		private String link;
		private String title;
		private String screencap;
		// for movies part
		private int season;
		private int runtime;

		public int getEpnum() {
			return epnum;
		}

		public String getSeasonnum() {
			return seasonnum;
		}

		public String getProdnum() {
			return prodnum;
		}

		public String getAirdate() {
			return airdate;
		}

		public String getLink() {
			return link;
		}

		public String getTitle() {
			return title;
		}

		public String getScreencap() {
			return screencap;
		}
	}

	private class TVRageShow {
		private int id;
		private String name;
		private String country;
		private int status;

		public int getId() {
			return id;
		}

		public String getName() {
			return name;
		}

		public String getCountry() {
			return country;
		}

		public int getStatus() {
			return status;
		}
	}
}
