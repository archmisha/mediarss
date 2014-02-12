<div class="access-stats-item-large">
    <div class='username-column' title="{{firstName}} {{lastName}} ({{email}})">{{firstName}} {{lastName}} ({{email}})</div>
<div class='last-login-column'>{{dateFormat lastLoginFormatted format="DD/MM/YYYY HH:mm" default='never'}}</div>
<div class='last-shows-feed-access-column'>{{dateFormat lastShowsFeedGeneratedFormatted format="DD/MM/YYYY HH:mm"
    default='never'}}
</div>
<div class='last-movies-feed-access-column'>{{dateFormat lastMoviesFeedGeneratedFormatted format="DD/MM/YYYY HH:mm"
    default='never'}}
</div>
<div class='impersonate-button-column'>
    <div class='btn btn-small impersonate-button'>Impersonate</div>
</div>
</div>
<div class="access-stats-item-small">
    <div>{{firstName}} {{lastName}} ({{email}}):</div>
    <div>Login: {{dateFormat lastLoginFormatted format="DD/MM/YYYY HH:mm" default='never'}}</div>
    <div>Shows RSS: {{dateFormat lastShowsFeedGeneratedFormatted format="DD/MM/YYYY HH:mm" default='never'}}</div>
    <div>Movies RSS{{dateFormat lastMoviesFeedGeneratedFormatted format="DD/MM/YYYY HH:mm" default='never'}}</div>
    <div class='impersonate-button-column'>
        <div class='btn btn-small impersonate-button'>Impersonate</div>
    </div>
</div>