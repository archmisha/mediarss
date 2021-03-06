define([
	'marionette',
	'handlebars',
	'text!features/settingsTab/templates/subtitles-item.tpl',
	'moment',
	'utils/HttpUtils'
],
	function(Marionette, Handlebars, template, Moment, HttpUtils) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'subtitles-item',

			ui: {
			},

			events: {
				'click .recent-subtitles-item-download-direct': 'onDownloadSubtitlesClick'
			},

			constructor: function(options) {
				Marionette.ItemView.prototype.constructor.apply(this, arguments);
			},

			onDownloadSubtitlesClick: function() {
				console.log('a');
			}
		});
	});

