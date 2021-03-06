/*global define*/
define([
	'marionette',
	'handlebars',
	'features/settingsTab/views/SubtitlesItemView'
],
	function(Marionette, Handlebars, SubtitlesItemView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: SubtitlesItemView,
			className: 'subtitles-list'
		});
	});
