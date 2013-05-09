<div class='movie-item-icon-wrapper'>
    <img src="images/pending.png" width="24" height="24" class='movie-item-scheduled-image'
         title="Movie Scheduled For Download"/>
    <img src="images/complete.png" width="20" height="20" class='movie-item-downloaded-image'
         title="Movie Was Already Downloaded"/>
    <img src="images/clock.jpg" width="16" height="16" class='movie-item-future-image'
         title="The Movie Will Be Downloaded As Soon As It Is Out"/>
</div>
<div style="margin-left:30px;">
    <div class='movie-item-inner'>
        <span class='movie-item-title' title='{{{title}}}'>{{{title}}}</span>
        <a href='rest/movies/imdb/{{id}}' class='movie-show-preview-{{id}}'><img src='images/preview.gif'
                                                                                 class='movie-show-preview-image'/></a>
        <img src='images/remove.png' class='future-movie-item-remove-image'/>
    </div>
    <div class='movie-sub-title'>
        <span class='movie-scheduled-on'>Scheduled on {{dateFormat scheduledDate format="DD/MM/YYYY HH:mm" default='never'}}</span>
    </div>
</div>
