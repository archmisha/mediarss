define([
	'marionette',
	'handlebars',
	'features/moviesTab/views/MovieTorrentItemView',
],
	function(Marionette, Handlebars, MovieTorrentItemView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: MovieTorrentItemView,
			className: 'movies-list',

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };
				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			}
		});
	});

