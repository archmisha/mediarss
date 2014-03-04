/*global define*/
define([
	'jquery',
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movies-tab.tpl',
	'features/moviesTab/views/MovieCollectionView',
	'features/moviesTab/views/MoviesSearchView',
	'features/moviesTab/collections/MoviesCollection',
	'utils/HttpUtils',
	'components/section/views/SectionView',
	'utils/MessageBox',
	'fancybox',
	'moment',
	'utils/StringUtils',
	'routers/RoutingPaths'
],
	function($, Marionette, Handlebars, template, MovieCollectionView, MoviesSearchView, MoviesCollection, HttpUtils, SectionView, MessageBox, Fancybox, Moment, StringUtils, RoutingPaths) {
		"use strict";

		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'movies-tab',

			ui: {
				availableMoviesCounter: '.movies-counter',
				userMoviesCounter: '.future-movies-counter',
				userMoviesFilter: '.future-movies-filter',
				availableMoviesFilter: '.movies-filter'
			},

			events: {
				'click .future-movies-filter': 'onFutureMoviesFilterClick',
				'click .movies-filter': 'onMoviesFilterClick'
			},

			regions: {
				moviesSearchRegion: '.movies-search-section',
				moviesListRegion: '.movies-list-container',
				moviesSectionRegion: '.movies-section'
			},

			constructor: function(options) {
				this.vent = new Backbone.Wreqr.EventAggregator();
				Marionette.Layout.prototype.constructor.apply(this, arguments);

				this.availalbleMoviesCollection = new MoviesCollection();
				this.userMoviesCollection = new MoviesCollection();
				this.availableMoviesCollectionView = new MovieCollectionView({vent: this.vent, collection: this.availalbleMoviesCollection});
				this.userMoviesCollectionView = new MovieCollectionView({vent: this.vent, collection: this.userMoviesCollection});

				this.moviesSection = new SectionView({
					title: 'Latest Movies',
					description: 'Updated on: <span class=\'movies-updated-on\'></span>' +
						'<br/>Select movies to download. Here you can find newly available movies. You can use IMDB preview'
				});

				this.moviesSearchView = new MoviesSearchView({vent: this.vent});

				this.vent.on('future-movie-remove', this.onUserMovieRemove, this);
				this.vent.on('movie-torrent-download', this.onMovieTorrentDownload, this);
				this.vent.on('movie-search-add', this.onFutureMovieAddButtonClick, this);
			},

			onRender: function() {
				this.moviesSectionRegion.show(this.moviesSection);
				this.moviesSearchRegion.show(this.moviesSearchView);

				var isAvailableMovies = window.location.href.indexOf('userMovies') === -1;
				if (isAvailableMovies) {
					this.moviesListRegion.show(this.availableMoviesCollectionView);
				} else {
					this.moviesListRegion.show(this.userMoviesCollectionView);
				}

				var that = this;
				HttpUtils.get('rest/movies/initial-data/' + (isAvailableMovies ? 'availableMovies' : 'userMovies'), function(res) {
					if (isAvailableMovies) {
						that._updateAvailableMovies(res.availableMovies);
						that.ui.userMoviesCounter.html(res.userMoviesCount);
					} else {
						that._switchToUserMovies(res.userMovies);
						that.ui.availableMoviesCounter.html(res.availableMoviesCount);
					}

					$('.movies-updated-on').html(Moment(new Date(res.moviesLastUpdated)).format('DD/MM/YYYY HH:mm '));
				}, false); // no need loading here
			},

			onMovieTorrentDownload: function(userTorrent, movieId) {
				var isUserMovies = this._isUserMoviesSelected();
				var that = this;
				HttpUtils.post('rest/movies/download', {
					torrentId: userTorrent.get('torrentId'),
					movieId: movieId,
					isUserMovies: isUserMovies
				}, function(res) {
					if (isUserMovies) {
						that._updateUserMovies(res.movies);

						// userMovies are sorted from latest downloaded to oldest, so need to scroll to top just in case
						that.moviesListRegion.$el.scrollTop(0);
					} else {
						that._updateAvailableMovies(res.movies);
						that.ui.userMoviesCounter.html(res.userMoviesCount);
					}
				});
			},

			onFutureMovieAddButtonClick: function(res) {
				this._switchToUserMovies(res.movies);
			},

			onUserMovieRemove: function(movieModel) {
				var that = this;
				HttpUtils.post("rest/movies/future/remove", {
					movieId: movieModel.get('id')
				}, function(res) {
					MessageBox.info(res.message);

					that.userMoviesCollection.remove(movieModel);
					that.ui.userMoviesCounter.html(that.userMoviesCollection.size());
				});
			},

			_isUserMoviesSelected: function() {
				return this.ui.userMoviesFilter.hasClass('filter-selected');
			},

			_updateUserMovies: function(movies) {
				this.ui.userMoviesCounter.html(movies.length);
				this.userMoviesCollection.reset(movies);
				this.userMoviesCollectionView.render();

				var moviesBeingSearched = this._getMoviesBeingSearched();
				if (moviesBeingSearched.length > 0) {
					this._startPollingThread(moviesBeingSearched);
				}
			},

			_getMoviesBeingSearched: function() {
				var moviesBeingSearched = [];
				this.userMoviesCollection.forEach(function(movieModel) {
					if (movieModel.get('downloadStatus') === 'BEING_SEARCHED') {
						moviesBeingSearched.push(movieModel.get('id'));
					}
				});
				return moviesBeingSearched;
			},

			_updateAvailableMovies: function(movies) {
				this.ui.availableMoviesCounter.html(movies.length);
				this.availalbleMoviesCollection.reset(movies);
				this.availableMoviesCollectionView.render();
			},

			_switchToUserMovies: function(movies) {
				Backbone.history.navigate(StringUtils.formatRoute(RoutingPaths.MOVIES, 'userMovies'), {trigger: false});

				this.ui.availableMoviesFilter.removeClass('filter-selected');
				this.ui.userMoviesFilter.addClass('filter-selected');
				this.moviesListRegion.$el.addClass('future-movies-list');
				this._updateUserMovies(movies);
			},

			_switchToAvailableMovies: function(movies) {
				Backbone.history.navigate(StringUtils.formatRoute(RoutingPaths.MOVIES, 'availableMovies'), {trigger: false});

				this.ui.userMoviesFilter.removeClass('filter-selected');
				this.ui.availableMoviesFilter.addClass('filter-selected');
				this.moviesListRegion.$el.removeClass('future-movies-list');
				this._updateAvailableMovies(movies);
			},

			onFutureMoviesFilterClick: function() {
				if (this._isUserMoviesSelected()) {
					return;
				}

				this._stopPollingThread();
				this._switchToUserMovies([]);
				var that = this;
				HttpUtils.get('rest/movies/user-movies', function(res) {
					that._updateUserMovies(res.movies);
					that.moviesListRegion.show(that.userMoviesCollectionView);
				});
			},

			onMoviesFilterClick: function() {
				if (!this._isUserMoviesSelected()) {
					return;
				}

				this._showAvailableMovies();
			},

			_showAvailableMovies: function() {
				this._stopPollingThread();
				this._switchToAvailableMovies([]);
				var that = this;
				HttpUtils.get('rest/movies/available-movies', function(res) {
					that._updateAvailableMovies(res.movies);
					that.moviesListRegion.show(that.availableMoviesCollectionView);
				});
			},

			onClose: function() {
				// when leaving the view stop polling the server for job updates
				this._stopPollingThread();
			},

			_startPollingThread: function(moviesBeingSearched) {
				var that = this;
				var f = function() {
					if (!that.timer) {
						return;
					}

					$.post("rest/movies/check-movies-being-searched", {
						ids: moviesBeingSearched
					}).success(function(res) {
						if (that.timer !== null) {
							var moviesCollection;
							if (this._isUserMoviesSelected()) {
								moviesCollection = this.userMoviesCollection;
							} else {
								moviesCollection = this.availalbleMoviesCollection;
							}

							for (var i = 0; i < res.completed.length; ++i) {
								var el = res.completed[i];
								var movieModel = moviesCollection.get(el.id);
								movieModel.set('torrents', el.torrents);
								movieModel.set('downloadStatus', el.downloadStatus);
							}

							if (res.completed.length === moviesBeingSearched.length) {
								that._stopPollingThread();
							}
						}
					}).error(function(res) {
						console.log('error. data: ' + res);
						that._stopPollingThread();
					});

					that.timer = setTimeout(f, 5000);
				};
				that.timer = setTimeout(f, 5000);
				f();
			},

			_stopPollingThread: function() {
				clearTimeout(this.timer);
				this.timer = null;
			}
		});
	});
