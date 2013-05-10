define([
	'marionette',
	'handlebars',
	'text!features/tvShowsTab/templates/tracked-shows-empty-label-container.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'tracked-shows-empty-label-container'
		});
	});