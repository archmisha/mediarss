/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movies-tab.tpl',
	'features/moviesTab/views/MovieCollectionView',
	'features/moviesTab/collections/MoviesCollection',
	'features/moviesTab/views/MovieTorrentCollectionView',
	'features/collections/UserTorrentCollection',
	'HttpUtils',
	'components/section/views/SectionView',
	'MessageBox'
],
	function(Marionette, Handlebars, template, MovieCollectionView, MoviesCollection, MovieTorrentCollectionView, UserTorrentCollection, HttpUtils, SectionView, MessageBox) {
		"use strict";

		var selectedMovie = null;
		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'movies-tab',

			ui: {
				imdbPreview: '.movies-imdb-preview',
				imdbNoPreview: '.movies-imdb-no-preview',
				imdbClickForPreview: '.movies-imdb-click-for-preview',
				imdbPreviewLoading: '.movies-imdb-preview-loading',
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

				this.moviesCollection = new MoviesCollection(this.loggedInUserData.movies);
				this.moviesCollectionView = new MovieCollectionView({vent: this.vent, collection: this.moviesCollection});

				this.movieTorrentCollection = new UserTorrentCollection();
				this.movieTorrentColletionView = new MovieTorrentCollectionView({vent: this.vent, collection: this.movieTorrentCollection});

				// reset the retries limit so wont run for ever
				window.moviesImdbPreviewHeightRetries = 5;
				window.resize_iframe = this.resize_iframe;

				this.moviesSection = new SectionView({
					title: 'Movies Download',
					description: 'Select movies to download. You can use IMDB preview'
				});

				this.futureMoviesSection = new SectionView({
					title: 'Movies You Want (experimental)',
					description: 'Add movies not available yet for download by their IMDB id and they will be ' +
						'automatically added <br/>to your feed once they are available for download.'
				});

				this.vent.on('movie-selected', this.onMovieSelected, this);
				this.vent.on('future-movie-remove', this.onFutureMovieRemove, this);
				this.vent.on('movie-torrent-download', this.onMovieTorrentDownload, this);
			},

			onMovieTorrentDownload: function(userTorrent) {
				HttpUtils.post("rest/movies/download", {
					torrentId: userTorrent.get('torrentId')
				}, function(res) {
					userTorrent.set('downloadStatus', 'SCHEDULED');
					selectedMovie.set('downloadStatus', 'SCHEDULED');
				});
			},

			// called in window scope, so no reference to this
			resize_iframe: function() {
				var scrollHeight = document.getElementById("movies-imdb-preview").contentWindow.document.body.offsetHeight;
//				console.log('scroll height:' + scrollHeight);

				document.getElementById("movies-imdb-preview").style.height = parseInt(scrollHeight, 10) + 10 + 'px';

				// after the page is loaded adjust the height again - assuming the height must change so running until changed
				setTimeout(function() {
					if (window.moviesImdbPreviewHeightRetries == 0) {
						return;
					}
					window.moviesImdbPreviewHeightRetries--;

					if (window.moviesImdbPreviewHeight !== undefined && window.moviesImdbPreviewHeight != scrollHeight) {
						return;
					}
					window.moviesImdbPreviewHeight = scrollHeight;
					resize_iframe();
				}, 500);
			},

			onRender: function() {
				this.moviesListRegion.show(this.moviesCollectionView);
				this.movieTorrentListRegion.show(this.movieTorrentColletionView);
				this.moviesSectionRegion.show(this.moviesSection);
				this.futureMoviesSectionRegion.show(this.futureMoviesSection);

				this.ui.moviesCounter.html(this.loggedInUserData.movies.length);
				this.ui.futureMoviesCounter.html(this.loggedInUserData.futureMovies.length);
			},

			onMovieSelected: function(movieModel) {
				// clicked twice the same movie
				if (selectedMovie == movieModel) {
					return;
				}
				selectedMovie = movieModel;

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

				// display imdb preview
				var that = this;
				this.ui.imdbClickForPreview.hide();
				this.ui.imdbPreview.hide();
				var imdbUrl = movieModel.get('imdbUrl');
				if (imdbUrl) {
					this.ui.imdbNoPreview.hide();
					this.ui.imdbPreviewLoading.show();

					HttpUtils.get("rest/movies/imdb/" + movieModel.get('id'), function(res) {
						that.ui.imdbPreviewLoading.hide();
						that.ui.imdbPreview.show();
						that.ui.imdbPreview.contents().find('body').html(res);
					}, false);
				} else {
					this.ui.imdbPreviewLoading.hide();
					this.ui.imdbPreview.hide();
					this.ui.imdbNoPreview.show();
				}

				// update movie torrents list
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

					that.loggedInUserData = res.user;
					that.ui.moviesCounter.html(that.loggedInUserData.movies.length);
					that.ui.futureMoviesCounter.html(that.loggedInUserData.futureMovies.length);
					if (that.ui.futureMoviesFilter.hasClass('filter-selected')) {
						that.moviesCollection.reset(that.loggedInUserData.futureMovies);
					} else {
						that.moviesCollection.reset(that.loggedInUserData.movies);
					}
				});
			},

			onFutureMoviesFilterClick: function() {
				this.moviesCollection.reset(this.loggedInUserData.futureMovies);
				this.ui.futureMoviesFilter.addClass('filter-selected');
				this.ui.moviesFilter.removeClass('filter-selected');
				this.moviesListRegion.$el.addClass('future-movies-list');
				this.movieTorrentCollection.reset();
			},

			onMoviesFilterClick: function() {
				this.moviesCollection.reset(this.loggedInUserData.movies);
				this.ui.futureMoviesFilter.removeClass('filter-selected');
				this.ui.moviesFilter.addClass('filter-selected');
				this.moviesListRegion.$el.removeClass('future-movies-list');
				this.movieTorrentCollection.reset();
			},

			onFutureMovieRemove: function(movieModel) {
				var that = this;
				HttpUtils.post("rest/movies/future/remove", {movieId: movieModel.get('id')}, function(res) {
					MessageBox.info(res.message);

					var i;
					for (i = 0; i < that.loggedInUserData.futureMovies.length; ++i) {
						if (that.loggedInUserData.futureMovies[i].id == movieModel.get('id')) {
							break;
						}
					}
					that.loggedInUserData.futureMovies.splice(i, 1);
					that.ui.futureMoviesCounter.html(that.loggedInUserData.futureMovies.length);
					if (that.ui.futureMoviesFilter.hasClass('filter-selected')) {
						that.moviesCollection.reset(that.loggedInUserData.futureMovies);
					}
				});
			}
		});
	});
