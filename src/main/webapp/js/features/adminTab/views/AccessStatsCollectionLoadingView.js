define([
	'marionette',
	'handlebars',
	'text!features/adminTab/templates/access-stats-list-loading.tpl'
],
	function(Marionette, Handlebars, template) {
		"use strict";

		return Marionette.ItemView.extend({
			template: Handlebars.compile(template),
			className: 'access-stats-empty-label-container'
		});
	});