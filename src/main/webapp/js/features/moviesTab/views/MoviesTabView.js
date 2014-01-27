/*global define*/
define([
	'jquery',
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movies-tab.tpl',
	'features/moviesTab/views/MovieCollectionView',
	'features/moviesTab/views/MoviesSearchView',
	'features/moviesTab/collections/MoviesCollection',
	'components/search-result/views/SearchResultsCollectionView',
	'features/collections/UserTorrentCollection',
	'HttpUtils',
	'components/section/views/SectionView',
	'MessageBox',
	'fancybox',
	'moment',
	'StringUtils',
	'routers/RoutingPaths'
],
	function($, Marionette, Handlebars, template, MovieCollectionView, MoviesSearchView, MoviesCollection, SearchResultsCollectionView, UserTorrentCollection, HttpUtils, SectionView, MessageBox, Fancybox, Moment, StringUtils, RoutingPaths) {
		"use strict";

		var selectedMovie = null;
		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'movies-tab',

			ui: {
				availableMoviesCounter: '.movies-counter',
				userMoviesCounter: '.future-movies-counter',
				userMoviesFilter: '.future-movies-filter',
				availableMoviesFilter: '.movies-filter',
				noMovieSelected: '.movies-torrents-list-movie-not-selected'
			},

			events: {
				'click .future-movies-filter': 'onFutureMoviesFilterClick',
				'click .movies-filter': 'onMoviesFilterClick'
			},

			regions: {
				moviesSearchRegin: '.movies-search-section',
				moviesListRegion: '.movies-list-container',
				movieTorrentListRegion: '.movies-torrents-list',
				moviesSectionRegion: '.movies-section'
			},

			constructor: function(options) {
				this.vent = new Backbone.Wreqr.EventAggregator();
				Marionette.Layout.prototype.constructor.apply(this, arguments);

				this.moviesCollection = new MoviesCollection();
				this.moviesCollectionView = new MovieCollectionView({vent: this.vent, collection: this.moviesCollection});

				this.movieTorrentCollection = new UserTorrentCollection();
				this.movieTorrentColletionView = new SearchResultsCollectionView({
					vent: this.vent,
					collection: this.movieTorrentCollection,
					emptyMessage: 'No available torrents yet'
				});

				this.moviesSection = new SectionView({
					title: 'Latest Movies',
					description: 'Updated on: <span class=\'movies-updated-on\'></span>' +
						'<br/>Select movies to download. Here you can find newly available movies. You can use IMDB preview'
				});

				this.moviesSearchView = new MoviesSearchView({vent: this.vent});

				this.vent.on('movie-selected', this.onMovieSelected, this);
				this.vent.on('future-movie-remove', this.onUserMovieRemove, this);
				this.vent.on('search-result-item-download', this.onMovieTorrentDownload, this);
				this.vent.on('movie-search-add', this.onFutureMovieAddButtonClick, this);
			},

			onRender: function() {
				this.moviesListRegion.show(this.moviesCollectionView);
				this.moviesSectionRegion.show(this.moviesSection);
				this.moviesSearchRegin.show(this.moviesSearchView);

				var isAvailableMovies = window.location.href.indexOf('userMovies') === -1;
				var that = this;
				HttpUtils.get("rest/movies/initial-data/" + (isAvailableMovies ? 'availableMovies' : 'userMovies'), function(res) {
					if (isAvailableMovies) {
						that._updateAvailableMovies(res.availableMovies);
						that.ui.userMoviesCounter.html(res.userMoviesCount);
					} else {
						that._switchToUserMovies(res.userMovies);
//						that._updateUserMovies(res.userMovies);
						that.ui.availableMoviesCounter.html(res.availableMoviesCount);
					}

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
						that.ui.userMoviesCounter.html(res.userMoviesCount);
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

				this.ui.noMovieSelected.hide();
				this.movieTorrentCollection.reset(movieModel.get('torrents'));
				this.movieTorrentListRegion.show(this.movieTorrentColletionView);

				if (movieModel.get('downloadStatus') === 'OLD') {
					this.movieTorrentColletionView.setEmptyMessage('No available torrents');
				} else {
					this.movieTorrentColletionView.setEmptyMessage('No available torrents yet');
				}
			},

			onFutureMovieAddButtonClick: function(res) {
				this._switchToUserMovies(res.movies);
				var movieModel = this.moviesCollection.get(res.movieId);
				this.onMovieSelected(movieModel);
			},

			onUserMovieRemove: function(movieModel) {
				var that = this;
				HttpUtils.post("rest/movies/future/remove", {
					movieId: movieModel.get('id')
				}, function(res) {
					MessageBox.info(res.message);

					that.moviesCollection.remove(movieModel.get('id'));
					that.ui.userMoviesCounter.html(that.moviesCollection.size());

					// only if the removed movie is the selected movie then clear the torrents list
					if (selectedMovie == movieModel) {
						that.movieTorrentListRegion.close();
						that.ui.noMovieSelected.show();
					}
				});
			},

			_isUserMoviesSelected: function() {
				return this.ui.userMoviesFilter.hasClass('filter-selected');
			},

			_updateUserMovies: function(movies) {
				this.ui.userMoviesCounter.html(movies.length);
				this.moviesCollection.reset(movies);

				var moviesBeingSearched = this._getMoviesBeingSearched();
				if (moviesBeingSearched.length > 0) {
					this._startPollingThread(moviesBeingSearched);
				}
			},

			_getMoviesBeingSearched: function() {
				var moviesBeingSearched = [];
				this.moviesCollection.forEach(function(movieModel) {
					if (movieModel.get('downloadStatus') === 'BEING_SEARCHED') {
						moviesBeingSearched.push(movieModel.get('id'));
					}
				});
				return    moviesBeingSearched;
			},

			_updateAvailableMovies: function(movies) {
				this.ui.availableMoviesCounter.html(movies.length);
				this.moviesCollection.reset(movies);
			},

			_switchToUserMovies: function(movies) {
				Backbone.history.navigate(StringUtils.formatRoute(RoutingPaths.MOVIES, 'userMovies'), {trigger: false});

				this.movieTorrentListRegion.close();
				this.ui.noMovieSelected.show();
				this.ui.availableMoviesFilter.removeClass('filter-selected');
				this.ui.userMoviesFilter.addClass('filter-selected');
				this.moviesListRegion.$el.addClass('future-movies-list');
				if (movies == null) {
					this.moviesCollection.reset();
				} else {
					this._updateUserMovies(movies);
				}
			},

			_switchToAvailableMovies: function(movies) {
				Backbone.history.navigate(StringUtils.formatRoute(RoutingPaths.MOVIES, 'availableMovies'), {trigger: false});

				this.movieTorrentListRegion.close();
				this.ui.noMovieSelected.show();
				this.ui.userMoviesFilter.removeClass('filter-selected');
				this.ui.availableMoviesFilter.addClass('filter-selected');
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

				this._stopPollingThread();
				this._switchToUserMovies(null);
				var that = this;
				HttpUtils.get('rest/movies/user-movies', function(res) {
					that._updateUserMovies(res.movies);
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
				this._switchToAvailableMovies(null);
				var that = this;
				HttpUtils.get('rest/movies/available-movies', function(res) {
					that._updateAvailableMovies(res.movies);
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
								for (var i = 0; i < res.completed.length; ++i) {
									var el = res.completed[i];
									var movieModel = that.moviesCollection.get(el.id);
									movieModel.set('torrents', el.torrents);
									movieModel.set('downloadStatus', el.downloadStatus);
								}
								if (selectedMovie == movieModel) {
									selectedMovie = null;
									that.onMovieSelected(movieModel);
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
			},

			_stopPollingThread: function() {
				clearTimeout(this.timer);
				this.timer = null;
			}
		});
	});
