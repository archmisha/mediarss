package rss.services;

import rss.entities.Subtitles;
import rss.entities.Torrent;

import java.util.Collection;

/**
 * User: Michael Dikman
 * Date: 24/11/12
 * Time: 17:02
 */
public interface RssFeedBuilder {

	String build(String feedTitle, String feedDescription, Collection<? extends Torrent> torrentEntries, Collection<Subtitles> subtitles);
}
