package rss.services.downloader;

import java.util.ArrayList;
import java.util.Collection;

/**
* User: dikmanm
* Date: 05/01/13 15:02
*/
public class DownloadResult<T, S> {
	private Collection<T> downloaded;
	private Collection<S> missing;

	public DownloadResult(Collection<T> downloaded, Collection<S> missing) {
		this.downloaded = new ArrayList<>(downloaded);
		this.missing = new ArrayList<>(missing);
	}

	public Collection<T> getDownloaded() {
		return downloaded;
	}

	public Collection<S> getMissing() {
		return missing;
	}
}
