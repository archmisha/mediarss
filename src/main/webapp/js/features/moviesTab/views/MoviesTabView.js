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
	'fancybox',
	'moment'
],
	function($, Marionette, Handlebars, template, MovieCollectionView, MoviesCollection, MovieTorrentCollectionView, UserTorrentCollection, HttpUtils, SectionView, MessageBox, Fancybox, Moment) {
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

				this.moviesCollection = new MoviesCollection(/*this.tabData.availableMovies*/);
				this.moviesCollectionView = new MovieCollectionView({vent: this.vent, collection: this.moviesCollection});

				this.movieTorrentCollection = new UserTorrentCollection();
				this.movieTorrentColletionView = new MovieTorrentCollectionView({vent: this.vent, collection: this.movieTorrentCollection});

				this.moviesSection = new SectionView({
					title: 'Latest Movies',
					description: 'Updated on: <span class=\'movies-updated-on\'></span>' + +
						'.<br/>Select movies to download. Here you can find newly available movies. You can use IMDB preview'
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

			onRender: function() {
				// must be first
				this.movieTorrentColletionView.setEmptyMessage(SELECT_MOVIE_EMPTY_MSG);

				this.moviesListRegion.show(this.moviesCollectionView);
				this.movieTorrentListRegion.show(this.movieTorrentColletionView);
				this.moviesSectionRegion.show(this.moviesSection);
				this.futureMoviesSectionRegion.show(this.futureMoviesSection);

				var that = this;
				HttpUtils.get("rest/movies/initial-data", function(res) {
					that._updateAvailableMovies(res.availableMovies);
					that.ui.futureMoviesCounter.html(res.userMoviesCount);

					// must be before reset
					that.movieTorrentColletionView.setEmptyMessage(SELECT_MOVIE_EMPTY_MSG);
					that.movieTorrentCollection.reset();

					$('.movies-updated-on').html(Moment(new Date(res.moviesLastUpdated)).format('DD/MM/YYYY HH:mm '));
				}, false); // no need loading here
			},

			onMovieTorrentDownload: function(userTorrent) {
				var isUserMovies = this._isUserMoviesSelected();
				var that = this;
				HttpUtils.post('rest/movies/download', {
					torrentId: userTorrent.get('torrentId'),
					movieId: userTorrent.get('movieId'),
					isUserMovies: isUserMovies
				}, function(res) {
					if (isUserMovies) {
						that._updateUserMovies(res.movies);

						// userMovies are sorted from latest downloaded to oldest, so need to scroll to top just in case
						that.moviesListRegion.$el.scrollTop(0);
					} else {
						that._updateAvailableMovies(res.movies);
						that.ui.futureMoviesCounter.html(res.userMoviesCount);
					}
					var movieModel = that.moviesCollection.get(userTorrent.get('movieId'));
					that.onMovieSelected(movieModel);
				});
			},

			onMovieSelected: function(movieModel) {
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
					if (that._isUserMoviesSelected) {
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
					that._switchToUserMovies(res.movies);
					var movieModel = that.moviesCollection.get(res.movieId);
					that.onMovieSelected(movieModel);
				});
			},

			onFutureMovieRemove: function(movieModel) {
				var that = this;
				HttpUtils.post("rest/movies/future/remove", {
					movieId: movieModel.get('id')
				}, function(res) {
					MessageBox.info(res.message);

					that.moviesCollection.remove(movieModel.get('id'));
					that.ui.futureMoviesCounter.html(that.moviesCollection.size());

					// must be before reset
					that.movieTorrentColletionView.setEmptyMessage(SELECT_MOVIE_EMPTY_MSG);
					that.movieTorrentCollection.reset();
				});
			},

			_isUserMoviesSelected: function() {
				return this.ui.futureMoviesFilter.hasClass('filter-selected');
			},

			_updateUserMovies: function(movies) {
				this.ui.futureMoviesCounter.html(movies.length);
				this.moviesCollection.reset(movies);
			},

			_updateAvailableMovies: function(movies) {
				this.ui.moviesCounter.html(movies.length);
				this.moviesCollection.reset(movies);
			},

			_switchToUserMovies: function(movies) {
				this.ui.moviesFilter.removeClass('filter-selected');
				this.ui.futureMoviesFilter.addClass('filter-selected');
				this.moviesListRegion.$el.addClass('future-movies-list');
				if (movies == null) {
					this.moviesCollection.reset();
				} else {
					this._updateUserMovies(movies);
				}
			},

			_switchToAvailableMovies: function(movies) {
				this.ui.futureMoviesFilter.removeClass('filter-selected');
				this.ui.moviesFilter.addClass('filter-selected');
				this.moviesListRegion.$el.removeClass('future-movies-list');
				if (movies == null) {
					this.moviesCollection.reset();
				} else {
					this._updateAvailableMovies(movies);
				}
			},

			onFutureMoviesFilterClick: function() {
				if (this._isUserMoviesSelected()) {
					return;
				}

				this._switchToUserMovies(null);
				var that = this;
				HttpUtils.get('rest/movies/userMovies', function(res) {
					that._updateUserMovies(res.movies);
					// must be before reset
					that.movieTorrentColletionView.setEmptyMessage(NO_TORRENTS_MSG);
					that.movieTorrentCollection.reset();
				});
			},

			onMoviesFilterClick: function() {
				if (!this._isUserMoviesSelected()) {
					return;
				}
				this._showAvailableMovies();
			},

			_showAvailableMovies: function() {
				this._switchToAvailableMovies(null);
				var that = this;
				HttpUtils.get('rest/movies/availableMovies', function(res) {
					that._updateAvailableMovies(res.movies);

					// must be before reset
					that.movieTorrentColletionView.setEmptyMessage(SELECT_MOVIE_EMPTY_MSG);
					that.movieTorrentCollection.reset();
				});
			}
		});
	});
