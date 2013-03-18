define([
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movie-torrent-item.tpl',
	'qtip'
],
	function(Marionette, Handlebars, template, qtip) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'movie-torrent-item',

			events: {
				'click .movie-torrent-item-download-image': 'onDownloadButtonClick'
			},

			ui: {
				downloadImage: '.movie-torrent-item-download-image',
				scheduledImage: '.movie-torrent-item-scheduled-image',
				downloadedImage: '.movie-torrent-item-downloaded-image',
				movieTorrentTitle: '.movie-torrent-title',
				scheduledOn: '.movie-torrent-scheduled-on'
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.ItemView.prototype.constructor.apply(this, arguments);

				var that = this;
				this.model.on('change:downloadStatus', function() {
					that.updateDownloadStatus();
				});
			},

			updateDownloadStatus: function() {
				if (this.model.get('downloadStatus') == 'SCHEDULED') {
					this.ui.downloadImage.hide();
					this.ui.scheduledImage.show();
					this.ui.downloadedImage.hide();
				} else if (this.model.get('downloadStatus') == 'NONE') {
					this.ui.downloadImage.show();
					this.ui.scheduledImage.hide();
					this.ui.downloadedImage.hide();
				} else { // DOWNLOADED
					this.ui.downloadImage.hide();
					this.ui.scheduledImage.hide();
					this.ui.downloadedImage.show();
				}
			},

			onRender: function() {
				if (!this.model.get('viewed')) {
					this.$el.addClass('movie-torrent-item-not-viewed');
				}

				this.updateDownloadStatus();

				[this.ui.downloadImage, this.ui.scheduledImage, this.ui.downloadedImage, this.ui.movieTorrentTitle].forEach(
					function(el) {
						el.qtip({
							style: 'rssStyle'
						});
					});

				if (this.model.get('scheduledDate') == null) {
					this.ui.hide();
				}
			},

			onDownloadButtonClick: function() {
				this.vent.trigger('movie-torrent-download', this.model);
			}
		});
	});

