<div class="movies-search-result-wrapper">
    <img width='40' height='60' src='{{image}}' class="movies-search-result-item-icon">

    <div class='movies-search-result-title-wrapper' title="{{{escapedTitle}}}">
        <div class='movies-search-result-title-wrapper2'>
            <span class='movies-search-result-item-title'>{{name}}</span>
            {{#ifneq year value="-1"}}
            <span class='movie-search-result-item-year'> ({{year}})</span>
            {{/ifneq}}
        </div>
    </div>
</div>
<div class='movies-search-result-item-status'>
    {{#if added}}
    <div class='movies-search-result-item-added'>added</div>
    {{else}}
    <button class='btn btn-small movies-search-result-item-add'>Add</button>
    {{/if}}
</div>