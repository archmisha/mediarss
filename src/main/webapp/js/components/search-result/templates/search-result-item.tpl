<div class='search-result-item-status'>
    <img src="images/download-blue.png" width="20" height="20" class='search-result-item-download-image'
         title="Download Torrent"/>
    <img src="images/pending.png" width="24" height="24" class='search-result-item-scheduled-image'
         title="Torrent Scheduled for Download"/>
    <img src="images/complete.png" width="20" height="20" class='search-result-item-downloaded-image'
         title="Torrent Was Already Downloaded"/>
</div>
<div class='search-result-item-body' title='{{{escapedTitle}}}'>
<div class='search-result-item-title'>{{{title}}}</div>
    <div class='search-result-item-sub-title'>Uploaded on {{dateFormat uploadedDate format="DD/MM/YYYY HH:mm"
        default='never'}}<span class='search-result-item-size'><span
                class='search-result-item-size-large'>, </span><span class='search-result-item-size-small'></span>Size: {{size}} MB</span><span
                class='search-result-item-scheduled-on'>, Scheduled on <span
                class='search-result-item-scheduled-on-date'>{{dateFormat scheduledDate format="DD/MM/YYYY HH:mm" default='never'}}</span></span>
    </div>
</div>
