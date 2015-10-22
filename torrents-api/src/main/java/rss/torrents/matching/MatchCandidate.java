package rss.torrents.matching;

/**
* User: dikmanm
* Date: 14/05/13 10:23
*/
public interface MatchCandidate {
	String getText();
	<T> T getObject();
}
