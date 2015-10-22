package rss.cache;

import rss.shows.CachedShow;

/**
 * User: dikmanm
 * Date: 12/05/13 19:37
 */
public class CachedShowSubsetSet {

    private CachedShow cachedShow;
    private CachedShowSubset[] subsets;

    public CachedShowSubsetSet(CachedShow cachedShow, CachedShowSubset[] subsets) {
        this.cachedShow = cachedShow;
        this.subsets = subsets;
    }

    public CachedShow getCachedShow() {
        return cachedShow;
    }

    public CachedShowSubset[] getSubsets() {
        return subsets;
    }
}
