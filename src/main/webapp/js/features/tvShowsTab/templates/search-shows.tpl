<div class='shows-search-first-row'>
    <label for="shows-search-title" class='shows-search-title-label'>Name: </label>
    <input class='shows-search-title' type='text' id='shows-search-title' tabindex="0"/>
    <button class='btn btn-small shows-search-button' tabindex="1">Search</button>
</div>
<div class='shows-search-second-row'>
    <label for="shows-search-season" class='shows-search-season-label'>Season: </label>
    <input class='shows-search-season' type='text' id='shows-search-season' size="2" tabindex="0"/>
    <label for="shows-search-episode" class='shows-search-episode-label'>Episode: </label>
    <input class='shows-search-episode' type='text' id='shows-search-episode' size="2" tabindex="0"/>
    <div class='shows-search-admin-force-download'>
        <input class='shows-search-admin-force-download-checkbox' type="checkbox" name="forceDownload" value="true">
        <span class='shows-search-admin-force-download-label'>Force episodes download</span>
    </div>
</div>
<div class='shows-search-results-header'>
    <div class='shows-search-results-showing-results-for'>Showing results for <span
            class='shows-search-results-showing-results-for-text'></span></div>
    <div class='shows-search-results-did-you-mean'>Did you mean: <span
            class='shows-search-results-did-you-mean-list'></span></div>
    <div class='shows-search-results-header-title-container'>
        <div class='shows-search-results-header-title-multiple'>There are <span
                class='shows-search-results-header-count'></span> results
        </div>
        <div class='shows-search-results-header-title-single'>There is 1 result</div>
        <button class='btn shows-search-results-download-all-button'>Download all</button>
    </div>
</div>
<div class='shows-search-results'></div>
<div class='shows-search-status-no-results'>There are no results</div>