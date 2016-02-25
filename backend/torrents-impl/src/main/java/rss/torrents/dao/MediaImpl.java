package rss.torrents.dao;


import rss.ems.entities.BaseEntity;
import rss.torrents.Media;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 18:48
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class MediaImpl extends BaseEntity implements Media {
	private static final long serialVersionUID = 2655420980314962072L;

	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Long> torrentIds;

	protected MediaImpl() {
		torrentIds = new HashSet<>();
	}

	public abstract String getName();

	public Set<Long> getTorrentIds() {
		return torrentIds;
	}
}
