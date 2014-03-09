<div class='movie-item-root'>
    <div class='movie-item-icon-wrapper'>
        <img src="images/pending.png" width="24" height="24" class='movie-item-scheduled-image'
             title="Movie Scheduled For Download"/>
        <img src="images/complete.png" width="20" height="20" class='movie-item-downloaded-image'
             title="Movie Was Already Downloaded"/>
        <img src="images/clock.jpg" width="16" height="16" class='movie-item-future-image'
             title="The Movie Will Be Downloaded As Soon As It Is Out"/>
        <img src="images/searching.png" width="18" height="18" class='movie-item-searching-image'
             title="Movie search is in progress"/>
    </div>
    <div class='movie-item-title-wrapper'>
        <div class='movie-item-title'>
            <div class='movie-item-long-wrapper'>
                <div class='movie-item-title-text' title="{{{escapedTitle}}}">{{{title}}}</div>
                <span class='movie-item-new-label'>{{notViewedTorrentsCounter}} new</span>
                <span class='future-movie-item-remove-image'>remove</span>
            </div>
            <span class='movie-item-title-text-short' title="{{{escapedTitle}}}">{{{title}}}<span
                    class='movie-item-new-label-short'>{{notViewedTorrentsCounter}} new</span>
                <span class='future-movie-item-remove-image-short'>remove</span>
            </span>
            <img src='images/preview.gif' class='movie-show-preview movie-show-preview-image'/>
            <img src='images/preview.gif' class='movie-show-preview-small movie-show-preview-image'/>
        </div>
        <div class='movie-item-sub-title'>
            {{#if scheduledDate}}
            <span class='movie-scheduled-on'>Scheduled on {{dateFormat scheduledDate format="DD/MM/YYYY HH:mm" default='never'}}</span>
            {{/if}}
        </div>
    </div>
</div>
<div class='movie-item-torrents'></div>
<div class='movie-item-summary'>
    {{torrentsLabel}}. <span class='movie-item-torrents-show-all'>show all</span>
    <span class='movie-item-torrents-collapse'>collapse</span>
</div>