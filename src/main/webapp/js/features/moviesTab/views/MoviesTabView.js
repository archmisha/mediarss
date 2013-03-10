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
	'components/section/views/SectionView'
],
	function(Marionette, Handlebars, template, MovieCollectionView, MoviesCollection, MovieTorrentCollectionView,
		UserTorrentCollection, HttpUtils, SectionView) {
		"use strict";

		var selectedMovie = null;
		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
			className: 'movies-tab',

			ui: {
				imdbPreview: '.movies-imdb-preview',
				imdbNoPreview: '.movies-imdb-no-preview',
				imdbClickForPreview: '.movies-imdb-click-for-preview',
				imdbPreviewLoading: '.movies-imdb-preview-loading'
			},

			events: {
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
				this.movieTorrentColletionView = new MovieTorrentCollectionView({collection: this.movieTorrentCollection});

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
			}
		});
	});
