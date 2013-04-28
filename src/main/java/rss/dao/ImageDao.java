package rss.dao;

import rss.entities.Image;

/**
 * User: Michael Dikman
 * Date: 12/05/12
 * Time: 15:30
 */
public interface ImageDao extends Dao<Image> {

	Image find(String key);
}
