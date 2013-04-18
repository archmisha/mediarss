define([
	'marionette',
	'handlebars',
	'features/tvShowsTab/views/SearchResultItemView',
	'HttpUtils'
],
	function(Marionette, Handlebars, SearchResultItemView, HttpUtils) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: SearchResultItemView,
			className: 'show-search-results',
//			tagName: 'select',

			clearSelection: function() {
			},

			onRender: function() {
			},

			onShow: function() {
			},

			constructor: function(options) {
				this.vent = new Marionette.EventAggregator();
				this.itemViewOptions = { vent: this.vent };

				Marionette.CollectionView.prototype.constructor.apply(this, arguments);

				this.vent.on('show-episode-download', this.onEpisodeDownload, this);
			},

			onEpisodeDownload: function(userTorrent) {
				HttpUtils.post("rest/shows/episode/download", {
					torrentId: userTorrent.get('torrentId')
				}, function(res) {
					userTorrent.set('downloaded', true);
				});
			}
		});
	});

