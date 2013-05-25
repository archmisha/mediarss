/*global define*/
define([
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movies-search.tpl',
	'features/moviesTab/views/MoviesSearchView',
	'HttpUtils',
	'components/section/views/SectionView',
	'MessageBox'
],
	function(Marionette, Handlebars, template,  MoviesSearchView, HttpUtils, SectionView, MessageBox) {
		"use strict";

		var selectedMovie = null;
		return Marionette.Layout.extend({
			template: Handlebars.compile(template),
//			className: 'movies-tab',

			ui: {
				imdbIdInput: '.future-movies-imdb-id-input'
			},

			events: {
				'click .future-movies-add-button': 'onFutureMovieAddButtonClick'
			},

			regions: {
				futureMoviesSectionRegion: '.future-movies-section'
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.Layout.prototype.constructor.apply(this, arguments);

				this.futureMoviesSection = new SectionView({
					title: 'Search Movies',
					description: 'Search for movies by IMDB ID.<br/>' +
						'If the movie is already available for download it will be automatically added to your feed<br/>' +
						'Otherwise it will be scheduled for download in the <b>future</b> once they will be available.'
				});
			},

			onRender: function() {
				this.futureMoviesSectionRegion.show(this.futureMoviesSection);
			},

			onFutureMovieAddButtonClick: function() {
				var imdbId = this.ui.imdbIdInput.val();

				if (!imdbId || imdbId.trim().length == 0) {
					return;
				}

				var that = this;
				HttpUtils.post('rest/movies/future/add', {imdbId: imdbId}, function(res) {
					that.ui.imdbIdInput.val('');
					MessageBox.info(res.message);
					that.vent.trigger('movie-search-add', res);
				});
			}
		});
	});
