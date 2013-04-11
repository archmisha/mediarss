<div class='movie-item-inner'>
    <img src="images/pending.jpg" width="16" height="16" class='movie-item-scheduled-image'
          title="Movie Scheduled For Download"/>
    <img src="images/complete.png" width="16" height="16" class='movie-item-downloaded-image'
         title="Movie Was Already Downloaded"/>
    <img src="images/clock.jpg" width="16" height="16" class='movie-item-future-image'
         title="The Movie Will Be Downloaded As Soon As It Is Out"/>
    <span class='movie-item-title' title='{{{title}}}'>{{{title}}}</span>
    <a href='rest/movies/imdb/{{id}}' class='movie-show-preview'><img src='images/preview.gif' class='movie-show-preview-image'/></a>
    <img src='images/remove.png' class='future-movie-item-remove-image'/>
</div>
<div class='movie-sub-title'>
    <span class='movie-scheduled-on'>Scheduled on {{dateFormat scheduledDate format="DD/MM/YYYY HH:mm" default='never'}}</span>
</div>