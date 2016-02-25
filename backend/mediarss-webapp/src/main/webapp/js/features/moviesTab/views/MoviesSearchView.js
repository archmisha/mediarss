/*global define*/
define([
        'marionette',
        'handlebars',
        'text!features/moviesTab/templates/movies-search.tpl',
        'features/moviesTab/views/MovieSearchResultsCollectionView',
        'utils/HttpUtils',
        'components/section/views/SectionView',
        'utils/MessageBox',
        'features/moviesTab/collections/MoviesCollection'
    ],
    function (Marionette, Handlebars, template, MovieSearchResultsCollectionView, HttpUtils, SectionView, MessageBox, MoviesCollection) {
        "use strict";

        return Marionette.Layout.extend({
            template: Handlebars.compile(template),
//			className: 'movies-tab',

            ui: {
                imdbIdInput: '.future-movies-imdb-id-input',
                adminForceDownload: '.shows-search-admin-force-download',
                adminForceDownloadCheckbox: '.shows-search-admin-force-download-checkbox'
            },

            events: {
                'click .future-movies-add-button': 'onFutureMovieAddButtonClick',
                'keypress .future-movies-imdb-id-input': 'onKeyPress'
            },

            regions: {
                moviesSearchHeaderRegion: '.movies-search-header',
                moviesSearchResultsRegion: '.movies-search-results'
            },

            constructor: function (options) {
                this.vent = options.vent;
                Marionette.Layout.prototype.constructor.apply(this, arguments);
                this.isAdmin = options.isAdmin;

                this.moviesSearchHeader = new SectionView({
                    title: 'Search Movies',
                    description: 'If the movie is already available for download it will be automatically added to your feed<br/>' +
                    'Otherwise it will be scheduled for download in the <b>future</b> once it will be available.'
                });

                this.movieSearchResultsCollection = new MoviesCollection();
                this.movieSearchResultsCollectionView = new MovieSearchResultsCollectionView({
                    collection: this.movieSearchResultsCollection,
                    vent: this.vent
                });

                this.vent.on('movie-search-result-item-add', this.onMovieSearchResultItemAdd, this);
            },

            onRender: function () {
                this.moviesSearchHeaderRegion.show(this.moviesSearchHeader);
            },

            onKeyPress: function (event) {
                var ENTER_KEY = 13;
                if (event.which === ENTER_KEY) {
                    this.onFutureMovieAddButtonClick();
                }
            },

            onFutureMovieAddButtonClick: function () {
                var query = this.ui.imdbIdInput.val();
                if (!query || query.trim().length == 0) {
                    return;
                }

                var that = this;
                HttpUtils.post('rest/movies/search', {query: query}, function (res) {
                    that.movieSearchResultsCollection.reset(res.searchResults);
                    that.moviesSearchResultsRegion.show(that.movieSearchResultsCollectionView);
                });
            },

            onMovieSearchResultItemAdd: function (model) {
                var that = this;
                HttpUtils.post('rest/movies/future/add', {imdbId: model.get('id')}, function (res) {
                    that.ui.imdbIdInput.val('');
                    MessageBox.info(res.message);
                    model.set('added', true);
                    that.vent.trigger('movie-search-add', res);
                });
            },

            templateHelpers: function () {
                return {
                    'isAdmin': this.isAdmin
                };
            }
        });
    });
