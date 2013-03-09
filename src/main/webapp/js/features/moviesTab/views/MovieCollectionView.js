define([
	'marionette',
	'handlebars',
	'features/moviesTab/views/MovieItemView',
	'HttpUtils'
],
	function(Marionette, Handlebars, MovieItemView, HttpUtils) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: MovieItemView,
			className: 'movies-list',

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			}
		});
	});

