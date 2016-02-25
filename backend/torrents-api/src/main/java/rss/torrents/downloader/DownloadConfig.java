package rss.torrents.downloader;

/**
 * User: dikmanm
 * Date: 26/10/13 13:37
 */
public class DownloadConfig {
	private boolean forceDownload;

	// if true, run heavy searches asynchronously
	private boolean asyncHeavy;

	public DownloadConfig() {
		asyncHeavy = false;
		forceDownload = false;
	}

	public boolean isAsyncHeavy() {
		return asyncHeavy;
	}

	public void setAsyncHeavy(boolean asyncHeavy) {
		this.asyncHeavy = asyncHeavy;
	}

	public boolean isForceDownload() {
		return forceDownload;
	}

	public void setForceDownload(boolean forceDownload) {
		this.forceDownload = forceDownload;
	}
}
