define([
	'backbone',
	'features/moviesTab/models/Movie'
],
	function(Backbone, Movie) {
		'use strict';

		return Backbone.Collection.extend({
			model: Movie
		});
	});

