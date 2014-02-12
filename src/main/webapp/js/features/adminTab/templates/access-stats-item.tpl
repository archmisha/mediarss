<div class="access-stats-item-large">
    <div class='username-column' title="{{firstName}} {{lastName}} ({{email}})">{{firstName}} {{lastName}} ({{email}})
    </div>
    <div class='last-login-column'>{{dateFormat lastLoginFormatted format="DD/MM/YYYY HH:mm" default='never'}}</div>
    <div class='last-shows-feed-access-column'>{{dateFormat lastShowsFeedGeneratedFormatted format="DD/MM/YYYY HH:mm"
        default='never'}}
    </div>
    <div class='last-movies-feed-access-column'>{{dateFormat lastMoviesFeedGeneratedFormatted format="DD/MM/YYYY HH:mm"
        default='never'}}
    </div>
    <div class='impersonate-button-column'>
        <div class='btn btn-small impersonate-button'>Impersonate</div>
        <div class='btn btn-small impersonate-button-disabled'>Logged in</div>
    </div>
</div>
<div class="access-stats-item-small">
    <div class="access-stats-item-small-name">{{firstName}} {{lastName}} ({{email}}):</div>
    <div class="access-stats-item-small-data">
        <div>
            <div class="access-stats-item-small-data-row-name">Login:</div>
            <span class="access-stats-item-small-data-row-value">{{dateFormat lastLoginFormatted format="DD/MM/YYYY HH:mm" default='never'}}</span>
        </div>
        <div>
            <div class="access-stats-item-small-data-row-name">Shows RSS:</div>
            <span class="access-stats-item-small-data-row-value">{{dateFormat lastShowsFeedGeneratedFormatted format="DD/MM/YYYY HH:mm" default='never'}}</span>
        </div>
        <div>
            <div class="access-stats-item-small-data-row-name">Movies RSS:</div>
            <span class="access-stats-item-small-data-row-value">{{dateFormat lastMoviesFeedGeneratedFormatted format="DD/MM/YYYY HH:mm" default='never'}}</span>
        </div>
        <div class='impersonate-button-column'>
            <div class='btn btn-small impersonate-button'>Impersonate</div>
            <div class='btn btn-small impersonate-button-disabled'>Logged in</div>
        </div>
    </div>
</div>