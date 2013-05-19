define([
	'marionette',
	'handlebars',
	'text!features/tvShowsTab/templates/search-result-item.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'shows-search-result-item',

			ui: {
				downloadedStatus: '.search-result-item-downloaded-image',
				downloadButton: '.search-result-item-download-image',
				scheduledStatus: '.search-result-item-scheduled-image',
				title: '.shows-search-result-item-title'
			},

			events: {
				'click .search-result-item-download-image': 'onDownloadButtonClick'
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
				this.vent = options.vent;

				var that = this;
				this.model.on('change:downloadStatus', function() {
					that.updateDownloadStatus();
				});
			},

			onRender: function() {
				this.updateDownloadStatus();

				this.ui.title.qtip({
					style: 'rssStyle'
				});
			},

			onDownloadButtonClick: function() {
				this.vent.trigger('show-episode-download', this.model);
			},

			updateDownloadStatus: function() {
				this.ui.downloadButton.hide();
				this.ui.scheduledStatus.hide();
				this.ui.downloadedStatus.hide();

				if (this.model.get('downloadStatus') == 'SCHEDULED') {
					this.ui.scheduledStatus.show();
				} else if (this.model.get('downloadStatus') == 'DOWNLOADED') {
					this.ui.downloadedStatus.show();
				} else if (this.model.get('downloadStatus') == 'NONE') {
					this.ui.downloadButton.show();
				}
			}
		});
	});

