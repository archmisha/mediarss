define([
	'marionette',
	'handlebars',
	'features/moviesTab/views/MovieTorrentItemView',
	'features/moviesTab/views/MovieTorrentEmptyItemView',
],
	function(Marionette, Handlebars, MovieTorrentItemView, MovieTorrentEmptyItemView) {
		"use strict";

		var emptyMessage = null;
		return Marionette.CollectionView.extend({
			itemView: MovieTorrentItemView,
			className: 'movies-list',
			emptyView: MovieTorrentEmptyItemView,

			constructor: function(options) {
				this.itemViewOptions = { vent: options.vent };
				Marionette.CollectionView.prototype.constructor.apply(this, arguments);
			},

			setEmptyMessage: function(msg) {
				emptyMessage = msg;
			},

			onRender: function() {
				this.$el.find('.movies-torrent-list-empty-label-container').html(emptyMessage);
			}
		});
	});

