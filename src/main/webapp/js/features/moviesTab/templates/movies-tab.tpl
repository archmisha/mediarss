<div class='future-movies-section'></div>
<div class='future-movies-content'>
    IMDB ID: <input type="text" class='future-movies-imdb-id-input'/>
    <button class='btn btn-small future-movies-add-button'>Add</button>
</div>
<div class='movies-section'></div>
<div class='movies-wrapper'>
    <div class='movies-lists-container'>
        <div class='movies-list-wrapper'>
            <span class='movies-filter filter-selected'>Movies (<span class='movies-counter'>0</span>)</span>
            <span class='future-movies-filter'>Scheduled Movies (<span class='future-movies-counter'>0</span>)</span>

            <div class='movies-list-container'></div>
        </div>
        <div class='movies-torrents-list-wrapper'>
            <span class='movies-torrents-list-title'>Available torrents</span>

            <div class='movies-torrents-list-container'></div>
        </div>
    </div>
    <div class='movies-preview-container'>
        <iframe class='movies-imdb-preview' id="movies-imdb-preview"></iframe>
        <div class='movies-imdb-no-preview'>There is no IMDB preview available</div>
        <div class='movies-imdb-click-for-preview'>Select a movie to see IMDB preview</div>
        <div class='movies-imdb-preview-loading'>IMDB preview is loading ...</div>
    </div>
</div>