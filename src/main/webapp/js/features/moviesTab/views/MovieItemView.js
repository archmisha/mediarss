define([
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movie-item.tpl',
	'qtip'
],
	function(Marionette, Handlebars, template, qtip) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'movie-item',

			ui: {
				scheduledImage: '.movie-item-scheduled-image',
				downloadedImage: '.movie-item-downloaded-image',
				movieTitle: '.movie-item-title'
			},

			events: {
				'click': 'onMovieClick'
			},

			onMovieClick: function() {
				var that = this;
				if (!this.model.get('viewed')) {
					setTimeout(function() {
						that.$el.removeClass('movie-item-not-viewed');
					}, 2000);
				}
				this.$el.parent().find('.movie-item').removeClass('movie-item-selected');
				this.$el.addClass('movie-item-selected');
				this.vent.trigger('movie-selected', this.model);
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
			},

			onRender: function() {
				if (!this.model.get('viewed')) {
					this.$el.addClass('movie-item-not-viewed');
				}

				this.updateDownloadStatus();

				this.ui.scheduledImage.qtip({
					style: 'rssStyle'
				});
				this.ui.downloadedImage.qtip({
					style: 'rssStyle'
				});
				this.ui.movieTitle.qtip({
					style: 'rssStyle'
				});
			},

			updateDownloadStatus: function() {
				if (this.model.get('downloadStatus') == 'SCHEDULED') {
					this.ui.scheduledImage.show();
					this.ui.downloadedImage.hide();
				} else if (this.model.get('downloadStatus') == 'DOWNLOADED') {
					this.ui.scheduledImage.hide();
					this.ui.downloadedImage.show();
				} else {
					this.ui.scheduledImage.hide();
					this.ui.downloadedImage.hide();
				}
			}
		});
	});

