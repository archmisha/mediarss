<div>
    <img src="images/download-blue.png" width="20" height="20" class='movie-torrent-item-download-image'
         title="Download Torrent"/>
    <img src="images/clock.jpg" width="20" height="20" class='movie-torrent-item-scheduled-image'
         title="Torrent Scheduled for Download"/>
    <img src="images/v2.jpg" width="16" height="16" class='movie-torrent-item-downloaded-image'
         title="Torrent Was Already Downloaded"/>
    <span class='movie-torrent-title' title='{{{title}}}'>{{{title}}}</span>
</div>
<div class='movie-torrent-sub-title'>Uploaded on {{dateFormat uploadedDate format="DD/MM/YYYY HH:mm"
    default='never'}}<span class='movie-torrent-scheduled-on'>, Scheduled on <span
            class='movie-torrent-scheduled-on-date'>{{dateFormat scheduledDate format="DD/MM/YYYY HH:mm" default='never'}}</span></span>
</div>