<div class='trakt-tv-section'></div>
<div class='trakt-tv-body'>
    {{#if isConnectedToTrakt }}
    <span class='trakt-connected-msg'>You are connected to Trakt.tv.</span>
    &nbsp;&nbsp;&nbsp;
    <span class='trakt-disconnect-button mediarss-link'>Disconnect</span>
    {{else}}
    <a href="http://trakt.tv/oauth/authorize?client_id={{clientId}}&redirect_uri={{redirectUri}}&response_type=code&state=settings">Click
        here to sign in to Trakt.tv</a>
    {{/if}}
</div>
<!--http://localhost:8083/main?code=eec163940dc72e00c377a55f5980bf25de769c78ffa29889419ca49548503235&state=settings-->