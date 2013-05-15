define([
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movie-torrent-item.tpl',
	'qtip',
	'moment'
],
	function(Marionette, Handlebars, template, qtip, Moment) {
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
				movieTorrentItem: '.movie-torrent-item-inner',
				scheduledOn: '.movie-torrent-scheduled-on',
				scheduledOnDate: '.movie-torrent-scheduled-on-date'
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.ItemView.prototype.constructor.apply(this, arguments);

				var that = this;
				this.model.on('change:downloadStatus', function() {
					that.updateDownloadStatus();
				});
				this.model.on('change:scheduledDate', function(model, val) {
					if (val) {
						that.ui.scheduledOnDate.html(Moment(val).format('DD/MM/YYYY HH:mm'));
						that.ui.scheduledOn.show();
					} else {
						that.ui.scheduledOn.hide();
					}
				});
			},

			updateDownloadStatus: function() {
				this.ui.downloadImage.hide();
				this.ui.scheduledImage.hide();
				this.ui.downloadedImage.hide();

				if (this.model.get('downloadStatus') == 'SCHEDULED') {
					this.ui.scheduledImage.show();
				} else if (this.model.get('downloadStatus') == 'NONE') {
					this.ui.downloadImage.show();
				} else { // DOWNLOADED
					this.ui.downloadedImage.show();
				}
			},

			onRender: function() {
				if (!this.model.get('viewed')) {
					this.$el.addClass('movie-torrent-item-not-viewed');
				}

				this.updateDownloadStatus();

				[this.ui.downloadImage, this.ui.scheduledImage, this.ui.downloadedImage, this.ui.movieTorrentItem].forEach(
					function(el) {
						el.qtip({
							style: 'rssStyle',
							position: {
								corner: {
									target: 'bottomLeft',
									tooltip: 'topLeft'
								}
							}
						});
					});

				if (this.model.get('scheduledDate') == null) {
					this.ui.scheduledOn.hide();
				}
			},

			onDownloadButtonClick: function() {
				this.vent.trigger('movie-torrent-download', this.model);
			}
		});
	});

