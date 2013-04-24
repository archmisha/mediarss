package rss.dao;

import org.springframework.stereotype.Repository;
import rss.SubtitleLanguage;
import rss.entities.Episode;
import rss.entities.Image;
import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.requests.DoubleEpisodeRequest;
import rss.services.requests.EpisodeRequest;
import rss.services.requests.FullSeasonRequest;
import rss.services.requests.SingleEpisodeRequest;

import java.util.*;

/**
 * User: Michael Dikman
 * Date: 24/05/12
 * Time: 11:05
 */
@Repository
public class ImageDaoImpl extends BaseDaoJPA<Image> implements ImageDao {

	@Override
	public Image find(String key) {
		Map<String, Object> params = new HashMap<>(1);
		params.put("key", key);
		return uniqueResult(super.<Image>findByNamedQueryAndNamedParams("Image.findByKey", params));
	}
}
