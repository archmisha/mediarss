package rss.entities;


import javax.persistence.*;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

/**
 * User: Michael Dikman
 * Date: 04/12/12
 * Time: 18:48
 */
@MappedSuperclass
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Media extends BaseEntity {
	private static final long serialVersionUID = 2655420980314962072L;

	//    @OneToMany(mappedBy = "media", targetEntity = Torrent.class)
	@ElementCollection(fetch = FetchType.EAGER)
	private Set<Long> torrentIds;

	public Media() {
		torrentIds = new HashSet<>();
	}

	public abstract String getName();

	public Set<Long> getTorrentIds() {
		return torrentIds;
	}
}
