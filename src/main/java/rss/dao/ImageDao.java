package rss.dao;

import rss.SubtitleLanguage;
import rss.entities.Episode;
import rss.entities.Image;
import rss.entities.Show;
import rss.entities.Torrent;
import rss.services.requests.EpisodeRequest;

import java.util.Collection;
import java.util.List;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface ImageDao extends Dao<Image> {

	Image find(String key);
}
