define([
	'marionette',
	'handlebars',
	'features/moviesTab/views/MovieItemView',
	'features/moviesTab/views/MovieCollectionLoadingView'
],
	function(Marionette, Handlebars, MovieItemView, MovieCollectionLoadingView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: MovieItemView,
			className: 'movies-list',
			emptyView: MovieCollectionLoadingView,

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			}
		});
	});

