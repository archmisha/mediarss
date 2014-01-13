/*global define*/
define([
	'marionette',
	'handlebars',
	'features/homeTab/views/SubtitlesItemView'
],
	function(Marionette, Handlebars, SubtitlesItemView) {
		"use strict";

		return Marionette.CollectionView.extend({
			itemView: SubtitlesItemView,
			className: 'subtitles-list'
		});
	});
