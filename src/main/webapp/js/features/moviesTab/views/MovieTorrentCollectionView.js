define([
	'marionette',
	'handlebars',
	'features/moviesTab/views/MovieTorrentItemView',
	'HttpUtils'
],
	function(Marionette, Handlebars, MovieTorrentItemView, HttpUtils) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: MovieTorrentItemView,
			className: 'movies-list',

			onRender: function() {
			},

			onShow: function() {
			},

			constructor: function(options) {
				this.vent = new Marionette.EventAggregator();
				this.itemViewOptions = { vent: this.vent };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);

				this.vent.on('movie-torrent-download', this.onMovieTorrentDownload, this);
			},

			onMovieTorrentDownload: function(userTorrent) {
				HttpUtils.post("rest/movies/download", {
					torrentId: userTorrent.get('torrentId')
				}, function(res) {
					userTorrent.set('downloadStatus', 'SCHEDULED');
				});
			}
		});
	});

