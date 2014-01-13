define([
	'backbone',
	'features/homeTab/models/Subtitles'

],
	function(Backbone, Subtitles) {
		'use strict';

		return Backbone.Collection.extend({
			model: Subtitles
		});
	});

