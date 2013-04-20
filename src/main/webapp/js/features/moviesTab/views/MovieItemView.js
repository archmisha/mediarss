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
				movieTitle: '.movie-item-title',
				futureImage: '.movie-item-future-image',
				subTitle: '.movie-sub-title',
				scheduledOn: '.movie-scheduled-on',
				moviePreviewLink: '.movie-show-preview'
			},

			events: {
				'click': 'onMovieClick',
				'click .future-movie-item-remove-image': 'onFutureMovieRemoveClick'
			},

			constructor: function(options) {
				this.vent = options.vent;
				Marionette.ItemView.prototype.constructor.apply(this, arguments);

				var that = this;
				this.model.on('change:downloadStatus', function() {
					that.updateDownloadStatus();
				});
				this.model.on('change:selected', function() {
					if (that.model.get('selected')) {
						that.$el.addClass('movie-item-selected');
					} else {
						that.$el.removeClass('movie-item-selected');
					}
				});
			},

			onRender: function() {
				if (!this.model.get('viewed')) {
					this.$el.addClass('movie-item-not-viewed');
				}

				this.updateDownloadStatus();

				[this.ui.scheduledImage, this.ui.downloadedImage, this.ui.movieTitle, this.ui.futureImage].forEach(
					function(el) {
						el.qtip({
							style: 'rssStyle'
						});
					});

				this.ui.moviePreviewLink.fancybox({
					'width': '800',
					'height': '75%',
					'autoScale': false,
					'transitionIn': 'none',
					'transitionOut': 'none',
					'type': 'iframe'
				});
			},

			onMovieClick: function(event) {
				// if remove icon was clicked, then ignore selection
				if ($(event.target).hasClass('future-movie-item-remove-image') ||
					$(event.target).hasClass('movie-show-preview-image')) {
					return;
				}

				var that = this;
				if (!this.model.get('viewed')) {
					setTimeout(function() {
						that.$el.removeClass('movie-item-not-viewed');
					}, 2000);
				}
//				this.$el.parent().find('.movie-item').removeClass('movie-item-selected');
//				this.$el.addClass('movie-item-selected');
				this.vent.trigger('movie-selected', this.model);
			},

			updateDownloadStatus: function() {
				this.ui.scheduledImage.hide();
				this.ui.downloadedImage.hide();
				this.ui.futureImage.hide();

				if (this.model.get('downloadStatus') == 'SCHEDULED') {
					this.ui.scheduledImage.show();
				} else if (this.model.get('downloadStatus') == 'DOWNLOADED') {
					this.ui.downloadedImage.show();
				} else if (this.model.get('downloadStatus') == 'FUTURE') {
					this.ui.futureImage.show();
					this.ui.scheduledOn.show();
					this.ui.subTitle.show();
				}
			},

			onFutureMovieRemoveClick: function() {
				this.vent.trigger('future-movie-remove', this.model);
			}
		});
	});

