/*global define*/
define([
	'jquery',
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movies-tab.tpl',
	'features/moviesTab/views/MovieCollectionView',
	'features/moviesTab/collections/MoviesCollection',
	'features/moviesTab/views/MovieTorrentCollectionView',
	'features/collections/UserTorrentCollection',
	'HttpUtils',
	'components/section/views/SectionView',
	'MessageBox',
	'fancybox'
],
	function($, Marionette, Handlebars, template, MovieCollectionView, MoviesCollection, MovieTorrentCollectionView, UserTorrentCollection, HttpUtils, SectionView, MessageBox, Fancybox) {
		"use strict";

		var selectedMovie = null;
		var SELECT_MOVIE_EMPTY_MSG = 'Select a movie to view available torrents';
		var NO_TORRENTS_MSG = 'No available torrents yet';
		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'movies-tab',

			ui: {
				imdbIdInput: '.future-movies-imdb-id-input',
				moviesCounter: '.movies-counter',
				futureMoviesCounter: '.future-movies-counter',
				futureMoviesFilter: '.future-movies-filter',
				moviesFilter: '.movies-filter'
			},

			events: {
				'click .future-movies-add-button': 'onFutureMovieAddButtonClick',
				'click .future-movies-filter': 'onFutureMoviesFilterClick',
				'click .movies-filter': 'onMoviesFilterClick'
			},

			regions: {
				moviesListRegion: '.movies-list-container',
				movieTorrentListRegion: '.movies-torrents-list-container',
				moviesSectionRegion: '.movies-section',
				futureMoviesSectionRegion: '.future-movies-section'
			},

			constructor: function(options) {
				this.vent = new Marionette.EventAggregator();
				Marionette.Layout.prototype.constructor.apply(this, arguments);
				this.loggedInUserData = options.loggedInUserData;
				this.initialData = options.initialData;

				this.moviesCollection = new MoviesCollection(this.loggedInUserData.availableMovies);
				this.moviesCollectionView = new MovieCollectionView({vent: this.vent, collection: this.moviesCollection});

				this.movieTorrentCollection = new UserTorrentCollection();
				this.movieTorrentColletionView = new MovieTorrentCollectionView({vent: this.vent, collection: this.movieTorrentCollection});

				this.moviesSection = new SectionView({
					title: 'Latest Movies',
					description: 'Select movies to download. Here you can find newly available movies. You can use IMDB preview'
				});

				this.futureMoviesSection = new SectionView({
					title: 'Search Movies',
					description: 'Search for movies by IMDB ID.<br/>' +
						'If the movie is already available for download it will be automatically added to your feed<br/>' +
						'Otherwise it will be scheduled for download in the <b>future</b> once they will be available.'
				});

				this.vent.on('movie-selected', this.onMovieSelected, this);
				this.vent.on('future-movie-remove', this.onFutureMovieRemove, this);
				this.vent.on('movie-torrent-download', this.onMovieTorrentDownload, this);
			},

			onMovieTorrentDownload: function(userTorrent) {
				var that = this;
				HttpUtils.post("rest/movies/download", {
					torrentId: userTorrent.get('torrentId'),
					movieId: userTorrent.get('movieId')
				}, function(res) {
					that._updateAllMovies(res);
//					userTorrent.set('downloadStatus', 'SCHEDULED');
//					userTorrent.set('scheduledDate', new Date());
//					selectedMovie.set('downloadStatus', 'SCHEDULED');
				});
			},

			onRender: function() {
				// must be first
				this.movieTorrentColletionView.setEmptyMessage(SELECT_MOVIE_EMPTY_MSG);

				this.moviesListRegion.show(this.moviesCollectionView);
				this.movieTorrentListRegion.show(this.movieTorrentColletionView);
				this.moviesSectionRegion.show(this.moviesSection);
				this.futureMoviesSectionRegion.show(this.futureMoviesSection);

				this.ui.moviesCounter.html(this.loggedInUserData.availableMovies.length);
				this.ui.futureMoviesCounter.html(this.loggedInUserData.userMovies.length);
			},

			onMovieSelected: function(movieModel) {
				this._selectMovieHelper(movieModel);
			},

			_selectMovieHelper: function(movieModel) {
				// clicked twice the same movie
				if (selectedMovie == movieModel) {
					return;
				}
				selectedMovie = movieModel;

				this.moviesCollection.forEach(function(curMovieModel) {
					curMovieModel.set('selected', false);
				});
				movieModel.set('selected', true);

				// update server that movie is viewed
				if (!movieModel.get('viewed')) {
					HttpUtils.post("rest/movies/view", {
						movieId: movieModel.get('id')
					}, function(res) {
						movieModel.set('viewed', true);
						movieModel.get('torrents').forEach(function(t) {
							t.viewed = true;
						});
					}, false);
				}

				// update movie torrents list
				var that = this;
				if (movieModel.get('torrents').length == 0) {
					var msg;
					if (that.ui.futureMoviesFilter.hasClass('filter-selected')) {
						msg = NO_TORRENTS_MSG;
					} else {
						msg = SELECT_MOVIE_EMPTY_MSG;
					}
					this.movieTorrentColletionView.setEmptyMessage(msg);
				}
				// must be after the message is set
				this.movieTorrentCollection.reset(movieModel.get('torrents'));
			},

			onFutureMovieAddButtonClick: function() {
				var imdbId = this.ui.imdbIdInput.val();

				if (!imdbId || imdbId.trim().length == 0) {
					return;
				}

				var that = this;
				HttpUtils.post("rest/movies/future/add", {imdbId: imdbId}, function(res) {
					that.ui.imdbIdInput.val('');
					MessageBox.info(res.message);
					that._updateAllMovies(res);
				});
			},

			_updateAllMovies: function(res) {
				var that = this;
				that.loggedInUserData = res.user;
				that.ui.moviesCounter.html(that.loggedInUserData.availableMovies.length);
				that.ui.futureMoviesCounter.html(that.loggedInUserData.userMovies.length);
				that.ui.moviesFilter.removeClass('filter-selected');
				that.ui.futureMoviesFilter.addClass('filter-selected');
				that.moviesListRegion.$el.addClass('future-movies-list');
				that.moviesCollection.reset(that.loggedInUserData.userMovies);

				var movieModel = that.moviesCollection.get(res.movieId);
				that._selectMovieHelper(movieModel);
			},

			onFutureMoviesFilterClick: function() {
				this.moviesCollection.reset(this.loggedInUserData.userMovies);
				this.ui.futureMoviesFilter.addClass('filter-selected');
				this.ui.moviesFilter.removeClass('filter-selected');
				this.moviesListRegion.$el.addClass('future-movies-list');
				// must be before reset
				this.movieTorrentColletionView.setEmptyMessage(NO_TORRENTS_MSG);
				this.movieTorrentCollection.reset();
			},

			onMoviesFilterClick: function() {
				this.moviesCollection.reset(this.loggedInUserData.availableMovies);
				this.ui.futureMoviesFilter.removeClass('filter-selected');
				this.ui.moviesFilter.addClass('filter-selected');
				this.moviesListRegion.$el.removeClass('future-movies-list');
				// must be before reset
				this.movieTorrentColletionView.setEmptyMessage(SELECT_MOVIE_EMPTY_MSG);
				this.movieTorrentCollection.reset();
			},

			onFutureMovieRemove: function(movieModel) {
				var that = this;
				HttpUtils.post("rest/movies/future/remove", {movieId: movieModel.get('id')}, function(res) {
					MessageBox.info(res.message);

					var i;
					for (i = 0; i < that.loggedInUserData.userMovies.length; ++i) {
						if (that.loggedInUserData.userMovies[i].id == movieModel.get('id')) {
							break;
						}
					}
					that.loggedInUserData.userMovies.splice(i, 1);
					that.ui.futureMoviesCounter.html(that.loggedInUserData.userMovies.length);
					if (that.ui.futureMoviesFilter.hasClass('filter-selected')) {
						that.moviesCollection.reset(that.loggedInUserData.userMovies);
					}

					// must be before reset
					that.movieTorrentColletionView.setEmptyMessage(SELECT_MOVIE_EMPTY_MSG);
					that.movieTorrentCollection.reset();
				});
			}
		});
	});
