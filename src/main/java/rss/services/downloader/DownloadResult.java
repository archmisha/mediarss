package rss.services.downloader;

import java.util.Collection;

/**
 * User: dikmanm
 * Date: 05/01/13 15:02
 */
public class DownloadResult<T, S> {
	private Collection<T> downloaded;
	private Collection<S> missing;
	private boolean heavy;

	public static <T, S> DownloadResult<T, S> createHeavyDownloadResult() {
		return new DownloadResult<T, S>().withHeavy(true);
	}

	public static <T, S> DownloadResult<T, S> createLightDownloadResult() {
		return new DownloadResult<T, S>().withHeavy(false);
	}

	public void setDownloaded(Collection<T> downloaded) {
		this.downloaded = downloaded;
	}

	public void setMissing(Collection<S> missing) {
		this.missing = missing;
	}

	public Collection<T> getDownloaded() {
		return downloaded;
	}

	public Collection<S> getMissing() {
		return missing;
	}

	public boolean isHeavy() {
		return heavy;
	}

	public DownloadResult<T, S> withHeavy(boolean heavy) {
		this.heavy = heavy;
		return this;
	}
}
