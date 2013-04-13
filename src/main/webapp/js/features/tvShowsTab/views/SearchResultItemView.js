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
				downloadedStatus: '.shows-search-result-item-downloaded-status',
				downloadButton: '.shows-search-result-item-download-button',
				title: '.shows-search-result-item-title'
			},

			events: {
				'click .shows-search-result-item-download-button': 'onDownloadButtonClick'
			},

			setDownloadedState: function() {
				this.ui.downloadButton.hide();
				this.ui.downloadedStatus.css('display', 'inline'); // show make display: block we need inline here
			},

			setNotDownloadedState: function() {
				this.ui.downloadButton.show();
				this.ui.downloadedStatus.hide();
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
				this.vent = options.vent;

				var that = this;
				this.model.on('change:downloaded', function() {
					that.setDownloadedState();
				});
			},

			onRender: function() {
				if (this.model.get('downloaded')) {
					this.setDownloadedState();
				} else {
					this.setNotDownloadedState();
				}

				this.ui.title.qtip({
					style: 'rssStyle'
				});
			},

			onDownloadButtonClick: function() {
				this.vent.trigger('show-episode-download', this.model);
			}
		});
	});

