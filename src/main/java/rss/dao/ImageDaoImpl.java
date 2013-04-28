package rss.dao;

import org.springframework.stereotype.Repository;
import rss.entities.Image;

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
