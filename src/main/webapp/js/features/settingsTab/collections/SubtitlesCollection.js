define([
	'backbone',
	'features/settingsTab/models/Subtitles'

],
	function(Backbone, Subtitles) {
		'use strict';

		return Backbone.Collection.extend({
			model: Subtitles
		});
	});

