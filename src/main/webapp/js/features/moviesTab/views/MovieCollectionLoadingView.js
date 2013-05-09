define([
	'marionette',
	'handlebars',
	'text!features/moviesTab/templates/movie-list-loading.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'movies-list-empty-label-container'
		});
	});